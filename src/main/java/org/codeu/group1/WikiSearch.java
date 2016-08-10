package org.codeu.group1;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {

	// map from URLs that contain the term(s) to relevance score
	private Map<String, Integer> map;

	/**
	 * Constructor.
	 *
	 * @param map
	 */
	public WikiSearch(Map<String, Integer> map) {
		this.map = map;
	}

	/**
	 * Looks up the URLs of the WikiSearch.
	 */
	public Set<String> getURLs() {
		return this.map.keySet();
	}


	/**
	 * Looks up the relevance of a given URL.
	 *
	 * @param url
	 * @return
	 */
	public Integer getRelevance(String url) {
		Integer relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}

	/**
	 * Prints the contents in order of term frequency.
	 *
	 * @param map
	 */
	private  void print() {
		List<Entry<String, Integer>> entries = sort();
		for (Entry<String, Integer> entry: entries) {
			System.out.println(entry);
		}
	}

	/**
	 * Prints N contents in order of term frequency.
	 *
	 * @param map
	 */
	public void printN(int n) {
		List<Entry<String, Integer>> entries = sort();
		for (int i = 0; i < n; i++) {
			System.out.println(entries.get(i));
		}
	}

	/**
	 * Computes the union of two search results.
	 *
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
		Map<String, Integer> or_map = new HashMap<String, Integer>(this.map);

		for (String url : that.getURLs()) {
			or_map.put(url, totalRelevance(this.getRelevance(url),
			                               that.getRelevance(url)));
		}
		return new WikiSearch(or_map);
	}

	/**
	 * Computes the intersection of two search results.
	 *
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
		Map<String, Integer> and_map = new HashMap<String, Integer>();
		Set<String> intersection = this.getURLs();
		intersection.retainAll(that.getURLs());

		for (String url : intersection) {
			and_map.put(url, totalRelevance(this.getRelevance(url),
			                               that.getRelevance(url)));
		}
		return new WikiSearch(and_map);
	}

	/**
	 * Computes the intersection of two search results.
	 *
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
		Map<String, Integer> minus_map = new HashMap<String, Integer>(this.map);

		for (String url : that.getURLs()) {
			minus_map.remove(url);
		}
		return new WikiSearch(minus_map);
	}

	/**
	 * Computes the relevance of a search with multiple terms.
	 *
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 *
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Integer>> sort() {
    List<Entry<String, Integer>> sorted = new LinkedList<>(map.entrySet());

		Collections.sort(sorted, new Comparator<Entry<String, Integer>> () {
			public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
	        return e2.getValue().compareTo(e1.getValue());
	    }
		});
		return sorted;
	}
}
