package com.vnsearch.analytics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.security.admin-api-key=khoa-kiem-thu-du-dai-32-ky-tu-000",
        "app.security.rate-limit.enabled=false"
})

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
class AnalyticsAuthorizationTest {

    private static final String KEY_HEADER = "X-API-Key";
    private static final String VALID_KEY = "khoa-kiem-thu-du-dai-32-ky-tu-000";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void withoutApiKeyMetricsAreNotReadable() throws Exception {
        mockMvc.perform(get("/api/admin/analytics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongApiKeyIsAlsoRejected() throws Exception {
        mockMvc.perform(get("/api/admin/analytics").header(KEY_HEADER, "khoa-sai-hoan-toan"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validKeyReturnsItsOwnMetricsBlock() throws Exception {
        mockMvc.perform(get("/api/admin/analytics").header(KEY_HEADER, VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedAt").exists())
                .andExpect(jsonPath("$.traffic.searches").exists());
    }

    @Test
    void deadDependencyStillReturns200WithEmptyBlock() throws Exception {
        mockMvc.perform(get("/api/admin/analytics").header(KEY_HEADER, VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.index.scorer").value("khong-ro"))
                .andExpect(jsonPath("$.accounts.total").value(0))
                .andExpect(jsonPath("$.crawl").doesNotExist());
    }

    @Test
    void resettingMetricsAlsoRequiresApiKey() throws Exception {
        mockMvc.perform(post("/api/admin/analytics/reset"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/admin/analytics/reset").header(KEY_HEADER, VALID_KEY))
                .andExpect(status().isNoContent());
    }

     */
    @Test
    void anyoneCanPostUsageEvents() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"search","sessionId":"phien-1","query":"ha noi",
                                 "resultCount":12,"tookMs":18}"""))
                .andExpect(status().isNoContent());
    }

    @Test
    void malformedEventIsRejectedNotSilentlyIgnored() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"khong-ton-tai\",\"sessionId\":\"phien-1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void overlongQueryIsRejectedAtTheApplicationBoundary() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"search\",\"sessionId\":\"phien-1\",\"query\":\""
                                + "x".repeat(500) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void topParameterIsCappedAtTheUpperBound() throws Exception {
        mockMvc.perform(get("/api/admin/analytics").param("top", "1000000")
                        .header(KEY_HEADER, VALID_KEY))
                .andExpect(status().isBadRequest());
    }
}
