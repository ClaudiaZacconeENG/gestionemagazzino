package com.epicode.magazzino.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProdottoTest {

    private Prodotto viti() {
        return new Prodotto("VIT", "Viti", new BigDecimal("6.90"), 10, 3);
    }

    @Test
    void prelevaTogliePezziDallaGiacenza() {
        Prodotto viti = viti();
        viti.preleva(4);
        assertEquals(6, viti.getDisponibili());
    }

    @Test
    void sipuoPrelevareTuttaLaGiacenza() {
        Prodotto viti = viti();
        viti.preleva(10);
        assertEquals(0, viti.getDisponibili());
    }

    @Test
    void unPezzoInPiuDellaGiacenzaVieneRifiutatoSenzaToccareNulla() {
        Prodotto viti = viti();
        assertThrows(ScortaInsufficienteException.class, () -> viti.preleva(11));
        assertEquals(10, viti.getDisponibili());
    }

    @Test
    void quantitaZeroONegativaNonValida() {
        Prodotto viti = viti();
        assertThrows(RichiestaNonValidaException.class, () -> viti.preleva(0));
        assertThrows(RichiestaNonValidaException.class, () -> viti.riassortisci(-1));
    }

    @Test
    void prezzoNegativoRifiutato() {
        assertThrows(RichiestaNonValidaException.class,
                () -> new Prodotto("X", "X", new BigDecimal("-1"), 1, 0));
    }
}
