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
class CarApiTests {

    private static final String CARS   = "/api/cars";
    private static final String OWNERS = "/api/owners";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    private String asJson(Object o) throws Exception { return om.writeValueAsString(o); }
    private JsonNode parse(MvcResult r) throws Exception { return om.readTree(r.getResponse().getContentAsString()); }

    private Map<String, Object> carDto(Long ownerId, String vin, String make, String model, int year) {
        Map<String, Object> m = new HashMap<>();
        m.put("ownerId", ownerId);
        m.put("vin", vin);
        m.put("make", make);
        m.put("model", model);
        m.put("year", year);
        return m;
    }

    private long createOwner(String name, String email) throws Exception {
        MvcResult res = mvc.perform(post(OWNERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(new HashMap<String,Object>() {{
                            put("name", name);
                            put("email", email);
                        }})))
                .andExpect(status().isCreated())
                .andReturn();
        return parse(res).get("id").asLong();
    }

    // Generate a vin with exactly 8 characters
    private String genVin8() {
        String base = ("V" + Long.toHexString(System.nanoTime()).toUpperCase());
        if (base.length() >= 8) return base.substring(base.length() - 8);
        return String.format("%-8s", base).replace(' ', '0');
    }

    @Test
    void createOkAndLocationHeader() throws Exception {
        long ownerId = createOwner("Boby Bob", "Boby.bob@example.com");

        mvc.perform(post(CARS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(carDto(ownerId, "VINAB123", "Audi", "A4", 2016))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(CARS + "/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.ownerId").value(ownerId))
                .andExpect(jsonPath("$.vin").value("VINAB123"))
                .andExpect(jsonPath("$.make").value("Audi"))
                .andExpect(jsonPath("$.model").value("A4"))
                .andExpect(jsonPath("$.year").value(2016));
    }

    @Test
    void createValidationErrorsOnVin() throws Exception {
        long ownerId = createOwner("Sony Son", "sony@test.com");

        ///Vin is empty
        mvc.perform(post(CARS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(carDto(ownerId, "   ", "VW", "Golf", 2020))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.vin").exists());

        ///Wrong length for vin
        mvc.perform(post(CARS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(carDto(ownerId, "1234567", "VW", "Golf", 2020))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.vin").exists());
    }

    @Test
    void createOwnerNotFound() throws Exception {
        mvc.perform(post(CARS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(carDto(999L, "VINZZ999", "VW", "Golf", 2020))))
                .andExpect(status().isNotFound());
    }

    @Test
    void createConflictOnDuplicateVin() throws Exception {
        // owner valid
        MvcResult ownerRes = mvc.perform(post(OWNERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(new HashMap<String,Object>() {{
                            put("name", "Ion ion");
                            put("email", "ion.ion+" + System.nanoTime() + "@example.com");
                        }})))
                .andExpect(status().isCreated())
                .andReturn();
        long ownerId = parse(ownerRes).get("id").asLong();

        // Unique Vin (ex: "V12AB3CD")
        String vinUpper = genVin8();
        // Same Vin but with lowercase
        String vinLower = vinUpper.toLowerCase();

        // Car A
        MvcResult car1 = mvc.perform(post(CARS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(carDto(ownerId, vinUpper, "Audi", "A4", 2016))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(CARS + "/")))
                .andExpect(jsonPath("$.vin").value(vinUpper))
                .andReturn();
        long car1Id = parse(car1).get("id").asLong();

        // Car 2 (Vin conflict)
        mvc.perform(post(CARS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(carDto(ownerId, vinLower, "BMW", "320", 2018))))
                .andExpect(status().isConflict());

        // First Car remain intact
        mvc.perform(get(CARS + "/" + car1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vin").value(vinUpper));
    }

    @Test
    void updateConflictOnDuplicateVin() throws Exception {
        long ownerId = createOwner("Same Vin", "same.vin@example.com");

        // Car A
        long idA = parse(mvc.perform(post(CARS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(carDto(ownerId, "VINAAAAA", "Audi", "A3", 2015))))
                .andExpect(status().isCreated())
                .andReturn()).get("id").asLong();

        // Car B
        long idB = parse(mvc.perform(post(CARS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(carDto(ownerId, "VINB2222", "Audi", "A4", 2016))))
                .andExpect(status().isCreated())
                .andReturn()).get("id").asLong();

        // Update A vin with B vin
        mvc.perform(put(CARS + "/" + idA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(carDto(ownerId, "VINB2222", "Audi", "A4", 2016))))
                .andExpect(status().isConflict());

        // A is intact
        mvc.perform(get(CARS + "/" + idA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vin").value("VINAAAAA"));
    }

    @Test
    void putNotFound() throws Exception {
        long ownerId = createOwner("P Test", "p@test.com");
        mvc.perform(put(CARS + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(carDto(ownerId, "VINNN111", "Skoda", "Octavia", 2019))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByIdNotFound() throws Exception {
        mvc.perform(get(CARS + "/20"))
                .andExpect(status().isNotFound());
    }
}
