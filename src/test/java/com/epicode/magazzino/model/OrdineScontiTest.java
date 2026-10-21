package com.epicode.magazzino.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** R2-R5 sugli importi: ogni valore atteso è calcolato a mano. */
class OrdineScontiTest {

    private static final LocalDateTime ADESSO = LocalDateTime.of(2026, 10, 26, 9, 0);

    private Ordine ordineCon(String prezzo, int quantita) {
        Prodotto prodotto = new Prodotto("P", "Prodotto", new BigDecimal(prezzo), 1000, 0);
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(prodotto, quantita);
        return ordine;
    }

    @Test
    void subtotaleScontatoEArrotondatoHalfUp() {
        // 4,50 x 13 = 58,50; meno il 5% = 55,575 -> 55,58
        Ordine ordine = ordineCon("4.50", 13);
        assertEquals(new BigDecimal("55.58"), ordine.getRighe().get(0).subtotale());
    }

    @Test
    void conCinquantaPezziScontoDelDieciPerCento() {
        // 6,90 x 50 = 345,00; meno il 10% = 310,50
        Ordine ordine = ordineCon("6.90", 50);
        assertEquals(new BigDecimal("310.50"), ordine.totale());
    }

    @Test
    void loStessoProdottoAggiuntoDueVolteSommaLaQuantitaPerLoSconto() {
        Prodotto viti = new Prodotto("VIT", "Viti", new BigDecimal("6.90"), 100, 0);
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(viti, 5);
        ordine.aggiungiRiga(viti, 5);
        // 10 pezzi: 69,00 meno il 5% = 65,55
        assertEquals(5, ordine.getRighe().get(0).percentualeSconto());
        assertEquals(new BigDecimal("65.55"), ordine.totale());
    }

    @Test
    void sottoCentocinquantaEuroLaSpedizioneCosta() {
        Ordine ordine = ordineCon("149.99", 1);
        assertEquals(new BigDecimal("9.90"), ordine.speseSpedizione());
        assertEquals(new BigDecimal("159.89"), ordine.totaleDaPagare());
    }

    @Test
    void daCentocinquantaEuroLaSpedizioneEGratuita() {
        Ordine ordine = ordineCon("150.00", 1);
        assertEquals(new BigDecimal("0.00"), ordine.speseSpedizione());
        assertEquals(new BigDecimal("150.00"), ordine.totaleDaPagare());
    }

    @Test
    void laSogliaSiConfrontaConLaMerceGiaScontata() {
        // 15,00 x 10 = 150,00; meno il 5% = 142,50: sotto soglia, la spedizione si paga
        Ordine ordine = ordineCon("15.00", 10);
        assertEquals(new BigDecimal("142.50"), ordine.totale());
        assertEquals(new BigDecimal("9.90"), ordine.speseSpedizione());
    }
}
