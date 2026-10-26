package com.epicode.magazzino.web;

import com.epicode.magazzino.model.RisorsaNonTrovataException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Un ordine o un prodotto che non esiste diventa una pagina 404 leggibile. */
@ControllerAdvice
public class GestoreErrori {

    @ExceptionHandler(RisorsaNonTrovataException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String nonTrovato(RisorsaNonTrovataException e, Model model) {
        model.addAttribute("errore", e.getMessage());
        return "errore";
    }
}
