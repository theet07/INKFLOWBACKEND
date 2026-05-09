package com.backend.INKFLOW;

import com.backend.INKFLOW.model.*;
import com.backend.INKFLOW.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private ArtistaService artistaService;

    @MockBean
    private CicatrizacaoService cicatrizacaoService;

    @MockBean
    private AgendamentoService agendamentoService;

    @Test
    public void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/health"))
               .andExpect(status().isOk());
    }

    @Test
    public void testLoginInvalido() throws Exception {
        when(clienteService.getUserByEmail(any())).thenReturn(Optional.empty());
        when(adminService.getByEmail(any())).thenReturn(Optional.empty());
        when(artistaService.getByEmail(any())).thenReturn(Optional.empty());
        
        String json = "{\"email\":\"invalido@gmail.com\", \"password\":\"errada\"}";
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    public void testGetCicatrizacaoAtiva() throws Exception {
        when(cicatrizacaoService.buscarAtiva(1L)).thenReturn(Optional.of(new Cicatrizacao()));

        mockMvc.perform(get("/api/cicatrizacao/ativa/1"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    public void testGetAgendamentos() throws Exception {
        when(agendamentoService.getAllAgendamentos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/agendamentos")) 
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testGetCliente() throws Exception {
        when(clienteService.getById(1L)).thenReturn(Optional.of(new Cliente()));

        mockMvc.perform(get("/api/clientes/1"))
               .andExpect(status().isOk());
    }
}
