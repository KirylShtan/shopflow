package org.example.inventoryservice.controller;

import org.example.inventoryservice.dto.StockResponse;
import org.example.inventoryservice.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    @Test
    void create_returns201() throws Exception {
        when(stockService.create(any())).thenReturn(new StockResponse(1L, 7L, 100));

        mockMvc.perform(post("/api/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":7,"quantity":100}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(7))
                .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    void getByProductId_returnsStock() throws Exception {
        when(stockService.getByProductId(7L)).thenReturn(new StockResponse(1L, 7L, 50));

        mockMvc.perform(get("/api/stock/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(50));
    }
}
