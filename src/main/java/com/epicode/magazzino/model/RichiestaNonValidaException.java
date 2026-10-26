package com.epicode.magazzino.model;

/** Dati in ingresso non accettabili: cliente vuoto, quantità non positiva, ordine senza righe. */
public class RichiestaNonValidaException extends MagazzinoException {

    public RichiestaNonValidaException(String messaggio) {
        super(messaggio);
    }
}
