package com.epicode.magazzino.web;

import com.epicode.magazzino.service.CatalogoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProdottiController {

    private final CatalogoService catalogo;

    public ProdottiController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/prodotti";
    }

    @GetMapping("/prodotti")
    public String elenco(Model model) {
        model.addAttribute("prodotti", catalogo.elenco());
        return "prodotti";
    }
}
