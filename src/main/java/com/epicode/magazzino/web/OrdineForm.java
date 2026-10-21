package com.epicode.magazzino.web;

import com.epicode.magazzino.service.NuovoOrdine;
import java.util.ArrayList;
import java.util.List;

/** Il form della pagina "Nuovo ordine": un cliente e tre righe, quelle vuote si ignorano. */
public class OrdineForm {

    public static final int RIGHE = 3;

    private String cliente;
    private List<RigaForm> righe = new ArrayList<>();

    public OrdineForm() {
        for (int i = 0; i < RIGHE; i++) {
            righe.add(new RigaForm());
        }
    }

    public NuovoOrdine toRichiesta() {
        List<NuovoOrdine.Riga> compilate = new ArrayList<>();
        for (RigaForm riga : righe) {
            boolean vuota = riga.getCodice() == null || riga.getCodice().isBlank()
                    || riga.getQuantita() == null;
            if (!vuota) {
                compilate.add(new NuovoOrdine.Riga(riga.getCodice(), riga.getQuantita()));
            }
        }
        return new NuovoOrdine(cliente, compilate);
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public List<RigaForm> getRighe() {
        return righe;
    }

    public void setRighe(List<RigaForm> righe) {
        this.righe = righe;
    }

    public static class RigaForm {

        private String codice;
        private Integer quantita;

        public String getCodice() {
            return codice;
        }

        public void setCodice(String codice) {
            this.codice = codice;
        }

        public Integer getQuantita() {
            return quantita;
        }

        public void setQuantita(Integer quantita) {
            this.quantita = quantita;
        }
    }
}
