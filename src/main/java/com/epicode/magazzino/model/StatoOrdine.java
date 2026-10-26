package com.epicode.magazzino.model;

/**
 * Ciclo di vita di un ordine.
 * CONFERMATO -> SPEDITO, oppure CONFERMATO -> ANNULLATO. Nessun altro passaggio.
 */
public enum StatoOrdine {
    CONFERMATO,
    SPEDITO,
    ANNULLATO
}
