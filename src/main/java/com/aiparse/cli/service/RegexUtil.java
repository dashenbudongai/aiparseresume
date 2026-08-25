package com.aiparse.cli.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class RegexUtil {
    private RegexUtil() {}

    static String firstMatch(String text, String regex) {
        if (text == null) return null;
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group() : null;
    }
}
