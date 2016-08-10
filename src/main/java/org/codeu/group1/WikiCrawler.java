package org.codeu.group1;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Comment;


public class WikiCrawler {
    // keeps track of where we started
    private final String source;

    // the index where the results go
    private JedisIndex index;

    // queue of URLs to be indexed
    private Queue<String> queue = new LinkedList<String>();

    // fetcher used to get pages from Wikipedia
    final static WikiFetcher wf = new WikiFetcher();

    private final static String site = "https://en.wikipedia.org";
    /**
     * Constructor.
     *
     * @param source
     * @param index
     */
    public WikiCrawler(String source, JedisIndex index) {
        this.source = source;
        this.index = index;
        queue.offer(source);
    }

    /**
     * Returns the number of URLs in the queue.
     *
     * @return
     */
    public int queueSize() {
        return queue.size();
    }

    /**
     * Gets a URL from the queue and indexes it.
     * @param
     *
     * @return Number of pages indexed.
     * @throws IOException
     */
    public String crawl(boolean testing) throws IOException {
        String url = queue.remove();

        if (index.isIndexed(url) && !testing) {
            return null;
        }

        Elements url_p = testing ? wf.readWikipedia(url) : wf.fetchWikipedia(url);
        index.indexPage(url, url_p);
        queueInternalLinks(url_p);

        return url;
    }

    /**
     * Parses paragraphs and adds internal links to the queue.
     *
     * @param paragraphs
     */
    // NOTE: absence of access level modifier means package-level
    void queueInternalLinks(Elements paragraphs) {
        for (Element p : paragraphs) {
            Iterable<Node> iter = new WikiNodeIterable(p);

            for(Node node : iter) {
                if (node instanceof TextNode) continue;
                if (node instanceof Comment) continue;
                Element e = (Element) node;

                if (e.tagName() == "a") {
                    String href = e.attr("href").split("#")[0];
                    String rel = e.attr("rel");

                    if (!href.isEmpty() &&
                            !href.contains("index.php") &&
                            !rel.equals("nofollow") &&
                            !href.contains(":") &&
                            href.startsWith("/wiki/")) {
                        queue.add(site + href);
                    }
                }
            }
        }
    }
}
