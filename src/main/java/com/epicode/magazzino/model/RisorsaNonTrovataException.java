package com.epicode.magazzino.model;

/** Prodotto od ordine inesistente. */
public class RisorsaNonTrovataException extends MagazzinoException {

    public RisorsaNonTrovataException(String messaggio) {
        super(messaggio);
    }
}
