package com.epicode.magazzino.repository;

import com.epicode.magazzino.model.Prodotto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdottoRepository extends JpaRepository<Prodotto, Long> {

    Optional<Prodotto> findByCodice(String codice);

    List<Prodotto> findAllByOrderByNomeAsc();
}
