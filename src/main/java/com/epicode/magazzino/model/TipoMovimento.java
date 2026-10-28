package com.epicode.magazzino.model;

/** Perché la giacenza è cambiata. */
public enum TipoMovimento {
    /** Merce arrivata in magazzino (riassortimento). */
    CARICO,
    /** Pezzi usciti con un ordine. */
    SCARICO,
    /** Pezzi rientrati per un ordine annullato. */
    RESO
}
