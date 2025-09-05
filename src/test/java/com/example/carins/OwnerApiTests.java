package com.example.carins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OwnerApiTests {

    private static final String OWNERS = "/api/owners";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    private String asJson(Object o) throws Exception {
        return om.writeValueAsString(o);
    }

    private JsonNode parse(MvcResult r) throws Exception {
        return om.readTree(r.getResponse().getContentAsString());
    }

    private Map<String, Object> dto(String name, String email) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("email", email);
        return m;
    }

    @Test
    void createOkAndLocationHeader() throws Exception {
        MvcResult res = mvc.perform(post(OWNERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(dto("Ion Test", "ion.test@example.com"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(OWNERS + "/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Ion Test"))
                .andExpect(jsonPath("$.email").value("ion.test@example.com"))
                .andReturn();

        long id = parse(res).get("id").asLong();

        mvc.perform(get(OWNERS + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Ion Test"))
                .andExpect(jsonPath("$.email").value("ion.test@example.com"));
    }

    @Test
    void createValidationErrors() throws Exception {
        // Empty name + invalid email
        mvc.perform(post(OWNERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(dto("   ", "not-an-email"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void createConflictOnDuplicateEmail() throws Exception {
        mvc.perform(post(OWNERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(dto("A", "dup@example.com"))))
                .andExpect(status().isCreated());

        mvc.perform(post(OWNERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(dto("B", "dup@example.com"))))
                .andExpect(status().isConflict());
    }

    @Test
    void updateConflictOnDuplicateEmail() throws Exception {
        // Owner A
        long idA = parse(mvc.perform(post(OWNERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(dto("A", "a@ex.com"))))
                .andExpect(status().isCreated())
                .andReturn()).get("id").asLong();
        // Owner B
        long idB = parse(mvc.perform(post(OWNERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(dto("B", "b@ex.com"))))
                .andExpect(status().isCreated())
                .andReturn()).get("id").asLong();

        // Try cu put A email to B
        mvc.perform(put(OWNERS + "/" + idB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(dto("B", "a@ex.com"))))
                .andExpect(status().isConflict());

        // B remains intact
        mvc.perform(get(OWNERS + "/" + idB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("b@ex.com"));
    }

    @Test
    void putNotFound() throws Exception {
        mvc.perform(put(OWNERS + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(dto("X", "x@ex.com"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByIdNotFound() throws Exception {
        mvc.perform(get(OWNERS + "/9999"))
                .andExpect(status().isNotFound());
    }
}
