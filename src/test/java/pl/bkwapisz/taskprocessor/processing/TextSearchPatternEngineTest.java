package pl.bkwapisz.taskprocessor.processing;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pl.bkwapisz.taskprocessor.processing.TextSearchPatternEngine.SearchResult;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class TextSearchPatternEngineTest {

    private static final Consumer<Integer> IGNORE_PROGRESS = p -> {
    };

    static class TestProgressValidator implements Consumer<Integer> {

        int gotProgressTimes = 0;
        int lastProgress = -1;

        @Override
        public void accept(final Integer progress) {
            assertThat(progress).isGreaterThanOrEqualTo(0).isLessThanOrEqualTo(100);
            assertThat(progress).isGreaterThanOrEqualTo(lastProgress);
            gotProgressTimes++;
            lastProgress = progress;
        }
    }

    private final TextSearchPatternEngine uut = new TextSearchPatternEngine();

    @ParameterizedTest
    @CsvSource(textBlock = """
            ,,0
            '','',0
            text,'',0
            test,test,0
            testxxxx,test,0
            testxxxx,test,0
            xxxxtestxxxx,test,4
            xxxxtest,test,4
            'test ',' ',4
            test1,1,4
            testą,ą,4
            test?,?,4
            """)
    void shouldFindTextWithoutTypos(final String input, final String pattern, final int expectedPosition) {
        // when
        final SearchResult result = uut.findPattern(input, pattern, IGNORE_PROGRESS);

        // then
        assertThat(result.position()).isEqualTo(expectedPosition);
        assertThat(result.typos()).isEqualTo(0);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            '',' '
            '',a
            xxxx,test
            tes,test
            xxxxtes,test
            texxxxst,test
            xxxxtestxxxx,testtest
            """)
    void shouldNotFindPattern(final String input, final String pattern) {
        // when
        final SearchResult result = uut.findPattern(input, pattern, IGNORE_PROGRESS);

        // then
        assertThat(result.position()).isEqualTo(-1);
        assertThat(result.typos()).isEqualTo(0);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            text,test,0, 1
            xxxtesxxx,test, 3, 1
            xxxxtesttxxxx,testtest, 4, 3
            """)
    void shouldPatternWithTypos(final String input, final String pattern, final int expectedPosition, final int expectedTypos) {
        // when
        final SearchResult result = uut.findPattern(input, pattern, IGNORE_PROGRESS);

        // then
        assertThat(result.position()).isEqualTo(expectedPosition);
        assertThat(result.typos()).isEqualTo(expectedTypos);
    }


    @ParameterizedTest
    @CsvSource(textBlock = """
            ,, 1
            aaaab,b,6
            aaaaa,b,6
            """)
    void shouldSendInformationAboutProgress(final String input, final String pattern, final int expectedSentProgressCount) {
        // given
        final var testProgressValidator = new TestProgressValidator();

        // when
        uut.findPattern(input, pattern, testProgressValidator);

        // then
        assertThat(testProgressValidator.gotProgressTimes).isEqualTo(expectedSentProgressCount);
        assertThat(testProgressValidator.lastProgress).isEqualTo(100);
    }
}