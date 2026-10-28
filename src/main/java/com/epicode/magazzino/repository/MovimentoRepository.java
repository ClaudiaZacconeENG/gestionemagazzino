package com.epicode.magazzino.repository;

import com.epicode.magazzino.model.MovimentoMagazzino;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentoRepository extends JpaRepository<MovimentoMagazzino, Long> {

    List<MovimentoMagazzino> findAllByOrderByCreatoIlDescIdDesc();
}
