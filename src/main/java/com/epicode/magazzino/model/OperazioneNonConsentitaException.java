package com.epicode.magazzino.model;

/** L'operazione non è ammessa nello stato attuale dell'ordine. */
public class OperazioneNonConsentitaException extends MagazzinoException {

    public OperazioneNonConsentitaException(String messaggio) {
        super(messaggio);
    }
}
