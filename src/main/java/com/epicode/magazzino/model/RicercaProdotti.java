package com.epicode.magazzino.model;

public record RicercaProdotti(String testo) {

    public RicercaProdotti {
        if (testo == null || testo.codePoints()
                .filter(carattere -> !Character.isWhitespace(carattere) && !Character.isSpaceChar(carattere))
                .count() < 2) {
            throw new RichiestaNonValidaException("Scrivi almeno 2 caratteri da cercare");
        }
        testo = testo.strip();
    }
}