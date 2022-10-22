package pl.bkwapisz.taskprocessor.processing;

import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.bkwapisz.taskprocessor.TaskProcessorAbstractIntegrationTest;

import java.time.Duration;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Slf4j
@AutoConfigureMockMvc
public class TaskProcessingFlowITTest extends TaskProcessorAbstractIntegrationTest {

    private static final Duration TEST_WAIT_TIMEOUT = Duration.ofSeconds(30);

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private TextSearchPatternEngine textSearchPatternEngine;

    @SpyBean
    private TaskStatusService taskStatusService;

    @Test
    public void shouldFinishNewCratedTask() {
        // given
        final var task = new TaskInputForTestRequest("aaaaasdddd", "asdf");

        // when
        final var taskId = createTask(task);

        // then
        await()
                .pollDelay(Duration.ofSeconds(1))
                .atMost(TEST_WAIT_TIMEOUT)
                .untilAsserted(() -> getTaskAndAssertIsFinished(taskId));
    }

    @Test
    public void shouldMarkTaskAsFailedInCaseOfError() {
        // given
        final var task = new TaskInputForTestRequest("errorInput1", "asdf");
        Mockito.reset(textSearchPatternEngine);
        doThrow(new IllegalStateException("Exception for test purpose"))
                .when(textSearchPatternEngine).findPattern(anyString(), anyString(), any());

        // when
        final var taskId = createTask(task);

        // then
        await()
                .atMost(TEST_WAIT_TIMEOUT)
                .untilAsserted(() ->
                        mockMvc.perform(get("/task/{id}", taskId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", equalTo(taskId)))
                                .andExpect(jsonPath("$.status", equalTo("failed")))
                );
    }

    @Test
    public void shouldRetryWhenCouldNotSaveAsFailure() {
        // given
        final var task = new TaskInputForTestRequest("errorInput2", "asdf");
        doThrow(new IllegalStateException("Exception for test purpose - patternSearchEngine"))
                .doCallRealMethod()
                .when(textSearchPatternEngine).findPattern(anyString(), anyString(), any());
        doThrow(new IllegalStateException("Exception for test purpose - taskStatusService"))
                .when(taskStatusService).markTaskAsFailed(anyString());

        // when
        final var taskId = createTask(task);

        // then
        await()
                .pollDelay(Duration.ofSeconds(1))
                .atMost(TEST_WAIT_TIMEOUT)
                .untilAsserted(() -> getTaskAndAssertIsFinished(taskId));
        verify(textSearchPatternEngine, times(2)).findPattern(anyString(), anyString(), any());
    }

    @SneakyThrows
    private Object createTask(final TaskInputForTestRequest task) {
        final var response = mockMvc.perform(
                        post("/task")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJson(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", not(emptyOrNullString())))
                .andReturn()
                .getResponse();
        log.info("got response: {}", response.getContentAsString());
        return JsonPath.read(response.getContentAsString(), "$.id");
    }

    @NotNull
    private ResultActions getTaskAndAssertIsFinished(final Object taskId) throws Exception {
        return mockMvc.perform(get("/task/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(taskId)))
                .andExpect(jsonPath("$.status", equalTo("finished")))
                .andExpect(jsonPath("$.progress", equalTo("100%")))
                .andExpect(jsonPath("$.result.position", not(empty())))
                .andExpect(jsonPath("$.result.typos", not(empty())));
    }

}
