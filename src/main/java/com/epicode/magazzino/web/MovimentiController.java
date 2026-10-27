package com.epicode.magazzino.web;

import com.epicode.magazzino.service.MovimentiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MovimentiController {

    private final MovimentiService movimenti;

    public MovimentiController(MovimentiService movimenti) {
        this.movimenti = movimenti;
    }

    @GetMapping("/movimenti")
    public String elenco(Model model) {
        model.addAttribute("movimenti", movimenti.elenco());
        return "movimenti";
    }
}
