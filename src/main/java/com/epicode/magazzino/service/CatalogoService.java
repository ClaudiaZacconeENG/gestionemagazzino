package com.epicode.magazzino.service;

import com.epicode.magazzino.model.Prodotto;
import com.epicode.magazzino.model.RisorsaNonTrovataException;
import com.epicode.magazzino.repository.ProdottoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CatalogoService {

    private final ProdottoRepository prodotti;

    public CatalogoService(ProdottoRepository prodotti) {
        this.prodotti = prodotti;
    }

    @Transactional(readOnly = true)
    public List<Prodotto> elenco() {
        return prodotti.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Prodotto trova(String codice) {
        return prodotti.findByCodice(codice)
                .orElseThrow(() -> new RisorsaNonTrovataException("Prodotto " + codice + " non trovato"));
    }

    public Prodotto riassortisci(String codice, int quantita) {
        Prodotto prodotto = trova(codice);
        prodotto.riassortisci(quantita);
        return prodotto;
    }
}
