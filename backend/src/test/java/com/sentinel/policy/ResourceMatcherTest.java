package com.sentinel.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceMatcherTest {

    @Test
    void doubleStarMatchesAcrossDirectories() {
        assertThat(ResourceMatcher.matches("secrets/**", "secrets/credentials.json")).isTrue();
        assertThat(ResourceMatcher.matches("src/**", "src/main/java/App.java")).isTrue();
    }

    @Test
    void singleStarMatchesWithinASegment() {
        assertThat(ResourceMatcher.matches("*.java", "App.java")).isTrue();
        assertThat(ResourceMatcher.matches("*.md", "App.java")).isFalse();
    }

    @Test
    void matchesAgainstFileNameWhenPatternHasNoSlash() {
        assertThat(ResourceMatcher.matches(".env", "config/.env")).isTrue();
    }

    @Test
    void blankPatternIsAWildcard() {
        assertThat(ResourceMatcher.matches(null, "anything")).isTrue();
        assertThat(ResourceMatcher.matches("", "anything")).isTrue();
    }
}
