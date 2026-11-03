package com.epicode.magazzino.repository;

import com.epicode.magazzino.model.Prodotto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProdottoRepository extends JpaRepository<Prodotto, Long> {

    Optional<Prodotto> findByCodice(String codice);

    List<Prodotto> findAllByOrderByNomeAsc();

    Page<Prodotto> findByCodiceContainingIgnoreCaseOrNomeContainingIgnoreCase(
            String codice, String nome, Pageable pagina);

    @Query("""
            select prodotto from Prodotto prodotto
            where prodotto.disponibili <= prodotto.scortaMinima
            order by (prodotto.scortaMinima - prodotto.disponibili) desc, prodotto.codice desc
            """)
    List<Prodotto> trovaDaRiassortire();
}
