package com.epicode.magazzino.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epicode.magazzino.model.Movimento;
import com.epicode.magazzino.model.Prodotto;
import com.epicode.magazzino.repository.MovimentoRepository;
import com.epicode.magazzino.repository.ProdottoRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private ProdottoRepository prodotti;

    @Autowired
    private MovimentoRepository movimenti;

    private String codiceTest;
    private int giacenzaIniziale;

    @BeforeEach
    void prodottoPerRiassortimento() {
        codiceTest = "T-" + UUID.randomUUID();
        giacenzaIniziale = 5;
        prodotti.save(new Prodotto(codiceTest, "Prodotto per riassortimento",
                new BigDecimal("1.00"), giacenzaIniziale, 1));
    }

    @Test
    void elencoProdotti() throws Exception {
        mvc.perform(get("/prodotti"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Trapano a batteria 18V")))
                .andExpect(content().string(containsString("/prodotti/VIT-M6-30/riassortimento")));
    }

    @Test
    void riassortimentoRegistraCaricoConNotaDiDuecentoCaratteri() throws Exception {
        mvc.perform(get("/prodotti/{codice}/riassortimento", codiceTest))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pezzi arrivati")));

        String nota = "a".repeat(200);
        mvc.perform(post("/prodotti/{codice}/riassortimento", codiceTest)
                        .param("pezzi", "1000")
                        .param("nota", nota))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prodotti"));

        assertEquals(giacenzaIniziale + 1000,
                prodotti.findByCodice(codiceTest).orElseThrow().getDisponibili());
        Movimento movimento = movimenti.findAllByOrderByDataOraDescIdDesc().stream()
                .filter(m -> codiceTest.equals(m.getProdotto().getCodice()))
                .findFirst()
                .orElseThrow();
        assertEquals("CARICO", movimento.getTipo().name());
        assertEquals(1000, movimento.getPezzi());
        assertEquals(nota, movimento.getNota());

        mvc.perform(get("/movimenti"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/movimenti\">Movimenti</a>")))
                .andExpect(content().string(containsString("CARICO")))
                .andExpect(content().string(containsString(codiceTest)));
    }

    @Test
    void riassortimentoRifiutaQuantitaNonValideSenzaCambiareGiacenza() throws Exception {
        mvc.perform(post("/prodotti/{codice}/riassortimento", codiceTest)
                        .param("pezzi", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tra 1 e 1000")));
        mvc.perform(post("/prodotti/{codice}/riassortimento", codiceTest)
                        .param("pezzi", "1001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tra 1 e 1000")));
        mvc.perform(post("/prodotti/{codice}/riassortimento", codiceTest)
                        .param("pezzi", "non-intero"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("numero intero")));

        assertEquals(giacenzaIniziale,
                prodotti.findByCodice(codiceTest).orElseThrow().getDisponibili());
    }

    @Test
    void riassortimentoRifiutaNotaDiDuecentounoCaratteriSenzaCambiareGiacenza() throws Exception {
        mvc.perform(post("/prodotti/{codice}/riassortimento", codiceTest)
                        .param("pezzi", "1")
                        .param("nota", "a".repeat(201)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("non può superare 200 caratteri")));

        assertEquals(giacenzaIniziale,
                prodotti.findByCodice(codiceTest).orElseThrow().getDisponibili());
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
    void dettaglioEElencoMostranoScontoESpedizione() throws Exception {
        String destinazione = mvc.perform(post("/ordini")
                        .param("cliente", "Ferramenta Bianchi")
                        .param("righe[0].codice", "GUA-L")
                        .param("righe[0].quantita", "10"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        mvc.perform(get(destinazione))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("5%")))
                .andExpect(content().string(containsString("30,40 €")))
                .andExpect(content().string(containsString("9,90 €")))
                .andExpect(content().string(containsString("40,30 €")));

        mvc.perform(get("/ordini"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Totale da pagare")))
                .andExpect(content().string(containsString("40,30 €")));
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
    void ordineInesistente() throws Exception {
        mvc.perform(get("/ordini/9999"))
                .andExpect(status().isNotFound());
    }
}
