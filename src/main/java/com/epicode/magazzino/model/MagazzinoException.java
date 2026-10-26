package com.epicode.magazzino.model;

/** Errore di dominio: il messaggio è pensato per essere mostrato all'utente. */
public abstract class MagazzinoException extends RuntimeException {

    protected MagazzinoException(String messaggio) {
        super(messaggio);
    }
}
