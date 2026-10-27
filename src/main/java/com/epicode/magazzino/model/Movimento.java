package com.epicode.magazzino.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimenti")
public class Movimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataOra;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Prodotto prodotto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimento tipo;

    @Column(nullable = false)
    private int pezzi;

    @Column(length = 200)
    private String nota;

    private Long numeroOrdine;

    protected Movimento() {
    }

    private Movimento(LocalDateTime dataOra, Prodotto prodotto, TipoMovimento tipo, int pezzi,
                      String nota, Long numeroOrdine) {
        this.dataOra = dataOra;
        this.prodotto = prodotto;
        this.tipo = tipo;
        this.pezzi = pezzi;
        this.nota = nota;
        this.numeroOrdine = numeroOrdine;
    }

    public static Movimento carico(LocalDateTime dataOra, Prodotto prodotto, int pezzi, String nota) {
        if (pezzi < 1 || pezzi > 1000) {
            throw new RichiestaNonValidaException("I pezzi arrivati devono essere tra 1 e 1000");
        }
        if (nota != null && nota.length() > 200) {
            throw new RichiestaNonValidaException("La nota non può superare 200 caratteri");
        }
        return new Movimento(dataOra, prodotto, TipoMovimento.CARICO, pezzi, nota, null);
    }

    public static Movimento scarico(LocalDateTime dataOra, Prodotto prodotto, int pezzi, Long numeroOrdine) {
        return movimentoOrdine(dataOra, prodotto, TipoMovimento.SCARICO, pezzi, numeroOrdine);
    }

    public static Movimento reso(LocalDateTime dataOra, Prodotto prodotto, int pezzi, Long numeroOrdine) {
        return movimentoOrdine(dataOra, prodotto, TipoMovimento.RESO, pezzi, numeroOrdine);
    }

    private static Movimento movimentoOrdine(LocalDateTime dataOra, Prodotto prodotto,
                                               TipoMovimento tipo, int pezzi, Long numeroOrdine) {
        if (pezzi <= 0 || numeroOrdine == null) {
            throw new RichiestaNonValidaException("Il movimento d'ordine non è valido");
        }
        return new Movimento(dataOra, prodotto, tipo, pezzi, null, numeroOrdine);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public Prodotto getProdotto() {
        return prodotto;
    }

    public TipoMovimento getTipo() {
        return tipo;
    }

    public int getPezzi() {
        return pezzi;
    }

    public String getNota() {
        return nota;
    }

    public Long getNumeroOrdine() {
        return numeroOrdine;
    }
}
