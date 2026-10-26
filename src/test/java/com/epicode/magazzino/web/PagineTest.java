package com.epicode.magazzino.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Le pagine principali si aprono e il form crea davvero un ordine. */
@SpringBootTest
@AutoConfigureMockMvc
class PagineTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void elencoProdotti() throws Exception {
        mvc.perform(get("/prodotti"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Trapano a batteria 18V")));
    }

    @Test
    void ilFormCreaUnOrdineEPortaAlDettaglio() throws Exception {
        mvc.perform(post("/ordini")
                        .param("cliente", "Ferramenta Bianchi")
                        .param("righe[0].codice", "GUA-L")
                        .param("righe[0].quantita", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/ordini/*"));
    }

    @Test
    void ilFormMostraLErroreSeLaScortaNonBasta() throws Exception {
        mvc.perform(post("/ordini")
                        .param("cliente", "Ferramenta Bianchi")
                        .param("righe[0].codice", "NAS-50")
                        .param("righe[0].quantita", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Scorta insufficiente per NAS-50")));
    }

    @Test
    void riassortimentoNonValidoMostraLErrore() throws Exception {
        mvc.perform(post("/prodotti/GUA-L/riassortimento").param("quantita", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La quantità da riassortire va da 1 a 1000")));
    }

    @Test
    void riassortimentoValidoPortaAiMovimenti() throws Exception {
        mvc.perform(post("/prodotti/MET-5/riassortimento").param("quantita", "10").param("nota", "Arrivo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/movimenti"));
        mvc.perform(get("/movimenti"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Arrivo")));
    }

    @Test
    void ordineInesistente() throws Exception {
        mvc.perform(get("/ordini/9999"))
                .andExpect(status().isNotFound());
    }
}
