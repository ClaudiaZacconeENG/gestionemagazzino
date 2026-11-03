package com.epicode.magazzino.web;

import com.epicode.magazzino.model.RisorsaNonTrovataException;
import com.epicode.magazzino.model.StatoOrdine;
import com.epicode.magazzino.service.OrdineService;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class OrdiniMcp {

    private final OrdineService ordini;
    private final ObjectMapper json;

    public OrdiniMcp(OrdineService ordini, ObjectMapper json) {
        this.ordini = ordini;
        this.json = json;
    }

    @McpTool(name = "dettaglio_ordine", description = "Consulta un ordine tramite il suo numero: cliente, stato, righe con sconto percentuale e subtotale scontato, spese di spedizione e totale da pagare. Non modifica ordini o scorte.", annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public CallToolResult dettaglioOrdine(
            @McpToolParam(description = "Numero identificativo dell'ordine", required = true) Long numero) {
        try {
            var dettaglio = dettaglio(numero);
            return CallToolResult.builder().structuredContent(dettaglio)
                    .addTextContent(json.writeValueAsString(dettaglio)).isError(false).build();
        } catch (RisorsaNonTrovataException errore) {
            return CallToolResult.builder().isError(true).addTextContent(errore.getMessage()).build();
        }
    }

    private DettaglioOrdine dettaglio(Long numero) {
        var ordine = ordini.trova(numero);
        var righe = ordine.getRighe().stream()
                .map(riga -> new RigaDettaglio(riga.getProdotto().getCodice(), riga.getProdotto().getNome(),
                        riga.getQuantita(), riga.getPrezzoUnitario().setScale(2, RoundingMode.HALF_UP),
                        riga.getPercentualeSconto(), riga.subtotale()))
                .toList();
        return new DettaglioOrdine(ordine.getId(), ordine.getCliente(), ordine.getStato(), righe,
                ordine.speseSpedizione(), ordine.totaleDaPagare());
    }

    public record DettaglioOrdine(Long numero, String cliente, StatoOrdine stato, List<RigaDettaglio> righe,
            BigDecimal speseSpedizione, BigDecimal totaleDaPagare) {
    }

    public record RigaDettaglio(String codiceProdotto, String nomeProdotto, int quantita,
            BigDecimal prezzoUnitario, int percentualeSconto, BigDecimal subtotale) {
    }
}