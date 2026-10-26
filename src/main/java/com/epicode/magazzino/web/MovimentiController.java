package com.epicode.magazzino.web;

import com.epicode.magazzino.service.CatalogoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MovimentiController {

    private final CatalogoService catalogo;

    public MovimentiController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/movimenti")
    public String elenco(Model model) {
        model.addAttribute("movimenti", catalogo.movimenti());
        return "movimenti";
    }
}
