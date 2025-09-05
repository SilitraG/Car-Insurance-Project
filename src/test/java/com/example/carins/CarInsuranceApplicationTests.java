package com.example.carins;

import com.example.carins.service.CarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import static org.hamcrest.Matchers.containsString;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class CarInsuranceApplicationTests {

    @Autowired
    CarService service;

    @Autowired
    MockMvc mvc;

    @Test
    void insuranceValidityBasic() {
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2024-06-01")));
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2025-06-01")));
        assertFalse(service.isInsuranceValid(2L, LocalDate.parse("2025-02-01")));
    }

    @Test
    void insuranceValidityBadFormatForDate() throws Exception {
        mvc.perform(get("/api/cars/1/insurance-valid").param("date", "02-01-2025"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid date format")));
    }

    @Test
    void insuranceValidityDateIsOutOffRange() throws Exception {
        mvc.perform(get("/api/cars/1/insurance-valid").param("date", "1897-02-01"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("out of supported range")));
    }

    @Test
    void insuranceValidityCarIdNotFound() throws Exception {
        mvc.perform(get("/api/cars/999/insurance-valid").param("date", "2025-02-01"))
                .andExpect(status().isNotFound());
    }

    @Test
    void insuranceValidityIsOK() throws Exception {
        mvc.perform(get("/api/cars/1/insurance-valid").param("date", "2025-06-01"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"carId\":1")));
    }
}
