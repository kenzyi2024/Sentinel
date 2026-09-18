package com.sentinel.policy;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Matches a resource path against a glob-style policy pattern. Supported wildcards:
 * <ul>
 *   <li>{@code *} — any run of characters except a path separator</li>
 *   <li>{@code **} — any run of characters including separators</li>
 *   <li>{@code ?} — any single character</li>
 * </ul>
 *
 * <p>A pattern matches if it matches the full path <em>or</em> the final path segment, so
 * {@code .env} matches both {@code .env} and {@code config/.env} without requiring callers to
 * write {@code **&#47;.env}. Matching is case-insensitive.
 */
public final class ResourceMatcher {

    private ResourceMatcher() {
    }

    public static boolean matches(String pattern, String resource) {
        if (pattern == null || pattern.isBlank()) {
            return true; // no constraint
        }
        if (resource == null) {
            return false;
        }
        String path = resource.toLowerCase(Locale.ROOT);
        String fileName = fileName(path);
        Pattern regex = globToRegex(pattern.toLowerCase(Locale.ROOT));
        return regex.matcher(path).matches() || regex.matcher(fileName).matches();
    }

    private static String fileName(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static Pattern globToRegex(String glob) {
        StringBuilder sb = new StringBuilder("^");
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
                case '*' -> {
                    if (i + 1 < glob.length() && glob.charAt(i + 1) == '*') {
                        sb.append(".*");
                        i++; // consume the second '*'
                    } else {
                        sb.append("[^/]*");
                    }
                }
                case '?' -> sb.append('.');
                case '.', '(', ')', '+', '|', '^', '$', '@', '%', '{', '}', '[', ']', '\\' ->
                        sb.append('\\').append(c);
                default -> sb.append(c);
            }
        }
        sb.append('$');
        return Pattern.compile(sb.toString());
    }
}
