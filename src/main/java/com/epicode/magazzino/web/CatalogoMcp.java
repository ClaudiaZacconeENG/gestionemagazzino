package com.epicode.magazzino.web;

import com.epicode.magazzino.service.CatalogoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class CatalogoMcp {

    private final CatalogoService catalogo;

    public CatalogoMcp(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @McpTool(name = "trova_prodotto", description = "Cerca porzioni di codice o nome prodotto, ignorando maiuscole e minuscole. Richiede almeno 2 caratteri esclusi gli spazi. Restituisce al massimo 20 prodotti e trovati con il totale. Non modifica il magazzino.", generateOutputSchema = true, annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public RisultatoRicerca trovaProdotto(
            @McpToolParam(description = "Porzione di codice o nome prodotto, almeno 2 caratteri esclusi gli spazi", required = true) String codice) {
        var pagina = catalogo.cerca(codice);
        var prodotti = pagina.getContent().stream()
                .map(prodotto -> new DettaglioProdotto(prodotto.getCodice(), prodotto.getNome(),
                        prodotto.getPrezzo().setScale(2, RoundingMode.HALF_UP), prodotto.getDisponibili()))
                .toList();
        return new RisultatoRicerca(prodotti, pagina.getTotalElements());
    }

    @McpTool(name = "prodotti_da_riassortire", description = "Elenca tutti i prodotti con giacenza minore o uguale alla scorta minima, ordinati per scopertura (scorta minima meno giacenza) decrescente e, a parita di scopertura, per codice decrescente. Non modifica il magazzino.", generateOutputSchema = true, annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public RisultatoScorte prodottiDaRiassortire() {
        var prodotti = catalogo.daRiassortire().stream()
                .map(prodotto -> new ProdottoDaRiassortire(prodotto.getCodice(), prodotto.getNome(),
                        prodotto.getPrezzo().setScale(2, RoundingMode.HALF_UP), prodotto.getDisponibili(),
                        prodotto.getScortaMinima()))
                .toList();
        return new RisultatoScorte(prodotti, prodotti.size());
    }

    public record RisultatoScorte(List<ProdottoDaRiassortire> prodotti, long trovati) {
    }

    public record ProdottoDaRiassortire(String codice, String nome, BigDecimal prezzo, int disponibili,
            int scortaMinima) {
    }

    public record RisultatoRicerca(List<DettaglioProdotto> prodotti, long trovati) {
    }

    public record DettaglioProdotto(String codice, String nome, BigDecimal prezzo, int disponibili) {
    }
}