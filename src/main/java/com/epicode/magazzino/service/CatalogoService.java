package com.epicode.magazzino.service;

import com.epicode.magazzino.model.MovimentoMagazzino;
import com.epicode.magazzino.model.Prodotto;
import com.epicode.magazzino.model.Riassortimento;
import com.epicode.magazzino.model.RisorsaNonTrovataException;
import com.epicode.magazzino.repository.MovimentoRepository;
import com.epicode.magazzino.repository.ProdottoRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CatalogoService {

    private final ProdottoRepository prodotti;
    private final MovimentoRepository movimenti;
    private final Clock clock;

    public CatalogoService(ProdottoRepository prodotti, MovimentoRepository movimenti, Clock clock) {
        this.prodotti = prodotti;
        this.movimenti = movimenti;
        this.clock = clock;
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

    /** R2-R4: la richiesta è già validata da Riassortimento; qui giacenza e movimento insieme. */
    public Prodotto riassortisci(String codice, Riassortimento richiesta) {
        Prodotto prodotto = trova(codice);
        prodotto.riassortisci(richiesta.quantita());
        movimenti.save(MovimentoMagazzino.carico(prodotto, richiesta, LocalDateTime.now(clock)));
        return prodotto;
    }

    /** R6: lo storico, dal movimento più recente. */
    @Transactional(readOnly = true)
    public List<MovimentoMagazzino> movimenti() {
        return movimenti.findAllByOrderByCreatoIlDescIdDesc();
    }
}
