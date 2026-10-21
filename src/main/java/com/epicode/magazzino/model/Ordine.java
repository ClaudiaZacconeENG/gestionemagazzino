package com.epicode.magazzino.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Un ordine di un cliente. Le regole di business stanno qui, non nel controller:
 * - si aggiungono righe solo a un ordine CONFERMATO, e ogni riga preleva dalla giacenza;
 * - lo stesso prodotto aggiunto due volte finisce nella stessa riga;
 * - annullare restituisce i pezzi al magazzino; un ordine spedito non si annulla.
 */
@Entity
@Table(name = "ordini")
public class Ordine {

    /** R5: da questa soglia di merce la spedizione è gratuita. */
    public static final BigDecimal SOGLIA_SPEDIZIONE_GRATUITA = new BigDecimal("150.00");
    public static final BigDecimal COSTO_SPEDIZIONE = new BigDecimal("9.90");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cliente;

    @Column(nullable = false)
    private LocalDateTime creatoIl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoOrdine stato;

    @OneToMany(mappedBy = "ordine", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<RigaOrdine> righe = new ArrayList<>();

    protected Ordine() {
        // per JPA
    }

    public Ordine(String cliente, LocalDateTime creatoIl) {
        if (cliente == null || cliente.isBlank()) {
            throw new RichiestaNonValidaException("Il cliente è obbligatorio");
        }
        this.cliente = cliente.strip();
        this.creatoIl = creatoIl;
        this.stato = StatoOrdine.CONFERMATO;
    }

    public void aggiungiRiga(Prodotto prodotto, int quantita) {
        if (stato != StatoOrdine.CONFERMATO) {
            throw new OperazioneNonConsentitaException("Non si aggiungono righe a un ordine " + stato);
        }
        prodotto.preleva(quantita);
        for (RigaOrdine riga : righe) {
            if (riga.getProdotto().getCodice().equals(prodotto.getCodice())) {
                riga.aumenta(quantita);
                return;
            }
        }
        righe.add(new RigaOrdine(this, prodotto, quantita));
    }

    /** R4: totale merce, somma dei subtotali già scontati. */
    public BigDecimal totale() {
        BigDecimal totale = BigDecimal.ZERO.setScale(2);
        for (RigaOrdine riga : righe) {
            totale = totale.add(riga.subtotale());
        }
        return totale;
    }

    /** R5: 9,90 euro sotto i 150,00 euro di merce, gratuita da 150,00 in su. */
    public BigDecimal speseSpedizione() {
        if (totale().compareTo(SOGLIA_SPEDIZIONE_GRATUITA) >= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return COSTO_SPEDIZIONE;
    }

    public BigDecimal totaleDaPagare() {
        return totale().add(speseSpedizione());
    }

    public void spedisci() {
        if (stato != StatoOrdine.CONFERMATO) {
            throw new OperazioneNonConsentitaException("Si spedisce solo un ordine confermato");
        }
        stato = StatoOrdine.SPEDITO;
    }

    public void annulla() {
        if (stato == StatoOrdine.SPEDITO) {
            throw new OperazioneNonConsentitaException("Un ordine spedito non si può annullare");
        }
        if (stato == StatoOrdine.ANNULLATO) {
            throw new OperazioneNonConsentitaException("L'ordine è già annullato");
        }
        for (RigaOrdine riga : righe) {
            riga.getProdotto().riassortisci(riga.getQuantita());
        }
        stato = StatoOrdine.ANNULLATO;
    }

    public Long getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }

    public LocalDateTime getCreatoIl() {
        return creatoIl;
    }

    public StatoOrdine getStato() {
        return stato;
    }

    public List<RigaOrdine> getRighe() {
        return Collections.unmodifiableList(righe);
    }
}
