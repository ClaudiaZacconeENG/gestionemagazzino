package com.epicode.magazzino.service;

import java.util.List;

/** La richiesta di un nuovo ordine, già ripulita dal form. */
public record NuovoOrdine(String cliente, List<Riga> righe) {

    public record Riga(String codiceProdotto, int quantita) {
    }
}
