package com.epicode.magazzino.model;

/**
 * R2: una richiesta di riassortimento valida. Il limite di 1000 pezzi vale solo
 * per il riassortimento, non per i resi: per questo sta qui e non in Prodotto.
 */
public record Riassortimento(int quantita, String nota) {

    public static final int MASSIMO_PEZZI = 1000;
    public static final int MASSIMO_NOTA = 200;

    public Riassortimento {
        if (quantita < 1 || quantita > MASSIMO_PEZZI) {
            throw new RichiestaNonValidaException(
                    "La quantità da riassortire va da 1 a " + MASSIMO_PEZZI);
        }
        nota = (nota == null || nota.isBlank()) ? null : nota.strip();
        if (nota != null && nota.length() > MASSIMO_NOTA) {
            throw new RichiestaNonValidaException(
                    "La nota può avere al massimo " + MASSIMO_NOTA + " caratteri");
        }
    }
}
