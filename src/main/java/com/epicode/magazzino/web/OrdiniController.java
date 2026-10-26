package com.epicode.magazzino.web;

import com.epicode.magazzino.model.MagazzinoException;
import com.epicode.magazzino.model.Ordine;
import com.epicode.magazzino.service.CatalogoService;
import com.epicode.magazzino.service.OrdineService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Il controller traduce HTTP in chiamate al service: nessuna regola di business qui. */
@Controller
@RequestMapping("/ordini")
public class OrdiniController {

    private final OrdineService ordini;
    private final CatalogoService catalogo;

    public OrdiniController(OrdineService ordini, CatalogoService catalogo) {
        this.ordini = ordini;
        this.catalogo = catalogo;
    }

    @GetMapping
    public String elenco(Model model) {
        model.addAttribute("ordini", ordini.elenco());
        return "ordini";
    }

    @GetMapping("/nuovo")
    public String nuovo(Model model) {
        model.addAttribute("form", new OrdineForm());
        model.addAttribute("prodotti", catalogo.elenco());
        return "ordine-nuovo";
    }

    @PostMapping
    public String crea(@ModelAttribute("form") OrdineForm form, Model model) {
        try {
            Ordine ordine = ordini.crea(form.toRichiesta());
            return "redirect:/ordini/" + ordine.getId();
        } catch (MagazzinoException e) {
            model.addAttribute("errore", e.getMessage());
            model.addAttribute("prodotti", catalogo.elenco());
            return "ordine-nuovo";
        }
    }

    @GetMapping("/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        model.addAttribute("ordine", ordini.trova(id));
        return "ordine-dettaglio";
    }

    @PostMapping("/{id}/spedisci")
    public String spedisci(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            ordini.spedisci(id);
            redirect.addFlashAttribute("messaggio", "Ordine spedito");
        } catch (MagazzinoException e) {
            redirect.addFlashAttribute("errore", e.getMessage());
        }
        return "redirect:/ordini/" + id;
    }

    @PostMapping("/{id}/annulla")
    public String annulla(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            ordini.annulla(id);
            redirect.addFlashAttribute("messaggio", "Ordine annullato: i pezzi sono tornati in magazzino");
        } catch (MagazzinoException e) {
            redirect.addFlashAttribute("errore", e.getMessage());
        }
        return "redirect:/ordini/" + id;
    }
}
