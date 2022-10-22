package pl.bkwapisz.taskprocessor.processing;

import com.jayway.jsonpath.JsonPath;
import groovy.time.TimeDuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.bkwapisz.taskprocessor.TaskProcessorAbstractIntegrationTest;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(properties = {"taskProcessor.rabbitmq.taskProcessorEnabled=false"})
class TaskControllerAPIITTest extends TaskProcessorAbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private TaskStatusService taskStatusService;

    @Test
    void shouldCreateNewTaskAndReturnId() throws Exception {
        callCreateTask(TaskInputForTestRequest.createNotEmpty())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", not(empty())));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            '', pattern
            ,pattern
            input,''
            input,
            '',''
            ,
            ,''
            '',
            """)
    void shouldReturnBadRequestForIncompleteParametersSet(final String inputParam, final String patternParam) throws Exception {
        callCreateTask(new TaskInputForTestRequest(inputParam, patternParam))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404ForNotExistingTaskId() throws Exception {
        mockMvc.perform(get("/task/not-existing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnStatusOfAlreadyCreatedTask() throws Exception {
        // when
        final var response = callCreateTask(TaskInputForTestRequest.createNotEmpty())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        log.info("create task response: {}", response.getContentAsString());
        final var taskId = JsonPath.read(response.getContentAsString(), "$.id");

        // then
        mockMvc.perform(get("/task/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(taskId)))
                .andExpect(jsonPath("$.status", equalTo("new")))
                .andExpect(jsonPath("$.progress", equalTo("0%")));
    }

    @Test
    void shouldCacheGetTaskEndpoint() throws Exception {
        // given
        reset(taskStatusService);
        final var response = callCreateTask(TaskInputForTestRequest.createNotEmpty())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        log.info("create task response: {}", response.getContentAsString());
        final String taskId = JsonPath.read(response.getContentAsString(), "$.id");

        // when
        mockMvc.perform(get("/task/{id}", taskId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/task/{id}", taskId))
                .andExpect(status().isOk());

        // then
        verify(taskStatusService, times(1)).getTaskStatusOpt(eq(taskId));
    }

    @Test
    void shouldCacheGetAllTasksEndpoint() throws Exception {
        // when
        mockMvc.perform(get("/task"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/task"))
                .andExpect(status().isOk());

        // then
        verify(taskStatusService, times(1)).getAllTaskStatuses();
    }

    @NotNull
    private ResultActions callCreateTask(final TaskInputForTestRequest input) throws Exception {
        return mockMvc.perform(
                post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(input)));
    }

    @SneakyThrows
    public String asJson(final Object obj) {
        return objectMapper.writeValueAsString(obj);
    }
}