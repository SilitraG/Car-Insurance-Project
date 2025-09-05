package com.example.carins;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClaimHistoryApiTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Test
    void historyReturnsArray() throws Exception {
        mvc.perform(get("/api/cars/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void historyWhenCarMissing() throws Exception {
        mvc.perform(get("/api/cars/999/history"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postClaimAndHistoryIncludesIt() throws Exception {
        Map<String,Object> body = Map.of(
                "carId", 1L,
                "claimDate", LocalDate.parse("2025-03-15"),
                "description", "Rear bumper",
                "amount", new BigDecimal("1200.50")
        );

        mvc.perform(post("/api/cars/1/claims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
                .andExpect(status().is2xxSuccessful());

        mvc.perform(get("/api/cars/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
