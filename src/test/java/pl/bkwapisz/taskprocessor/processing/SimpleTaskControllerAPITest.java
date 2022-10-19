package pl.bkwapisz.taskprocessor.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.bkwapisz.taskprocessor.TaskProcessorAbstractIntegrationTest;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
class SimpleTaskControllerAPITest extends TaskProcessorAbstractIntegrationTest {

    record CreateTaskInput(String input, String pattern) {
        static CreateTaskInput createNotEmpty() {
            return new CreateTaskInput("input", "pattern");
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateNewTaskAndReturnId() throws Exception {
        callCreateTask(CreateTaskInput.createNotEmpty())
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
        callCreateTask(new CreateTaskInput(inputParam, patternParam))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404ForNotExistingTaskId() throws Exception {
        mockMvc.perform(get("/task/not-existing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnStatusOfAlreadyCreatedTask() throws Exception {
        // given
        final var response = callCreateTask(CreateTaskInput.createNotEmpty())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        log.info("got response: {}", response.getContentAsString());

        final var taskId = JsonPath.read(response.getContentAsString(), "$.id");

        // then
        mockMvc.perform(get("/task/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(taskId)))
                .andExpect(jsonPath("$.status", equalTo("new")))
                .andExpect(jsonPath("$.progress", equalTo("0%")));
    }

    @NotNull
    private ResultActions callCreateTask(final CreateTaskInput input) throws Exception {
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