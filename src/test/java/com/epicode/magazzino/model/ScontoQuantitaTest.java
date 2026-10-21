package com.epicode.magazzino.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** R1: i confini degli scaglioni, scritti a mano dalla specifica. */
class ScontoQuantitaTest {

    @Test
    void finoANovePezziNessunoSconto() {
        assertEquals(0, ScontoQuantita.percentuale(1));
        assertEquals(0, ScontoQuantita.percentuale(9));
    }

    @Test
    void daDieciAQuarantanovePezziScontoCinquePerCento() {
        assertEquals(5, ScontoQuantita.percentuale(10));
        assertEquals(5, ScontoQuantita.percentuale(49));
    }

    @Test
    void daCinquantaPezziScontoDieciPerCento() {
        assertEquals(10, ScontoQuantita.percentuale(50));
        assertEquals(10, ScontoQuantita.percentuale(500));
    }
}
