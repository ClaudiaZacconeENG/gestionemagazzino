package com.epicode.magazzino.repository;

import com.epicode.magazzino.model.Ordine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdineRepository extends JpaRepository<Ordine, Long> {

    List<Ordine> findAllByOrderByCreatoIlDesc();
}
