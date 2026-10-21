package com.epicode.magazzino.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;

/** Un articolo del magazzino con la sua giacenza. */
@Entity
public class Prodotto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codice;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prezzo;

    private int disponibili;

    private int scortaMinima;

    protected Prodotto() {
        // per JPA
    }

    public Prodotto(String codice, String nome, BigDecimal prezzo, int disponibili,
                    int scortaMinima) {
        if (codice == null || codice.isBlank() || nome == null || nome.isBlank()) {
            throw new RichiestaNonValidaException("Codice e nome del prodotto sono obbligatori");
        }
        if (prezzo == null || prezzo.signum() < 0 || disponibili < 0 || scortaMinima < 0) {
            throw new RichiestaNonValidaException("Prezzo, giacenza e scorta minima non possono essere negativi");
        }
        this.codice = codice;
        this.nome = nome;
        this.prezzo = prezzo;
        this.disponibili = disponibili;
        this.scortaMinima = scortaMinima;
    }

    /** Toglie pezzi dalla giacenza. Se non bastano non tocca nulla e lancia l'eccezione. */
    public void preleva(int quantita) {
        if (quantita <= 0) {
            throw new RichiestaNonValidaException("La quantità deve essere maggiore di zero");
        }
        if (quantita > disponibili) {
            throw new ScortaInsufficienteException(codice, quantita, disponibili);
        }
        disponibili -= quantita;
    }

    /** Rimette pezzi in giacenza (arrivo merce o ordine annullato). */
    public void riassortisci(int quantita) {
        if (quantita <= 0) {
            throw new RichiestaNonValidaException("La quantità deve essere maggiore di zero");
        }
        disponibili += quantita;
    }

    public Long getId() {
        return id;
    }

    public String getCodice() {
        return codice;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public int getDisponibili() {
        return disponibili;
    }

    public int getScortaMinima() {
        return scortaMinima;
    }
}
