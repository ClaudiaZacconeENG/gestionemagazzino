package com.epicode.magazzino.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epicode.magazzino.model.Ordine;
import com.epicode.magazzino.model.Movimento;
import com.epicode.magazzino.model.Prodotto;
import com.epicode.magazzino.model.RichiestaNonValidaException;
import com.epicode.magazzino.model.RisorsaNonTrovataException;
import com.epicode.magazzino.model.ScortaInsufficienteException;
import com.epicode.magazzino.model.TipoMovimento;
import com.epicode.magazzino.repository.MovimentoRepository;
import com.epicode.magazzino.repository.ProdottoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Test di integrazione: service, repository e database H2 veri. */
@SpringBootTest
@Transactional
class OrdineServiceTest {

    @Autowired
    private OrdineService ordini;

    @Autowired
    private ProdottoRepository prodotti;

    @Autowired
    private MovimentoRepository movimenti;

    @Autowired
    private MovimentiService servizioMovimenti;

    @BeforeEach
    void prodottiDiTest() {
        prodotti.save(new Prodotto("T-VITI", "Viti di test", new BigDecimal("2.50"), 20, 5));
        prodotti.save(new Prodotto("T-MART", "Martello di test", new BigDecimal("15.00"), 3, 1));
    }

    @Test
    void creaUnOrdineEScalaLeScorte() {
        Ordine ordine = ordini.crea(new NuovoOrdine("Ferramenta Bianchi", List.of(
                new NuovoOrdine.Riga("T-VITI", 4),
                new NuovoOrdine.Riga("T-MART", 1))));

        assertNotNull(ordine.getId());
        assertEquals(new BigDecimal("25.00"), ordine.totale());
        assertEquals(16, prodotti.findByCodice("T-VITI").orElseThrow().getDisponibili());
        assertEquals(2, prodotti.findByCodice("T-MART").orElseThrow().getDisponibili());
    }

    @Test
    void prodottoInesistente() {
        NuovoOrdine richiesta = new NuovoOrdine("Bianchi", List.of(new NuovoOrdine.Riga("NON-ESISTE", 1)));
        assertThrows(RisorsaNonTrovataException.class, () -> ordini.crea(richiesta));
    }

    @Test
    void scortaInsufficiente() {
        NuovoOrdine richiesta = new NuovoOrdine("Bianchi", List.of(new NuovoOrdine.Riga("T-MART", 4)));
        assertThrows(ScortaInsufficienteException.class, () -> ordini.crea(richiesta));
    }

    @Test
    void ordineSenzaRighe() {
        NuovoOrdine richiesta = new NuovoOrdine("Bianchi", List.of());
        assertThrows(RichiestaNonValidaException.class, () -> ordini.crea(richiesta));
    }

    @Test
    void creaScarichiEAnnullaConResiPerOgniRiga() {
        Ordine ordine = ordini.crea(new NuovoOrdine("Bianchi", List.of(
                new NuovoOrdine.Riga("T-VITI", 2),
                new NuovoOrdine.Riga("T-MART", 1))));

        List<Movimento> scarichi = movimenti.findAllByOrderByDataOraDescIdDesc().stream()
                .filter(m -> ordine.getId().equals(m.getNumeroOrdine()))
                .toList();
        assertEquals(2, scarichi.size());
        assertEquals(List.of(TipoMovimento.SCARICO, TipoMovimento.SCARICO),
                scarichi.stream().map(Movimento::getTipo).toList());
        assertEquals(ordine.getId(), scarichi.getFirst().getNumeroOrdine());
        assertNotNull(scarichi.getFirst().getDataOra());

        ordini.annulla(ordine.getId());

        List<Movimento> registrati = movimenti.findAllByOrderByDataOraDescIdDesc().stream()
                .filter(m -> ordine.getId().equals(m.getNumeroOrdine()))
                .toList();
        assertEquals(4, registrati.size());
        assertEquals(2, registrati.stream().filter(m -> m.getTipo() == TipoMovimento.RESO).count());
        assertEquals(20, prodotti.findByCodice("T-VITI").orElseThrow().getDisponibili());
        assertEquals(3, prodotti.findByCodice("T-MART").orElseThrow().getDisponibili());
    }

    @Test
    void elencoMovimentiOrdinaDalPiuRecente() {
        Prodotto prodotto = prodotti.findByCodice("T-VITI").orElseThrow();
        LocalDateTime dataVecchia = LocalDateTime.of(2026, 1, 1, 10, 0);
        Movimento vecchio = movimenti.save(Movimento.carico(dataVecchia, prodotto, 1, null));
        Movimento recente = movimenti.save(Movimento.carico(dataVecchia.plusDays(1), prodotto, 1, null));

        List<Long> idInOrdine = servizioMovimenti.elenco().stream()
                .filter(m -> "T-VITI".equals(m.getProdotto().getCodice()))
                .map(Movimento::getId)
                .toList();

        assertEquals(List.of(recente.getId(), vecchio.getId()), idInOrdine);
    }
}
