package org.codeu.group1;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import redis.clients.jedis.Jedis;
import org.jsoup.select.Elements;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.glove.Glove.Builder;
import org.deeplearning4j.models.glove.Glove;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import java.util.Scanner;

import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.io.File;
import java.io.FileInputStream;

public class SearchEngine {

    public static void main(String[] args) throws Exception {
/*
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";

        Jedis jedis = new Jedis("100.111.160.126", 6379);
        jedis.auth("codeUgroup1");

        JedisIndex index = new JedisIndex(jedis);

        WikiCrawler wc = new WikiCrawler(source, index);
        WikiFetcher wf = new WikiFetcher();
*/

        List<String> sentences = new ArrayList<>();
        File gModel = new File("GoogleNews-vectors-negative300.bin.gz");
        WordVectors old_vec = WordVectorSerializer.loadGoogleModel(gModel, true);

/*        File dir = new File("src/main/resources");
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
        }*/
/*
        Elements paragraphs = wf.fetchWikipedia(source);
        String raw = paragraphs.text().replaceAll("\\[.*?\\] ?", "");

        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(raw);

        List<String> sentences = new ArrayList<>();
        int start = iterator.first();
        for (int end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
            sentences.add(raw.substring(start, end));
        }

        for(String sentence : sentences) {
            System.out.println(sentence);
        }

        SentenceIterator iter = new CollectionSentenceIterator(sentences);

        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());



        Glove new_vec = new Glove.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(10)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .useExistingWordVectors(old_vec)
                .build();

        new_vec.fit();
*/

        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter what you like to search: ");

        String currentLine;
        while (!(currentLine = scanner.nextLine()).equals("quit")) {
            System.out.println(old_vec.wordsNearest(currentLine, 10));
            System.out.print("Please enter another thing you would like to search: ");
        }
    }
}
