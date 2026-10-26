package com.epicode.magazzino.service;

import com.epicode.magazzino.model.MovimentoMagazzino;
import com.epicode.magazzino.model.Ordine;
import com.epicode.magazzino.model.Prodotto;
import com.epicode.magazzino.model.RichiestaNonValidaException;
import com.epicode.magazzino.model.RigaOrdine;
import com.epicode.magazzino.model.RisorsaNonTrovataException;
import com.epicode.magazzino.repository.MovimentoRepository;
import com.epicode.magazzino.repository.OrdineRepository;
import com.epicode.magazzino.repository.ProdottoRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casi d'uso sugli ordini. Ogni metodo è una transazione: se una riga fallisce
 * (prodotto inesistente, scorta insufficiente) non resta nulla di scritto.
 */
@Service
@Transactional
public class OrdineService {

    private final OrdineRepository ordini;
    private final ProdottoRepository prodotti;
    private final MovimentoRepository movimenti;
    private final Clock clock;

    public OrdineService(OrdineRepository ordini, ProdottoRepository prodotti,
                         MovimentoRepository movimenti, Clock clock) {
        this.ordini = ordini;
        this.prodotti = prodotti;
        this.movimenti = movimenti;
        this.clock = clock;
    }

    public Ordine crea(NuovoOrdine richiesta) {
        if (richiesta.righe() == null || richiesta.righe().isEmpty()) {
            throw new RichiestaNonValidaException("Un ordine deve avere almeno una riga");
        }
        Ordine ordine = new Ordine(richiesta.cliente(), LocalDateTime.now(clock));
        for (NuovoOrdine.Riga riga : richiesta.righe()) {
            Prodotto prodotto = prodotti.findByCodice(riga.codiceProdotto())
                    .orElseThrow(() -> new RisorsaNonTrovataException(
                            "Prodotto " + riga.codiceProdotto() + " non trovato"));
            ordine.aggiungiRiga(prodotto, riga.quantita());
        }
        Ordine salvato = ordini.save(ordine);
        // R5: uno SCARICO per riga, con il numero d'ordine come riferimento
        for (RigaOrdine riga : salvato.getRighe()) {
            movimenti.save(MovimentoMagazzino.scarico(riga, salvato.getId(), LocalDateTime.now(clock)));
        }
        return salvato;
    }

    @Transactional(readOnly = true)
    public List<Ordine> elenco() {
        return ordini.findAllByOrderByCreatoIlDesc();
    }

    @Transactional(readOnly = true)
    public Ordine trova(Long id) {
        return ordini.findById(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Ordine " + id + " non trovato"));
    }

    public Ordine spedisci(Long id) {
        Ordine ordine = trova(id);
        ordine.spedisci();
        return ordine;
    }

    public Ordine annulla(Long id) {
        Ordine ordine = trova(id);
        ordine.annulla();
        // R5: i pezzi rientrati diventano un RESO per riga
        for (RigaOrdine riga : ordine.getRighe()) {
            movimenti.save(MovimentoMagazzino.reso(riga, ordine.getId(), LocalDateTime.now(clock)));
        }
        return ordine;
    }
}
