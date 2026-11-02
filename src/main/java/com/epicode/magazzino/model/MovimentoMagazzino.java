package com.epicode.magazzino.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

/** R4-R5: una riga dello storico di magazzino. Si crea, non si modifica. */
@Entity
public class MovimentoMagazzino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime creatoIl;

    @ManyToOne(optional = false)
    private Prodotto prodotto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimento tipo;

    private int quantita;

    @Column(length = Riassortimento.MASSIMO_NOTA)
    private String nota;

    private Long ordineId;

    protected MovimentoMagazzino() {
        // per JPA
    }

    private MovimentoMagazzino(LocalDateTime creatoIl, Prodotto prodotto, TipoMovimento tipo,
                               int quantita, String nota, Long ordineId) {
        this.creatoIl = creatoIl;
        this.prodotto = prodotto;
        this.tipo = tipo;
        this.quantita = quantita;
        this.nota = nota;
        this.ordineId = ordineId;
    }

    public static MovimentoMagazzino carico(Prodotto prodotto, Riassortimento richiesta,
                                            LocalDateTime quando) {
        return new MovimentoMagazzino(quando, prodotto, TipoMovimento.CARICO,
                richiesta.quantita(), richiesta.nota(), null);
    }

    public static MovimentoMagazzino scarico(RigaOrdine riga, Long ordineId, LocalDateTime quando) {
        return new MovimentoMagazzino(quando, riga.getProdotto(), TipoMovimento.SCARICO,
                riga.getQuantita(), null, ordineId);
    }

    public static MovimentoMagazzino reso(RigaOrdine riga, Long ordineId, LocalDateTime quando) {
        return new MovimentoMagazzino(quando, riga.getProdotto(), TipoMovimento.RESO,
                riga.getQuantita(), null, ordineId);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatoIl() {
        return creatoIl;
    }

    public Prodotto getProdotto() {
        return prodotto;
    }

    public TipoMovimento getTipo() {
        return tipo;
    }

    public int getQuantita() {
        return quantita;
    }

    public String getNota() {
        return nota;
    }

    public Long getOrdineId() {
        return ordineId;
    }
}
