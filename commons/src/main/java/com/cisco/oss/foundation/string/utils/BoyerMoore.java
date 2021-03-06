/*
 * Copyright 2014 Cisco Systems, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.cisco.oss.foundation.string.utils;

/**
 * This class implements BoyerMoore algorithm for searching a pattern in a string
 * @author ykasten
 *
 */
public class BoyerMoore {

    public static final int ALPHABET_SIZE = Character.MAX_VALUE + 1;

    private String text;
    private String pattern;

    private int[] last;
    private int[] match;
    private int[] suffix;



    @Override
    public boolean equals(Object other){
        return pattern.equals(((BoyerMoore)other).pattern);
    }

    @Override
    public int hashCode(){
        return pattern.hashCode();
    }

    /**
     * Build BoyerMoore object with a giving pattern, making lastLoction array from the pattern
     * @param pattern
     */
    public BoyerMoore(String pattern) {
        this.pattern = pattern;
        last = new int[ALPHABET_SIZE];
        match = new int[pattern.length()];
        suffix = new int[pattern.length()];

        // Preprocessing
        computeLast();
        computeMatch();

    }


    /**
     * Searching the pattern in the text
     * @param text the text to search the pattern
     * @return -1 if the pattern was not found, location of the pattern if it was found.
     */
    public int search(String text) {

        // Searching
        int i = pattern.length() - 1;
        int j = pattern.length() - 1;
        while (i < text.length()) {
            if (pattern.charAt(j) == text.charAt(i)) {
                if (j == 0) {
                    //the left-most match is found
                    return i;
                }
                j--;
                i--;
            } else { //a difference
                i += pattern.length() - j - 1 + Math.max(j - last[text.charAt(i)], match[j]);
                j = pattern.length() - 1;
            }
        }
        return -1;
    }

    public String getPattern() {
        return pattern;
    }

    /**
     * Computes the function <i>last</i> and stores its values in the array <code>last</code>.
     * The function is defined as follows:
     * <pre>
     * last(Char ch) = the index of the right-most occurrence of the character ch
     *                                                           in the pattern;
     *                 -1 if ch does not occur in the pattern.
     * </pre>
     * The running time is O(pattern.length() + |Alphabet|).
     */
    private void computeLast() {
        for (int k = 0; k < last.length; k++) {
            last[k] = -1;
        }
        for (int j = pattern.length()-1; j >= 0; j--) {
            if (last[pattern.charAt(j)] < 0) {
                last[pattern.charAt(j)] = j;
            }
        }
    }

    /**
     * Computes the function <i>match</i> and stores its values in the array <code>match</code>.
     * The function is defined as follows:
     * <pre>
     * match(j) = min{ s | 0 < s <= j && p[j-s]!=p[j]
     *                            && p[j-s+1]..p[m-s-1] is suffix of p[j+1]..p[m-1] },
     *                                                         if such s exists, else
     *            min{ s | j+1 <= s <= m
     *                            && p[0]..p[m-s-1] is suffix of p[j+1]..p[m-1] },
     *                                                         if such s exists,
     *            m, otherwise,
     * where m is the pattern's length and p is the pattern.
     * </pre>
     * The running time is O(pattern.length()).
     */
    private void computeMatch() {
    /* Phase 1 */
        for (int j = 0; j < match.length; j++) {
            match[j] = match.length;
        } //O(m)

        computeSuffix(); //O(m)

    /* Phase 2 */
        //Uses an auxiliary array, backwards version of the KMP failure function.
        //suffix[i] = the smallest j > i s.t. p[j..m-1] is a prefix of p[i..m-1],
        //if there is no such j, suffix[i] = m

        //Compute the smallest shift s, such that 0 < s <= j and
        //p[j-s]!=p[j] and p[j-s+1..m-s-1] is suffix of p[j+1..m-1] or j == m-1},
        //                                                         if such s exists,
        for (int i = 0; i < match.length - 1; i++) {
            int j = suffix[i + 1] - 1; // suffix[i+1] <= suffix[i] + 1
            if (suffix[i] > j) { // therefore pattern[i] != pattern[j]
                match[j] = j - i;
            } else {// j == suffix[i]
                match[j] = Math.min(j - i + match[i], match[j]);
            }
        } //End of Phase 2

    /* Phase 3 */
        //Uses the suffix array to compute each shift s such that
        //p[0..m-s-1] is a suffix of p[j+1..m-1] with j < s < m
        //and stores the minimum of this shift and the previously computed one.
        if (suffix[0] < pattern.length()) {
            for (int j = suffix[0] - 1; j >= 0; j--) {
                if (suffix[0] < match[j]) { match[j] = suffix[0]; }
            }
            int j = suffix[0];
            for (int k = suffix[j]; k < pattern.length(); k = suffix[k]) {
                while (j < k) {
                    if (match[j] > k) match[j] = k;
                    j++;
                }
            }
        }//endif
    }

    /**
     * Computes the values of <code>suffix</code>, which is an auxiliary array,
     * backwards version of the KMP failure function.
     * <br>
     * suffix[i] = the smallest j > i s.t. p[j..m-1] is a prefix of p[i..m-1],
     * if there is no such j, suffix[i] = m, i.e. <br>
     * p[suffix[i]..m-1] is the longest prefix of p[i..m-1], if suffix[i] < m.
     * <br>
     * The running time for computing the <code>suffix</code> is O(m).
     */
    private void computeSuffix() {
        suffix[suffix.length-1] = suffix.length;
        int j = suffix.length - 1;
        //suffix[i] = m - the length of the longest prefix of p[i..m-1]
        for (int i = suffix.length - 2; i >= 0; i--) {
            while (j < suffix.length - 1 && pattern.charAt(j) != pattern.charAt(i)) {
                j = suffix[j + 1] - 1;
            }
            if (pattern.charAt(j) == pattern.charAt(i)) { j--; }
            suffix[i] = j + 1;
        }

    }


//	public void setPattern(String pattern) {
//		this.pattern = pattern;
//	}


}