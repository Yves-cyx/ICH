package com.yingjian.feiyi.ui;

import com.yingjian.feiyi.data.Models.Heritage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class CatalogSearch {
    private CatalogSearch() {}

    static List<Heritage> match(List<Heritage> entries, String query) {
        String keyword = normalize(query);
        var hits = new ArrayList<Hit>();
        for (Heritage entry : entries) {
            String title = normalize(entry.title);
            if (title.isEmpty()) continue;
            int rank = rank(entry, title, keyword);
            if (rank >= 0) hits.add(new Hit(entry, rank));
        }
        hits.sort(Comparator.comparingInt(Hit::rank).thenComparingLong(hit -> hit.entry().id));
        List<Heritage> results = new ArrayList<>(hits.size());
        for (Hit hit : hits) results.add(hit.entry());
        return results;
    }

    private static int rank(Heritage entry, String title, String keyword) {
        if (keyword.isEmpty() || title.equals(keyword)) return 0;
        if (title.startsWith(keyword)) return 1;
        if (title.contains(keyword)) return 2;
        if (normalize(entry.region).contains(keyword) || normalize(entry.summary).contains(keyword)) return 3;
        return -1;
    }

    static String normalize(String text) {
        if (text == null) return "";
        int start = 0;
        int end = text.length();
        while (start < end && isSpace(text.charAt(start))) start++;
        while (end > start && isSpace(text.charAt(end - 1))) end--;
        return text.substring(start, end).toLowerCase(Locale.ROOT);
    }

    private static boolean isSpace(char character) {
        return Character.isWhitespace(character) || Character.isSpaceChar(character);
    }

    private record Hit(Heritage entry, int rank) {}
}
