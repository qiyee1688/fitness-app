package com.fitness.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PostgreSqlIndexDefinitionTest {
    private static final Path SQL_RESOURCE_ROOT = Path.of("src/main/resources");
    private static final Pattern INDEX_KEY_LIST = Pattern.compile(
            "(?is)CREATE\\s+(?:UNIQUE\\s+)?INDEX\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?"
                    + "[a-z_][a-z0-9_]*\\s+ON\\s+[a-z_][a-z0-9_]*\\s*\\(([^)]*)\\)");
    private static final Pattern COLUMN_KEY = Pattern.compile(
            "(?i)[a-z_][a-z0-9_]*(?:\\s+[a-z_][a-z0-9_]*)?(?:\\s+(?:ASC|DESC))?");

    @Test
    void indexKeysUseOnlyColumnsToAvoidNonImmutablePostgresExpressions() throws IOException {
        List<String> invalidDefinitions = new ArrayList<>();

        try (Stream<Path> resourcePaths = Files.walk(SQL_RESOURCE_ROOT)) {
            resourcePaths
                    .filter(path -> path.toString().endsWith(".sql") || path.toString().endsWith(".xml"))
                    .forEach(path -> collectExpressionIndexDefinitions(path, invalidDefinitions));
        }

        assertThat(invalidDefinitions)
                .as("PostgreSQL index keys must not contain functions or casts; a column may declare an operator class")
                .isEmpty();
    }

    private void collectExpressionIndexDefinitions(Path path, List<String> invalidDefinitions) {
        try {
            String sql = Files.readString(path);
            Matcher matcher = INDEX_KEY_LIST.matcher(sql);
            while (matcher.find()) {
                String keys = matcher.group(1).trim();
                boolean hasExpressionKey = Stream.of(keys.split(","))
                        .map(String::trim)
                        .anyMatch(key -> !COLUMN_KEY.matcher(key).matches());
                if (hasExpressionKey) {
                    invalidDefinitions.add(path + ": " + matcher.group().replaceAll("\\s+", " "));
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to inspect SQL resource " + path, exception);
        }
    }
}
