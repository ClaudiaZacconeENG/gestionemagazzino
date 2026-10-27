package com.epicode.magazzino.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class MovimentoTest {

    private final Prodotto prodotto = new Prodotto("MOV", "Prodotto", new BigDecimal("1.00"), 0, 0);
    private final LocalDateTime dataOra = LocalDateTime.of(2026, 10, 27, 12, 0);

    @Test
    void caricoAccettaUnPezzoEMillePezzi() {
        assertEquals(1, Movimento.carico(dataOra, prodotto, 1, null).getPezzi());
        assertEquals(1000, Movimento.carico(dataOra, prodotto, 1000, null).getPezzi());
    }

    @Test
    void caricoRifiutaZeroEOltreMillePezzi() {
        assertThrows(RichiestaNonValidaException.class,
                () -> Movimento.carico(dataOra, prodotto, 0, null));
        assertThrows(RichiestaNonValidaException.class,
                () -> Movimento.carico(dataOra, prodotto, 1001, null));
    }

    @Test
    void notaAccettaDuecentoCaratteriERifiutaDuecentouno() {
        String notaValida = "a".repeat(200);
        assertEquals(notaValida, Movimento.carico(dataOra, prodotto, 1, notaValida).getNota());
        assertThrows(RichiestaNonValidaException.class,
                () -> Movimento.carico(dataOra, prodotto, 1, "a".repeat(201)));
    }
}
