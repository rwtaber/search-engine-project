package org.codeu.group1;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.*;

public class SearchEngine {
    private Jedis jedis = null;
    private JedisIndex index = null;
    private WikiCrawler wc = null;
    private File gModel = null;
    private WordVectors vec = null;

    public static void main(String[] args) throws Exception {
        SearchEngine se = new SearchEngine();
        se.setJedis("localhost", 11120, null);
        se.setModel();

        Scanner scanner = new Scanner(System.in);
        printMenu();

        String currentLine;
        while (!(currentLine = scanner.nextLine()).equals("quit")) {
            int scase;
            try {
                scase = Integer.parseInt(currentLine);
            } catch (NumberFormatException e) {
                System.out.println("Input not an int, try again");
                continue;
            }

            switch (scase) {
                case 1: se.doIndexCrawl(); break;
                case 2: se.searchIndex(); break;
                case 3: se.searchVec(); break;
                default: System.out.println("Input a number 1-3"); break;
            }
            printMenu();
        }
    }

    private static void printMenu() {
        System.out.println("Group One search engine 0.1");
        System.out.println("Select and enter an option from below, or type quit to quit:");
        System.out.println("1) Crawl and index page");
        System.out.println("2) Search for a term");
        System.out.println("3) Search word2vec");
    }

    private void doIndexCrawl() throws Exception {
        Scanner reader = new Scanner(System.in);
        String source;
        int pagesToCrawl;

        System.out.println("Enter Wikipedia URL to use as source:");
        source = reader.nextLine();
        System.out.println("Enter the number of pages to index:");
        pagesToCrawl = reader.nextInt();

        wc = new WikiCrawler(source, index);
        int pagesCrawled = 0;

        while(pagesCrawled <= pagesToCrawl) {
            wc.crawl(false);
            pagesCrawled++;
        }
    }

    private void searchIndex() {
        Scanner reader = new Scanner(System.in);
        String term;

        System.out.println("Enter term to search for:");
        term = reader.nextLine();
        Map<String, Integer> map = index.getCounts(term.toLowerCase());
        WikiSearch search = new WikiSearch(map);
        search.printN(10);
    }

    private void searchVec() {
        Scanner reader = new Scanner(System.in);
        String word;

        System.out.println("Enter word to vector search:");
        word = reader.nextLine();
        System.out.println(vec.wordsNearest(word, 10));
    }

/*    private void doSearch() {
        Scanner reader = new Scanner(System.in);
        String term;

        System.out.println("Enter a term to search for:");
        source = reader.nextLine();
        System.out.println("Enter the number of pages to index:");
        pagesToCrawl = reader.nextInt();
    }*/

    private void setJedis(String host, int port, String password) {
        jedis = new Jedis(host, port, 30);
        if (password != null) jedis.auth(password);
        index = new JedisIndex(jedis);
    }

    private void setModel() throws Exception {
        gModel = new File("GoogleNews-vectors-negative300.bin.gz");
        vec = WordVectorSerializer.loadGoogleModel(gModel, true);
    }

    private List<String> getSentences(String directory) throws Exception {
        List<String> sentences = new ArrayList<>();

        File dir = new File(directory);
        File[] directoryListing = dir.listFiles();

        if (directoryListing != null) {
            for (File child : directoryListing) {
                InputStream stream = new FileInputStream(child);

                Document doc = Jsoup.parse(stream, "UTF-8", "");
                Element content = doc.getElementById("mw-content-text");
                Elements paras = content.select("p");

                String raw = paras.text().replaceAll("\\[.*?\\] ?", "");

                BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
                iterator.setText(raw);

                int start = iterator.first();
                for (int end = iterator.next();
                     end != BreakIterator.DONE;
                     start = end, end = iterator.next()) {
                    sentences.add(raw.substring(start, end));
                }
            }
        }

        return sentences;
    }

    private Word2Vec trainModel(List<String> sentences) {
        SentenceIterator iter = new CollectionSentenceIterator(sentences);

        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());

        Word2Vec new_vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(10)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        new_vec.fit();
        return new_vec;
    }
}
