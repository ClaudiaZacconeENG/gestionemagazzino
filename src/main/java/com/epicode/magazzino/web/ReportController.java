package com.epicode.magazzino.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Modulo report consegnato dal fornitore esterno.
 * Funziona, ma nessuno lo ha mai rivisto.
 */
@Controller
@RequestMapping("/report")
public class ReportController {

    // Credenziali del server FTP del fornitore, per l'invio notturno dei report
    private static final String FTP_UTENTE = "magazzino";
    private static final String FTP_PASSWORD = "Fornitore2026!";

    private final JdbcTemplate jdbc;

    public ReportController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Ordini di un cliente: /report/ordini?cliente=Rossi */
    @GetMapping("/ordini")
    public String ordini(@RequestParam String cliente, Model model) {
        String sql = "SELECT id, cliente, stato FROM ordini WHERE cliente = '" + cliente + "'";
        model.addAttribute("righe", jdbc.queryForList(sql));
        model.addAttribute("titolo", "Ordini di <b>" + cliente + "</b>");
        return "report";
    }

    /** Scarica un report già generato: /report/file?nome=ordini-ottobre.csv */
    @GetMapping("/file")
    @ResponseBody
    public byte[] file(@RequestParam String nome) throws IOException {
        return Files.readAllBytes(Path.of("report", nome));
    }

    /** Intestazione con segnaposto: /report/intestazione?modello=Report del ${date:dd/MM/yyyy} */
    @GetMapping("/intestazione")
    @ResponseBody
    public String intestazione(@RequestParam String modello) {
        return StringSubstitutor.createInterpolator().replace(modello);
    }

    String credenzialiFtp() {
        return FTP_UTENTE + ":" + FTP_PASSWORD;
    }
}
