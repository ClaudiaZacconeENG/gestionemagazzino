package com.epicode.magazzino.config;

import com.epicode.magazzino.model.Prodotto;
import com.epicode.magazzino.repository.ProdottoRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Catalogo di prova: il database H2 è in memoria e riparte vuoto a ogni avvio. */
@Component
public class DatiIniziali implements CommandLineRunner {

    private final ProdottoRepository prodotti;

    public DatiIniziali(ProdottoRepository prodotti) {
        this.prodotti = prodotti;
    }

    @Override
    public void run(String... args) {
        if (prodotti.count() > 0) {
            return;
        }
        prodotti.save(new Prodotto("VIT-M6-30", "Viti M6x30 (conf. 100)", new BigDecimal("6.90"), 240, 50));
        prodotti.save(new Prodotto("TAS-8", "Tasselli 8 mm (conf. 50)", new BigDecimal("4.50"), 35, 40));
        prodotti.save(new Prodotto("TRA-18V", "Trapano a batteria 18V", new BigDecimal("89.00"), 12, 5));
        prodotti.save(new Prodotto("GUA-L", "Guanti da lavoro taglia L", new BigDecimal("3.20"), 150, 30));
        prodotti.save(new Prodotto("NAS-50", "Nastro americano 50 m", new BigDecimal("7.40"), 0, 10));
        prodotti.save(new Prodotto("MET-5", "Metro a nastro 5 m", new BigDecimal("8.90"), 60, 15));
        prodotti.save(new Prodotto("CAC-PH2", "Cacciavite a croce PH2", new BigDecimal("4.10"), 18, 20));
        prodotti.save(new Prodotto("COL-1KG", "Colla vinilica 1 kg", new BigDecimal("5.60"), 75, 25));
    }
}
