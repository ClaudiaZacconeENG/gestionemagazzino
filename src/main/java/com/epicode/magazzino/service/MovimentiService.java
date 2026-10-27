package com.epicode.magazzino.service;

import com.epicode.magazzino.model.Movimento;
import com.epicode.magazzino.model.Ordine;
import com.epicode.magazzino.model.Prodotto;
import com.epicode.magazzino.repository.MovimentoRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MovimentiService {

    private final MovimentoRepository movimenti;
    private final Clock clock;

    public MovimentiService(MovimentoRepository movimenti, Clock clock) {
        this.movimenti = movimenti;
        this.clock = clock;
    }

    public Movimento registraCarico(Prodotto prodotto, int pezzi, String nota) {
        return movimenti.save(Movimento.carico(LocalDateTime.now(clock), prodotto, pezzi, nota));
    }

    public void registraScarichi(Ordine ordine) {
        for (var riga : ordine.getRighe()) {
            movimenti.save(Movimento.scarico(LocalDateTime.now(clock), riga.getProdotto(),
                    riga.getQuantita(), ordine.getId()));
        }
    }

    public void registraResi(Ordine ordine) {
        for (var riga : ordine.getRighe()) {
            movimenti.save(Movimento.reso(LocalDateTime.now(clock), riga.getProdotto(),
                    riga.getQuantita(), ordine.getId()));
        }
    }

    @Transactional(readOnly = true)
    public List<Movimento> elenco() {
        return movimenti.findAllByOrderByDataOraDescIdDesc();
    }
}
