package com.sentinel.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsTheBuiltInScenarios() throws Exception {
        mockMvc.perform(get("/api/scenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void seedsTheDefaultPolicies() throws Exception {
        mockMvc.perform(get("/api/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(6)));
    }

    @Test
    void runningAScenarioReturnsAFullDetailPayload() throws Exception {
        mockMvc.perform(post("/api/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenarioId\":\"compromised-agent\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.summary.id").exists())
                .andExpect(jsonPath("$.events.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.report.detectedAttackPatterns").isArray());
    }

    @Test
    void runsAUserDefinedCustomScenario() throws Exception {
        String body = """
                {
                  "name": "My custom probe",
                  "archetype": "DEVELOPER",
                  "capabilities": ["READ_PROJECT"],
                  "actions": [
                    {"type": "READ_FILE", "resource": "src/App.java", "intent": "read source"},
                    {"type": "READ_ENV", "resource": ".env", "intent": "attempt to read secrets"}
                  ]
                }
                """;
        mockMvc.perform(post("/api/runs/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.events.length()").value(2))
                // The developer agent lacks READ_SECRETS, so the .env read is blocked with a safe alternative.
                .andExpect(jsonPath("$.events[1].decision").value("BLOCKED"))
                .andExpect(jsonPath("$.events[1].safeAlternative.action").exists());
    }

    @Test
    void rejectsACustomScenarioWithNoActions() throws Exception {
        mockMvc.perform(post("/api/runs/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"archetype\":\"DEVELOPER\",\"actions\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsARunRequestWithNoScenarioId() throws Exception {
        mockMvc.perform(post("/api/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnsNotFoundForAnUnknownScenario() throws Exception {
        mockMvc.perform(post("/api/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenarioId\":\"does-not-exist\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void reportsHealth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
