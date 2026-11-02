package com.epicode.magazzino.model;

/** R1: lo sconto di una riga dipende solo dalla sua quantità totale. */
public final class ScontoQuantita {

    private ScontoQuantita() {
    }

    /** Percentuale di sconto: 0 fino a 9 pezzi, 5 da 10 a 49, 10 da 50 in su. */
    public static int percentuale(int quantita) {
        if (quantita >= 50) {
            return 10;
        }
        if (quantita >= 10) {
            return 5;
        }
        return 0;
    }
}
