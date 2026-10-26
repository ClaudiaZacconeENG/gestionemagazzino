package com.epicode.magazzino.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrdineTest {

    private static final LocalDateTime ADESSO = LocalDateTime.of(2026, 10, 26, 9, 0);

    private Prodotto viti;
    private Prodotto trapano;

    @BeforeEach
    void catalogo() {
        viti = new Prodotto("VIT", "Viti", new BigDecimal("6.90"), 100, 10);
        trapano = new Prodotto("TRA", "Trapano", new BigDecimal("89.00"), 5, 1);
    }

    @Test
    void ilTotaleESommaDeiSubtotali() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(viti, 10);     // 65,55 dopo lo sconto del 5%
        ordine.aggiungiRiga(trapano, 2);   // 178,00
        assertEquals(new BigDecimal("243.55"), ordine.totaleMerce());
        assertEquals(new BigDecimal("243.55"), ordine.totaleDaPagare());
    }

    @Test
    void ogniRigaPrelevaDalMagazzino() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(trapano, 2);
        assertEquals(3, trapano.getDisponibili());
    }

    @Test
    void loStessoProdottoDueVolteFiniscenellaStessaRiga() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(viti, 3);
        ordine.aggiungiRiga(viti, 2);
        assertEquals(1, ordine.getRighe().size());
        assertEquals(5, ordine.getRighe().get(0).getQuantita());
    }

    @Test
    void loScontoDipendeDalleSoglieDiQuantita() {
        assertEquals(0, rigaConQuantita(9).getPercentualeSconto());
        assertEquals(5, rigaConQuantita(10).getPercentualeSconto());
        assertEquals(5, rigaConQuantita(49).getPercentualeSconto());
        assertEquals(10, rigaConQuantita(50).getPercentualeSconto());
    }

    @Test
    void riaggiungereLoStessoProdottoApplicaLoScontoSullaQuantitaAggregata() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(viti, 5);
        ordine.aggiungiRiga(viti, 5);

        assertEquals(1, ordine.getRighe().size());
        assertEquals(10, ordine.getRighe().get(0).getQuantita());
        assertEquals(5, ordine.getRighe().get(0).getPercentualeSconto());
        assertEquals(new BigDecimal("65.55"), ordine.getRighe().get(0).subtotale());
    }

    @Test
    void subtotaleScontatoArrotondaAlCentesimoConHalfUp() {
        Prodotto economico = new Prodotto("ECO", "Economico", new BigDecimal("0.05"), 10, 0);
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(economico, 10);

        assertEquals(new BigDecimal("0.48"), ordine.getRighe().get(0).subtotale());
    }

    @Test
    void spedizioneGratuitaDa150EuroDiMerceScontata() {
        Ordine sottoSoglia = ordineConProdottoDa("149.99");
        Ordine sullaSoglia = ordineConProdottoDa("150.00");

        assertEquals(new BigDecimal("9.90"), sottoSoglia.speseSpedizione());
        assertEquals(new BigDecimal("159.89"), sottoSoglia.totaleDaPagare());
        assertEquals(new BigDecimal("0.00"), sullaSoglia.speseSpedizione());
        assertEquals(new BigDecimal("150.00"), sullaSoglia.totaleDaPagare());
    }

    @Test
    void ilPrezzoDellaRigaRestaQuelloDelMomentoDellOrdine() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(viti, 1);
        assertEquals(new BigDecimal("6.90"), ordine.getRighe().get(0).getPrezzoUnitario());
    }

    @Test
    void annullareRestituisceIPezzi() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(trapano, 2);
        ordine.annulla();
        assertEquals(StatoOrdine.ANNULLATO, ordine.getStato());
        assertEquals(5, trapano.getDisponibili());
    }

    @Test
    void unOrdineSpeditoNonSiAnnulla() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(trapano, 1);
        ordine.spedisci();
        assertThrows(OperazioneNonConsentitaException.class, ordine::annulla);
        assertEquals(4, trapano.getDisponibili());
    }

    @Test
    void unOrdineAnnullatoNonSiAnnullaDueVolte() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(trapano, 1);
        ordine.annulla();
        assertThrows(OperazioneNonConsentitaException.class, ordine::annulla);
        assertEquals(5, trapano.getDisponibili());
    }

    @Test
    void nonSiAggiungonoRigheAUnOrdineSpedito() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(viti, 1);
        ordine.spedisci();
        assertThrows(OperazioneNonConsentitaException.class, () -> ordine.aggiungiRiga(viti, 1));
    }

    @Test
    void clienteObbligatorio() {
        assertThrows(RichiestaNonValidaException.class, () -> new Ordine("  ", ADESSO));
    }

    private RigaOrdine rigaConQuantita(int quantita) {
        Prodotto prodotto = new Prodotto("Q" + quantita, "Prodotto test", new BigDecimal("1.00"), 100, 0);
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(prodotto, quantita);
        return ordine.getRighe().get(0);
    }

    private Ordine ordineConProdottoDa(String prezzo) {
        Prodotto prodotto = new Prodotto("P" + prezzo, "Prodotto test", new BigDecimal(prezzo), 1, 0);
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(prodotto, 1);
        return ordine;
    }
}
