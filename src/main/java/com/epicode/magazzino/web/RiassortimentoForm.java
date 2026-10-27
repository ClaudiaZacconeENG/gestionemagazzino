package com.epicode.magazzino.web;

import com.epicode.magazzino.model.RichiestaNonValidaException;

public class RiassortimentoForm {

    private String pezzi = "1";
    private String nota;

    public int getQuantita() {
        try {
            return Integer.parseInt(pezzi);
        } catch (NumberFormatException | NullPointerException e) {
            throw new RichiestaNonValidaException("I pezzi arrivati devono essere un numero intero");
        }
    }

    public String getPezzi() {
        return pezzi;
    }

    public void setPezzi(String pezzi) {
        this.pezzi = pezzi;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}
