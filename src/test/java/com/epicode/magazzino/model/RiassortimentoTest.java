package com.epicode.magazzino.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** R2: i confini della quantità e della nota, scritti a mano dalla specifica. */
class RiassortimentoTest {

    @Test
    void daUnoAMillePezziSiAccetta() {
        assertEquals(1, new Riassortimento(1, null).quantita());
        assertEquals(1000, new Riassortimento(1000, null).quantita());
    }

    @Test
    void zeroOMilleUnoSiRifiutano() {
        assertThrows(RichiestaNonValidaException.class, () -> new Riassortimento(0, null));
        assertThrows(RichiestaNonValidaException.class, () -> new Riassortimento(1001, null));
    }

    @Test
    void notaVuotaDiventaAssente() {
        assertNull(new Riassortimento(5, "   ").nota());
    }

    @Test
    void notaDiDuecentoCaratteriSiAccettaDuecentounoNo() {
        String duecento = "x".repeat(200);
        assertEquals(duecento, new Riassortimento(5, duecento).nota());
        assertThrows(RichiestaNonValidaException.class, () -> new Riassortimento(5, duecento + "x"));
    }
}
