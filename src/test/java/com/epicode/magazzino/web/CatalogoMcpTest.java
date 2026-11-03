package com.epicode.magazzino.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epicode.magazzino.model.RichiestaNonValidaException;
import com.epicode.magazzino.model.Prodotto;
import com.epicode.magazzino.model.Ordine;
import com.epicode.magazzino.model.StatoOrdine;
import com.epicode.magazzino.repository.OrdineRepository;
import com.epicode.magazzino.repository.ProdottoRepository;
import com.epicode.magazzino.service.CatalogoService;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogoMcpTest {

    @LocalServerPort
    private int porta;

    @Autowired
    private CatalogoService catalogo;

    @Autowired
    private CatalogoMcp strumenti;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private ProdottoRepository prodotti;

    @Autowired
    private OrdineRepository ordini;

    @Autowired
    private PlatformTransactionManager transazioni;

    @Autowired
    private Clock clock;

    @Autowired
    private EntityManager entita;

    private final List<Long> ordiniDiTest = new ArrayList<>();
    private final List<Long> prodottiDiTest = new ArrayList<>();

    private McpSyncClient client;

    @BeforeEach
    void collegaClient() {
        var trasporto = HttpClientStreamableHttpTransport.builder("http://127.0.0.1:" + porta)
                .endpoint("/mcp")
                .build();
        client = McpClient.sync(trasporto).requestTimeout(Duration.ofSeconds(10)).build();
        client.initialize();
    }

    @AfterEach
    void chiudiClient() {
        try {
            if (client != null) {
                client.closeGracefully();
            }
        } finally {
            if (!ordiniDiTest.isEmpty() || !prodottiDiTest.isEmpty()) {
                new TransactionTemplate(transazioni).executeWithoutResult(stato -> {
                    ordini.deleteAllById(ordiniDiTest);
                    prodotti.deleteAllById(prodottiDiTest);
                });
            }
        }
    }

    @Test
    void inizializzaServerEPubblicaIToolDiLettura() {
        assertEquals("magazzino-ordini", client.getServerInfo().name());
        assertEquals("1.0.0", client.getServerInfo().version());
        var strumentiDisponibili = client.listTools().tools();
        assertEquals(List.of("dettaglio_ordine", "prodotti_da_riassortire", "trova_prodotto"),
                strumentiDisponibili.stream().map(tool -> tool.name()).sorted().toList());
        for (var disponibile : strumentiDisponibili) {
            assertTrue(disponibile.annotations().readOnlyHint());
            assertFalse(disponibile.annotations().destructiveHint());
            if (!disponibile.name().equals("dettaglio_ordine")) {
                assertNotNull(disponibile.outputSchema());
            }
        }
        var strumento = strumentiDisponibili.stream()
                .filter(disponibile -> disponibile.name().equals("trova_prodotto")).findFirst().orElseThrow();
        assertEquals(List.of("codice"), strumento.inputSchema().get("required"));
        assertNotNull(strumento.outputSchema());
        var dettaglioOrdine = strumentiDisponibili.stream()
                .filter(disponibile -> disponibile.name().equals("dettaglio_ordine")).findFirst().orElseThrow();
        assertEquals(List.of("numero"), dettaglioOrdine.inputSchema().get("required"));
    }

    @Test
    void consultaProdottoSenzaModificareLaGiacenza() {
        int disponibiliPrima = catalogo.trova("VIT-M6-30").getDisponibili();

        var risultato = client.callTool(new CallToolRequest("trova_prodotto", Map.of("codice", "VIT-M6-30")));

        assertFalse(Boolean.TRUE.equals(risultato.isError()));
        var ricerca = json.convertValue(risultato.structuredContent(), CatalogoMcp.RisultatoRicerca.class);
        assertEquals(1, ricerca.trovati());
        assertEquals(1, ricerca.prodotti().size());
        var prodotto = ricerca.prodotti().getFirst();
        assertEquals("VIT-M6-30", prodotto.codice());
        assertEquals("Viti M6x30 (conf. 100)", prodotto.nome());
        assertEquals(new BigDecimal("6.90"), prodotto.prezzo().setScale(2, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("6.90"), strumenti.trovaProdotto("VIT-M6-30").prodotti().getFirst().prezzo());
        assertEquals(disponibiliPrima, prodotto.disponibili());
        assertEquals(disponibiliPrima, catalogo.trova("VIT-M6-30").getDisponibili());
    }

    @Test
    void restituisceElencoVuotoSenzaCorrispondenze() {
        var risultato = client.callTool(new CallToolRequest("trova_prodotto", Map.of("codice", "NON-ESISTE")));

        assertFalse(Boolean.TRUE.equals(risultato.isError()));
        var ricerca = json.convertValue(risultato.structuredContent(), CatalogoMcp.RisultatoRicerca.class);
        assertEquals(0, ricerca.trovati());
        assertTrue(ricerca.prodotti().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "v", " v ", "\tv\t", "\u00a0v\u00a0" })
    void rifiutaRicercheTroppoCorteTramiteProtocollo(String testo) {
        var risultato = client.callTool(new CallToolRequest("trova_prodotto", Map.of("codice", testo)));

        verificaErrore(risultato, "Scrivi almeno 2 caratteri da cercare");
    }

    @Test
    void rifiutaCodiceMancanteTramiteProtocollo() {
        var risultato = client.callTool(new CallToolRequest("trova_prodotto", Map.of()));

        assertTrue(Boolean.TRUE.equals(risultato.isError()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "\t", "v", " v " })
    void rifiutaCodiciNonValidi(String codice) {
        var errore = assertThrows(RichiestaNonValidaException.class, () -> strumenti.trovaProdotto(codice));
        assertEquals("Scrivi almeno 2 caratteri da cercare", errore.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "m6", "M6", "m6-", "ViTi", " vItI " })
    void cercaPorzioniDiCodiceONomeSenzaDistinguereMaiuscole(String testo) {
        var risultato = client.callTool(new CallToolRequest("trova_prodotto", Map.of("codice", testo)));

        assertFalse(Boolean.TRUE.equals(risultato.isError()));
        var ricerca = json.convertValue(risultato.structuredContent(), CatalogoMcp.RisultatoRicerca.class);
        assertEquals(1, ricerca.trovati());
        assertEquals(List.of("VIT-M6-30"), ricerca.prodotti().stream().map(prodotto -> prodotto.codice()).toList());
    }

    @ParameterizedTest
    @ValueSource(ints = { 19, 20, 21 })
    void limitaA20ProdottiMantenendoIlTotaleSenzaDuplicati(int quantita) {
        String prefisso = "RICERCA-MCP-" + quantita + "-";
        var inseriti = prodotti.saveAll(java.util.stream.IntStream.range(0, quantita)
                .mapToObj(indice -> new Prodotto(prefisso + String.format("%02d", indice),
                        prefisso + String.format("%02d", indice), new BigDecimal("1.00"), 1, 0))
                .toList());
        try {
            var risultato = client.callTool(new CallToolRequest("trova_prodotto",
                    Map.of("codice", prefisso.toLowerCase(java.util.Locale.ROOT))));

            assertFalse(Boolean.TRUE.equals(risultato.isError()));
            var ricerca = json.convertValue(risultato.structuredContent(), CatalogoMcp.RisultatoRicerca.class);
            assertEquals(quantita, ricerca.trovati());
            assertEquals(Math.min(20, quantita), ricerca.prodotti().size());
            assertEquals(inseriti.stream().limit(20).map(prodotto -> prodotto.getCodice()).toList(),
                    ricerca.prodotti().stream().map(prodotto -> prodotto.codice()).toList());
        } finally {
            prodotti.deleteAll(inseriti);
        }
    }

    @Test
    void trattaICaratteriJollyComeTestoLetterale() {
        var risultato = strumenti.trovaProdotto("%_");

        assertEquals(0, risultato.trovati());
        assertTrue(risultato.prodotti().isEmpty());
    }

    @Test
    void elencaScorteAllaSogliaOSottoOrdinatePerScoperturaECodiceDecrescenti() {
        var inseriti = prodotti.saveAll(List.of(
                new Prodotto("SCORTE-MCP-A", "Maggiore scopertura", new BigDecimal("1.25"), 0, 40),
                new Prodotto("SCORTE-MCP-Z", "Alfa", new BigDecimal("2.00"), 10, 40),
                new Prodotto("SCORTE-MCP-B", "Zeta", new BigDecimal("3.00"), 0, 30),
                new Prodotto("SCORTE-MCP-SOTTO", "Appena sotto", new BigDecimal("4.00"), 4, 5),
                new Prodotto("SCORTE-MCP-PARI", "Alla soglia", new BigDecimal("5.00"), 5, 5),
                new Prodotto("SCORTE-MCP-SOPRA", "Appena sopra", new BigDecimal("6.00"), 6, 5),
                new Prodotto("SCORTE-MCP-ZERO", "Soglia zero", new BigDecimal("0.00"), 0, 0)));
        try {
            var risultato = client.callTool(new CallToolRequest("prodotti_da_riassortire", Map.of()));

            assertFalse(Boolean.TRUE.equals(risultato.isError()));
            var scorte = json.convertValue(risultato.structuredContent(), CatalogoMcp.RisultatoScorte.class);
            assertEquals(scorte.prodotti().size(), scorte.trovati());
            assertEquals(List.of("SCORTE-MCP-A", "SCORTE-MCP-Z", "SCORTE-MCP-B"),
                    scorte.prodotti().stream().limit(3).map(prodotto -> prodotto.codice()).toList());
            assertEquals(List.of("SCORTE-MCP-A", "SCORTE-MCP-Z", "SCORTE-MCP-B", "SCORTE-MCP-SOTTO",
                    "SCORTE-MCP-ZERO", "SCORTE-MCP-PARI"),
                    scorte.prodotti().stream().map(prodotto -> prodotto.codice())
                            .filter(codice -> codice.startsWith("SCORTE-MCP-")).toList());
            assertTrue(scorte.prodotti().stream()
                    .allMatch(prodotto -> prodotto.disponibili() <= prodotto.scortaMinima()));
            var primo = scorte.prodotti().getFirst();
            assertEquals("Maggiore scopertura", primo.nome());
            assertEquals(new BigDecimal("1.25"), primo.prezzo().setScale(2, RoundingMode.HALF_UP));
            assertEquals(0, primo.disponibili());
            assertEquals(40, primo.scortaMinima());
            for (var prodotto : inseriti) {
                assertEquals(prodotto.getDisponibili(), catalogo.trova(prodotto.getCodice()).getDisponibili());
            }
        } finally {
            prodotti.deleteAll(inseriti);
        }
    }

    @Test
    void nonLimitaA20IProdottiDaRiassortire() {
        var inseriti = prodotti.saveAll(java.util.stream.IntStream.range(0, 21)
                .mapToObj(indice -> new Prodotto("SCORTE-OLTRE-" + indice, "Scorta aggiuntiva " + indice,
                        new BigDecimal("1.00"), 0, 1))
                .toList());
        try {
            var risultato = client.callTool(new CallToolRequest("prodotti_da_riassortire", Map.of()));

            assertFalse(Boolean.TRUE.equals(risultato.isError()));
            var scorte = json.convertValue(risultato.structuredContent(), CatalogoMcp.RisultatoScorte.class);
            assertEquals(scorte.prodotti().size(), scorte.trovati());
            assertEquals(21, scorte.prodotti().stream()
                    .filter(prodotto -> prodotto.codice().startsWith("SCORTE-OLTRE-")).count());
        } finally {
            prodotti.deleteAll(inseriti);
        }
    }

    @Test
    void restituisceElencoVuotoSeNessunProdottoRichiedeRiassortimento() {
        var catalogoVuoto = mock(CatalogoService.class);
        when(catalogoVuoto.daRiassortire()).thenReturn(List.of());

        var risultato = new CatalogoMcp(catalogoVuoto).prodottiDaRiassortire();

        assertEquals(0, risultato.trovati());
        assertTrue(risultato.prodotti().isEmpty());
    }

    @Test
    void segnalaOrdineInesistenteConIlNumeroRichiesto() {
        var risultato = client.callTool(new CallToolRequest("dettaglio_ordine", Map.of("numero", Long.MAX_VALUE)));

        assertTrue(Boolean.TRUE.equals(risultato.isError()));
        assertEquals(List.of("Ordine " + Long.MAX_VALUE + " non trovato"), risultato.content().stream()
                .filter(TextContent.class::isInstance)
                .map(TextContent.class::cast)
                .map(TextContent::text).toList());
    }

    @Test
    void restituisceClienteStatoERigheScontateSenzaModificareOrdineOScorte() {
        Long numero = creaOrdineDiTest(new RigaDiTest("1.00", 9), new RigaDiTest("1.99", 10),
                new RigaDiTest("1.00", 50));
        var prima = ordini.findById(numero).orElseThrow();

        var dettaglio = consultaOrdine(numero);

        assertEquals(numero, dettaglio.numero());
        assertEquals("Cliente MCP", dettaglio.cliente());
        assertEquals(StatoOrdine.CONFERMATO, dettaglio.stato());
        assertEquals(3, dettaglio.righe().size());
        var perQuantita = dettaglio.righe().stream().collect(java.util.stream.Collectors.toMap(
                OrdiniMcp.RigaDettaglio::quantita, riga -> riga));
        assertEquals(0, perQuantita.get(9).percentualeSconto());
        assertEquals(5, perQuantita.get(10).percentualeSconto());
        assertEquals(10, perQuantita.get(50).percentualeSconto());
        verificaImporto("18.91", perQuantita.get(10).subtotale());
        verificaImporto("9.90", dettaglio.speseSpedizione());
        verificaImporto("82.81", dettaglio.totaleDaPagare());
        for (var riga : dettaglio.righe()) {
            assertEquals("Prodotto MCP", riga.nomeProdotto());
            assertEquals(100 - riga.quantita(), catalogo.trova(riga.codiceProdotto()).getDisponibili());
        }
        var dopo = ordini.findById(numero).orElseThrow();
        assertEquals(prima.getStato(), dopo.getStato());
        assertEquals(prima.getCliente(), dopo.getCliente());
        assertEquals(prima.getRighe().size(), dopo.getRighe().size());
        assertEquals(prima.totaleDaPagare(), dopo.totaleDaPagare());
    }

    @ParameterizedTest
    @CsvSource({ "9, 0, 17.91, 27.81", "10, 5, 18.91, 28.81", "49, 5, 92.63, 102.53", "50, 10, 89.55, 99.45" })
    void esponeScontiESubtotaliAiConfini(int quantita, int percentuale, String subtotale, String totale) {
        Long numero = creaOrdineDiTest(new RigaDiTest("1.99", quantita));

        var dettaglio = consultaOrdine(numero);

        var riga = dettaglio.righe().getFirst();
        assertEquals(quantita, riga.quantita());
        assertEquals(percentuale, riga.percentualeSconto());
        verificaImporto("1.99", riga.prezzoUnitario());
        verificaImporto(subtotale, riga.subtotale());
        verificaImporto(totale, dettaglio.totaleDaPagare());
    }

    @ParameterizedTest
    @CsvSource({ "149.99, 1, 9.90, 159.89", "150.00, 1, 0.00, 150.00", "150.01, 1, 0.00, 150.01",
            "15.50, 10, 9.90, 157.15" })
    void esponeSpedizioneSulTotaleScontato(String prezzo, int quantita, String spedizione, String totale) {
        Long numero = creaOrdineDiTest(new RigaDiTest(prezzo, quantita));

        var dettaglio = consultaOrdine(numero);

        verificaImporto(spedizione, dettaglio.speseSpedizione());
        verificaImporto(totale, dettaglio.totaleDaPagare());
    }

    @ParameterizedTest
    @EnumSource(StatoOrdine.class)
    void leggeLoStatoDellOrdineSenzaCambiarlo(StatoOrdine statoAtteso) {
        Long numero = creaOrdineDiTest(new RigaDiTest("2.00", 1));
        new TransactionTemplate(transazioni).executeWithoutResult(transazione -> {
            var ordine = ordini.findById(numero).orElseThrow();
            if (statoAtteso == StatoOrdine.SPEDITO) {
                ordine.spedisci();
            } else if (statoAtteso == StatoOrdine.ANNULLATO) {
                ordine.annulla();
            }
        });

        var dettaglio = consultaOrdine(numero);

        assertEquals(statoAtteso, dettaglio.stato());
        assertEquals(statoAtteso, ordini.findById(numero).orElseThrow().getStato());
        int giacenzaAttesa = statoAtteso == StatoOrdine.ANNULLATO ? 100 : 99;
        assertEquals(giacenzaAttesa, catalogo.trova(dettaglio.righe().getFirst().codiceProdotto()).getDisponibili());
    }

    @Test
    void usaIlPrezzoStoricoAncheSeIlCatalogoCambia() {
        Long numero = creaOrdineDiTest(new RigaDiTest("1.99", 10));
        new TransactionTemplate(transazioni).executeWithoutResult(transazione -> entita
                .createQuery("update Prodotto prodotto set prodotto.prezzo = :prezzo where prodotto.id = :id")
                .setParameter("prezzo", new BigDecimal("9.99"))
                .setParameter("id", prodottiDiTest.getFirst())
                .executeUpdate());

        var dettaglio = consultaOrdine(numero);

        verificaImporto("1.99", dettaglio.righe().getFirst().prezzoUnitario());
        verificaImporto("18.91", dettaglio.righe().getFirst().subtotale());
        verificaImporto("28.81", dettaglio.totaleDaPagare());
    }

    @Test
    void rifiutaIlNumeroOrdineMancante() {
        var risultato = client.callTool(new CallToolRequest("dettaglio_ordine", Map.of()));

        assertTrue(Boolean.TRUE.equals(risultato.isError()));
    }

    private Long creaOrdineDiTest(RigaDiTest... righe) {
        return new TransactionTemplate(transazioni).execute(transazione -> {
            var ordine = new Ordine("Cliente MCP", LocalDateTime.now(clock));
            for (var riga : righe) {
                var prodotto = prodotti.save(new Prodotto("ORD-MCP-" + UUID.randomUUID(), "Prodotto MCP",
                        new BigDecimal(riga.prezzo()), 100, 0));
                prodottiDiTest.add(prodotto.getId());
                ordine.aggiungiRiga(prodotto, riga.quantita());
            }
            var salvato = ordini.saveAndFlush(ordine);
            ordiniDiTest.add(salvato.getId());
            return salvato.getId();
        });
    }

    private OrdiniMcp.DettaglioOrdine consultaOrdine(Long numero) {
        var risultato = client.callTool(new CallToolRequest("dettaglio_ordine", Map.of("numero", numero)));
        assertFalse(Boolean.TRUE.equals(risultato.isError()));
        assertNotNull(risultato.structuredContent());
        return json.convertValue(risultato.structuredContent(), OrdiniMcp.DettaglioOrdine.class);
    }

    private void verificaImporto(String atteso, BigDecimal effettivo) {
        assertEquals(new BigDecimal(atteso), effettivo.setScale(2, RoundingMode.HALF_UP));
    }

    private record RigaDiTest(String prezzo, int quantita) {
    }

    private void verificaErrore(CallToolResult risultato, String messaggio) {
        assertTrue(Boolean.TRUE.equals(risultato.isError()));
        assertTrue(risultato.content().stream()
                .filter(TextContent.class::isInstance)
                .map(TextContent.class::cast)
                .anyMatch(contenuto -> contenuto.text().contains(messaggio)));
    }
}