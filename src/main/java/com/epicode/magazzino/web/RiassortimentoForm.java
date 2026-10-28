package com.epicode.magazzino.web;

/** Il form della pagina di riassortimento: i valori arrivano grezzi, li valida Riassortimento. */
public class RiassortimentoForm {

    private Integer quantita;
    private String nota;

    public Integer getQuantita() {
        return quantita;
    }

    public void setQuantita(Integer quantita) {
        this.quantita = quantita;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}
