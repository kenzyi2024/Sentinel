package com.sentinel.security;

import com.sentinel.domain.enums.ResourceSensitivity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Classifies a targeted resource (usually a file path) into a {@link ResourceSensitivity} tier.
 *
 * <p>Classification is deliberately ordered from most to least sensitive and returns on the first
 * match, so a path is always assigned its <em>highest</em> applicable tier. Directory markers are
 * matched against the whole path; extension/name markers are matched against the final path
 * segment to avoid false positives (e.g. {@code Tokenizer.java} is source, not a secret).
 */
@Component
public class ResourceClassifier {

    private static final List<String> SECRET_PATH_MARKERS = List.of(
            "/secrets/", "secrets/", "/.ssh/", "/.aws/", "/.gnupg/", "/vault/");
    private static final List<String> SECRET_NAME_SUFFIXES = List.of(
            ".env", ".pem", ".key", ".keystore", ".jks", ".p12", ".pfx", ".ppk");
    private static final List<String> SECRET_NAME_MARKERS = List.of(
            "id_rsa", "id_ed25519", "credentials", ".npmrc", ".pypirc", ".netrc", ".htpasswd");

    private static final List<String> SENSITIVE_NAME_EXACT = List.of(
            "pom.xml", "build.gradle", "build.gradle.kts", "package.json", "package-lock.json",
            "yarn.lock", "requirements.txt", "pipfile", "go.mod", "cargo.toml", "gemfile",
            "dockerfile", "makefile", ".gitignore", ".dockerignore");
    private static final List<String> SENSITIVE_PATH_MARKERS = List.of(
            "/.github/", ".github/", "/.git/", "/config/", "/infra/", "/deploy/", "/k8s/", "/.circleci/");
    private static final List<String> SENSITIVE_NAME_SUFFIXES = List.of(
            ".yml", ".yaml", ".toml", ".ini", ".conf", ".config", ".tf", ".tfvars", ".properties");

    private static final List<String> INTERNAL_PATH_MARKERS = List.of(
            "src/", "/src/", "lib/", "/lib/", "test/", "/test/", "app/", "/app/");
    private static final List<String> INTERNAL_NAME_SUFFIXES = List.of(
            ".java", ".ts", ".tsx", ".js", ".jsx", ".py", ".go", ".rs", ".rb", ".c", ".cpp", ".h",
            ".cs", ".kt", ".php", ".sql", ".md", ".txt", ".html", ".css");

    public ResourceSensitivity classify(String resource) {
        if (resource == null || resource.isBlank()) {
            return ResourceSensitivity.PUBLIC;
        }
        String path = resource.toLowerCase(Locale.ROOT).trim();
        String name = fileName(path);

        if (containsAny(path, SECRET_PATH_MARKERS)
                || endsWithAny(name, SECRET_NAME_SUFFIXES)
                || name.startsWith(".env")
                || containsAny(name, SECRET_NAME_MARKERS)) {
            return ResourceSensitivity.SECRET;
        }
        if (SENSITIVE_NAME_EXACT.contains(name)
                || containsAny(path, SENSITIVE_PATH_MARKERS)
                || endsWithAny(name, SENSITIVE_NAME_SUFFIXES)) {
            return ResourceSensitivity.SENSITIVE;
        }
        if (containsAny(path, INTERNAL_PATH_MARKERS) || endsWithAny(name, INTERNAL_NAME_SUFFIXES)) {
            return ResourceSensitivity.INTERNAL;
        }
        return ResourceSensitivity.PUBLIC;
    }

    private static String fileName(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static boolean containsAny(String haystack, List<String> needles) {
        for (String n : needles) {
            if (haystack.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private static boolean endsWithAny(String haystack, List<String> suffixes) {
        for (String s : suffixes) {
            if (haystack.endsWith(s)) {
                return true;
            }
        }
        return false;
    }
}
