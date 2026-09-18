package com.sentinel.security;

import com.sentinel.domain.enums.ResourceSensitivity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceClassifierTest {

    private final ResourceClassifier classifier = new ResourceClassifier();

    @Test
    void classifiesSecretResources() {
        assertThat(classifier.classify(".env")).isEqualTo(ResourceSensitivity.SECRET);
        assertThat(classifier.classify("config/.env")).isEqualTo(ResourceSensitivity.SECRET);
        assertThat(classifier.classify("secrets/credentials.json")).isEqualTo(ResourceSensitivity.SECRET);
        assertThat(classifier.classify("deploy/server.pem")).isEqualTo(ResourceSensitivity.SECRET);
        assertThat(classifier.classify("home/user/.ssh/id_rsa")).isEqualTo(ResourceSensitivity.SECRET);
    }

    @Test
    void classifiesSensitiveConfigAndManifests() {
        assertThat(classifier.classify("pom.xml")).isEqualTo(ResourceSensitivity.SENSITIVE);
        assertThat(classifier.classify("package.json")).isEqualTo(ResourceSensitivity.SENSITIVE);
        assertThat(classifier.classify("config/application.yml")).isEqualTo(ResourceSensitivity.SENSITIVE);
        assertThat(classifier.classify(".github/workflows/ci.yml")).isEqualTo(ResourceSensitivity.SENSITIVE);
    }

    @Test
    void classifiesInternalSourceAndDocs() {
        assertThat(classifier.classify("src/main/java/com/acme/App.java")).isEqualTo(ResourceSensitivity.INTERNAL);
        assertThat(classifier.classify("README.md")).isEqualTo(ResourceSensitivity.INTERNAL);
        // A source file whose name merely contains "token" must not be mistaken for a secret.
        assertThat(classifier.classify("src/main/java/Tokenizer.java")).isEqualTo(ResourceSensitivity.INTERNAL);
    }

    @Test
    void defaultsToPublic() {
        assertThat(classifier.classify("data")).isEqualTo(ResourceSensitivity.PUBLIC);
        assertThat(classifier.classify(null)).isEqualTo(ResourceSensitivity.PUBLIC);
        assertThat(classifier.classify("")).isEqualTo(ResourceSensitivity.PUBLIC);
    }
}
