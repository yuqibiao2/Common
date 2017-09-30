package com.afrid.common.utils;

/**
 * Created by Alex on 2017/1/28.
 */

public class Encryptor {
    public static final char[] SEED = "OEMFANGKA".toCharArray();

    public static String encrypt(String input) {

        StringBuilder output = new StringBuilder();
        char[] inputCharArr = input.toCharArray();

        for(int i=1; i<=input.length(); i++) {
            int j = i % SEED.length + 1;
            output.append((char)((int)inputCharArr[i-1] ^ ((int)SEED[j-1] % 10)));
        }

        return output.toString();
    }
}
