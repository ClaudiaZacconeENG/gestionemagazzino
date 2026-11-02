package com.epicode.magazzino.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private static final Pattern SEGNAPOSTO_DATA = Pattern.compile("\\$\\{date:([^}]+)}");

    // Credenziali del server FTP del fornitore, per l'invio notturno dei report
    private static final String FTP_UTENTE = "magazzino";
    private static final String FTP_PASSWORD = "Fornitore2026!";

    private final JdbcTemplate jdbc;
    private final Clock clock;

    public ReportController(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
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
        Matcher matcher = SEGNAPOSTO_DATA.matcher(modello);
        StringBuilder risultato = new StringBuilder();
        while (matcher.find()) {
            String data = LocalDate.now(clock)
                    .format(DateTimeFormatter.ofPattern(matcher.group(1)));
            matcher.appendReplacement(risultato, Matcher.quoteReplacement(data));
        }
        matcher.appendTail(risultato);
        return risultato.toString();
    }

    String credenzialiFtp() {
        return FTP_UTENTE + ":" + FTP_PASSWORD;
    }
}
