package com.epicode.magazzino.model;

/** Si chiede di prelevare più pezzi di quelli disponibili. */
public class ScortaInsufficienteException extends MagazzinoException {

    public ScortaInsufficienteException(String codice, int richiesti, int disponibili) {
        super("Scorta insufficiente per " + codice + ": richiesti " + richiesti
                + ", disponibili " + disponibili);
    }
}
