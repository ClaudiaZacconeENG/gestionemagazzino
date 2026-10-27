package com.epicode.magazzino.repository;

import com.epicode.magazzino.model.Movimento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentoRepository extends JpaRepository<Movimento, Long> {

    List<Movimento> findAllByOrderByDataOraDescIdDesc();
}
