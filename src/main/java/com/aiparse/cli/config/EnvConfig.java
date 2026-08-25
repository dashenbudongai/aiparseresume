package com.aiparse.cli.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal .env loader. Reads KEY=VALUE pairs from a `.env` file in the
 * current working directory (or an explicitly given path) and exposes
 * them via {@link #get(String)}. Values from the actual process
 * environment take precedence — callers should query
 * {@code System.getenv()} first, then fall back to this map.
 *
 * <p>The parser intentionally supports the most common subset:
 * <ul>
 *   <li>Blank lines and lines starting with {@code #} are ignored.</li>
 *   <li>Keys may be optionally exported via a leading {@code export }.</li>
 *   <li>Values may be wrapped in single or double quotes; surrounding
 *       whitespace is stripped.</li>
 *   <li>Inline comments after a value (preceded by whitespace + {@code #})
 *       are stripped.</li>
 * </ul>
 */
public final class EnvConfig {

    private static final String DEFAULT_FILE = ".env";

    private final Map<String, String> values;

    private EnvConfig(Map<String, String> values) {
        this.values = values;
    }

    /**
     * Load the .env file from the current working directory. If the file
     * does not exist, an empty (but non-null) instance is returned.
     */
    public static EnvConfig load() {
        return load(Path.of(DEFAULT_FILE));
    }

    /**
     * Load the given .env file. A missing or unreadable file yields an
     * empty config.
     */
    public static EnvConfig load(Path file) {
        Map<String, String> map = new HashMap<>();
        if (file == null || !Files.exists(file)) {
            return new EnvConfig(map);
        }
        try {
            for (String raw : Files.readAllLines(file)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.startsWith("export ")) {
                    line = line.substring("export ".length()).trim();
                }
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim();
                String value = stripComment(line.substring(eq + 1)).trim();
                value = unquote(value);
                if (!key.isEmpty()) {
                    map.put(key, value);
                }
            }
        } catch (IOException e) {
            // swallow — treat as empty config
        }
        return new EnvConfig(map);
    }

    /** Return the value for {@code key}, or {@code null} if not set. */
    public String get(String key) {
        return values.get(key);
    }

    /** Return the value for {@code key}, or {@code defaultValue} if not set. */
    public String getOrDefault(String key, String defaultValue) {
        String v = values.get(key);
        return (v == null || v.isEmpty()) ? defaultValue : v;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    // ---- helpers ----

    static String stripComment(String s) {
        // Walk through and stop at the first " #" preceded by whitespace,
        // or a " #" at the start. Single/double quoted values are skipped
        // so '#' inside them is preserved.
        boolean single = false;
        boolean dbl = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'' && !dbl) single = !single;
            else if (c == '"' && !single) dbl = !dbl;
            else if (c == '#' && !single && !dbl) {
                // must be preceded by whitespace to count as inline comment
                if (i == 0 || Character.isWhitespace(s.charAt(i - 1))) {
                    return s.substring(0, i);
                }
            }
        }
        return s;
    }

    static String unquote(String s) {
        if (s.length() >= 2) {
            char first = s.charAt(0);
            char last = s.charAt(s.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return s.substring(1, s.length() - 1);
            }
        }
        return s;
    }
}
