package pl.bkwapisz.taskprocessor.processing;


import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.function.Consumer;

@Component
@Slf4j
class TextSearchPatternEngine {

    /**
     * Threshold of equal characters in string when we decide that String is equal enough to be considered as searched pattern
     */
    private static final float MINIMUM_EQUAL_CHARACTER_RATIO = 0.6f;

    private final Random random = new Random();

    record SearchResult(int position, int typos) {
        static SearchResult notFound() {
            return new SearchResult(-1, 0);
        }
    }

    public SearchResult findPattern(final String input, final String pattern, final Consumer<Integer> progressConsumer) {
        log.debug("findPattern for input: {}, pattern: {}", input, pattern);
        if (StringUtils.isEmpty(pattern)) {
            progressConsumer.accept(100);
            return new SearchResult(0, 0);
        } else if (StringUtils.isEmpty(input) || input.length() < pattern.length()) {
            progressConsumer.accept(100);
            return SearchResult.notFound();
        } else {
            var lastEmittedProgress = -1;
            for (int position = 0; position <= input.length() - pattern.length(); position++) {
                
                final var progressToEmit = position * 100 / input.length();
                if (lastEmittedProgress != progressToEmit) {
                    progressConsumer.accept(progressToEmit);
                    lastEmittedProgress = progressToEmit;
                }

                final var examinedSubstring = input.substring(position, position + pattern.length());
                final var equalCharactersCount = findEqualCharactersCount(examinedSubstring, pattern);
                if ((float) equalCharactersCount / pattern.length() >= MINIMUM_EQUAL_CHARACTER_RATIO) {
                    progressConsumer.accept(100);
                    return new SearchResult(position, pattern.length() - equalCharactersCount);
                }
            }

            progressConsumer.accept(100);
            return SearchResult.notFound();
        }
    }

    private int findEqualCharactersCount(final String examinedSubstring, final String pattern) {
        int equalCharactersCount = 0;
        for (int i = 0; i < examinedSubstring.length(); i++) {
            if (examinedSubstring.charAt(i) == pattern.charAt(i)) {
                equalCharactersCount++;
            }
        }
        return equalCharactersCount;
    }
}
