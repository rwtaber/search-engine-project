package org.codeu.group1;

import redis.clients.jedis.Jedis;

public class SearchEngine {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("100.111.160.126", 6379);
        jedis.auth("codeUgroup1");
    }
}
