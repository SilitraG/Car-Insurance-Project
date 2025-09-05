package com.example.carins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InsuranceApiTests {

    private static final String BASE = "/api/insurances";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    private String json(Map<String,Object> m) throws Exception {
        return om.writeValueAsString(m);
    }

    private Map<String,Object> dto(Long carId, String provider, String startDate, String endDate) {
        Map<String,Object> m = new HashMap<String,Object>();

        m.put("carId", carId);
        m.put("provider", provider);
        m.put("startDate", startDate != null ? LocalDate.parse(startDate) : null);
        m.put("endDate", endDate != null ? LocalDate.parse(endDate) : null);

        return m;
    }

    // POST CASES
    @Test void postOk() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(json(dto(1L, "Allianz", "2025-01-01", "2025-12-31"))))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.carId").value(1))
                .andExpect(jsonPath("$.provider").value("Allianz"));
    }

    @Test void postCarIdNotFound() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(json(dto(999L, "Allianz", "2025-01-01", "2025-12-31"))))
                .andExpect(status().isNotFound());
    }

    @Test void postStartDateIsNull() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(json(dto(1L, "Allianz", null, "2025-12-31"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Start date")));
    }

    @Test void postEndDateIsNull() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(json(dto(1L, "Allianz", "2025-01-01", null))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("End date")));
    }

    @Test void postStartDateAfterEndDate() throws Exception {
        Map<String,Object> m = dto(1L, "Allianz", "2026-12-31", "2025-01-01");
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(m)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Start date must not be after end date")));
    }

    //PUT CASES
    private long createPolicy(Long carId) throws Exception {
        MvcResult r = mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(json(dto(carId, "Allianz", "2025-01-01", "2025-12-31"))))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        JsonNode n = om.readTree(r.getResponse().getContentAsString());

        return n.get("id").asLong();
    }

    @Test void putOk() throws Exception {
        long id = createPolicy(1L);
        Map<String,Object> m = dto(1L, "Groupama", "2025-01-01", "2025-11-30");

        mvc.perform(put(BASE + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(m)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.provider").value("Groupama"))
                .andExpect(jsonPath("$.endDate").value("2025-11-30"));
    }

    @Test void putCarIdNotFound() throws Exception {
        long id = createPolicy(1L);
        Map<String,Object> m = dto(999L, "Allianz", "2025-01-01", "2025-12-31");

        mvc.perform(put(BASE + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(m)))
                .andExpect(status().isNotFound());
    }

    @Test void putStartDateIsNull() throws Exception {
        long id = createPolicy(1L);
        Map<String,Object> m = dto(1L, "Allianz", null, "2025-12-31");

        mvc.perform(put(BASE + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(m)))
                .andExpect(status().isBadRequest());
    }

    @Test void putEndDateIsNull() throws Exception {
        long id = createPolicy(1L);
        Map<String,Object> m = dto(1L, "Allianz", "2025-01-01", null);

        mvc.perform(put(BASE + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(m)))
                .andExpect(status().isBadRequest());
    }

    @Test void putStartDateAfterEndDate() throws Exception {
        long id = createPolicy(1L);
        Map<String,Object> m = dto(1L, "Allianz", "2025-12-31", "2025-01-01");

        mvc.perform(put(BASE + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(m)))
                .andExpect(status().isBadRequest());
    }

    // GET BY ID CASES
    @Test void getByIdNotFound() throws Exception {
        mvc.perform(get(BASE + "/20"))
                .andExpect(status().isNotFound());
    }
}
