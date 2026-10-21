package com.epicode.magazzino.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Un prodotto dentro un ordine, con il prezzo fissato al momento dell'ordine. */
@Entity
public class RigaOrdine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Ordine ordine;

    @ManyToOne(optional = false)
    private Prodotto prodotto;

    private int quantita;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prezzoUnitario;

    protected RigaOrdine() {
        // per JPA
    }

    RigaOrdine(Ordine ordine, Prodotto prodotto, int quantita) {
        this.ordine = ordine;
        this.prodotto = prodotto;
        this.quantita = quantita;
        this.prezzoUnitario = prodotto.getPrezzo();
    }

    void aumenta(int altri) {
        quantita += altri;
    }

    /** Prezzo unitario per quantità, arrotondato al centesimo. */
    public BigDecimal subtotale() {
        return prezzoUnitario.multiply(BigDecimal.valueOf(quantita))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public Prodotto getProdotto() {
        return prodotto;
    }

    public int getQuantita() {
        return quantita;
    }

    public BigDecimal getPrezzoUnitario() {
        return prezzoUnitario;
    }
}
