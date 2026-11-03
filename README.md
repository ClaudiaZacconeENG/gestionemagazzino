# Magazzino & Ordini

L'applicazione del **Modulo 6** del corso *Agentic Software Development*: un piccolo
gestionale di ordini e magazzino su cui lavorano gli agenti di **GitHub Copilot**
(agent mode, plan mode, cloud agent, code review).

È volutamente semplice: il protagonista del modulo non è il codice ma come lo si fa
scrivere, verificare e rivedere agli agenti.

## Stack

| | |
| --- | --- |
| Linguaggio | Java 25 |
| Framework | Spring Boot 4.1.1 (Spring MVC, Spring Data JPA) |
| MCP | Spring AI 2.0.0, WebMVC, Streamable HTTP |
| Pagine | Thymeleaf (template HTML lato server) |
| Database | H2 in memoria: riparte dal catalogo iniziale a ogni avvio |
| Test | JUnit 5, Spring Boot Test, MockMvc |
| Build | Maven Wrapper (`mvnw`) |

## Avvio

```powershell
.\mvnw.cmd test              # tutti i test devono essere verdi
.\mvnw.cmd spring-boot:run   # poi apri http://localhost:8080
```

## Server MCP

Lo stesso processo espone un server MCP sincrono all'indirizzo
`http://127.0.0.1:8080/mcp`, senza richiedere un modello AI o chiavi API.
Le pagine web restano disponibili sulla stessa porta. L'ascolto e' limitato a
`127.0.0.1`: anche il gestionale e' quindi raggiungibile solo dalla macchina locale.

Il tool `trova_prodotto` accetta porzioni di codice oppure di nome prodotto,
senza distinguere maiuscole e minuscole. Il parametro mantiene il nome `codice`
per compatibilita, ma puo contenere anche un nome: per esempio `{"codice":"viti"}`
oppure `{"codice":"m6"}`. Gli spazi iniziali e finali vengono rimossi.

La risposta contiene `prodotti` (al massimo 20 elementi, ordinati per nome e
codice) e `trovati` (numero totale di corrispondenze, anche oltre 20). Ogni
prodotto contiene codice, nome, prezzo e disponibili. Senza corrispondenze la
risposta e' `{"prodotti":[],"trovati":0}`.

Meno di 2 caratteri, senza contare gli spazi, produce un risultato MCP con
`isError=true` e messaggio `Scrivi almeno 2 caratteri da cercare`.
Non modifica le scorte e non espone le entita JPA.

Il tool `prodotti_da_riassortire` non richiede parametri (argomenti `{}`) e
restituisce tutti i prodotti con giacenza minore o uguale alla scorta minima.
L'ordine e' per scopertura (`scortaMinima - disponibili`) decrescente e, a
parita di scopertura, per codice decrescente. Sono inclusi anche i prodotti
esattamente alla soglia, compreso il caso giacenza zero e scorta minima zero.

La risposta contiene `prodotti`, con codice, nome, prezzo, disponibili e
scortaMinima, e `trovati` con il numero totale. Non si applica il limite di 20
del tool di ricerca testuale. Se nessun prodotto richiede riassortimento,
restituisce `{"prodotti":[],"trovati":0}`. Anche questo tool e' di sola lettura.

Il tool `dettaglio_ordine` richiede il numero identificativo dell'ordine, per
esempio `{"numero":1}`. Restituisce `numero`, `cliente`, `stato`, `righe`,
`speseSpedizione` e `totaleDaPagare` come risultato strutturato e testo JSON.
Ogni riga contiene `codiceProdotto`, `nomeProdotto`, `quantita`, `prezzoUnitario`,
`percentualeSconto` e `subtotale` gia scontato e arrotondato al centesimo.
Il prezzo unitario e' quello storico salvato nell'ordine; spedizione e totale
riusano i calcoli del dominio. La consultazione non modifica ordini o scorte.
Se l'ordine non esiste, restituisce `isError=true` con messaggio
`Ordine N non trovato`, dove `N` e' il numero richiesto.

Avvia l'applicazione con il comando precedente, poi aggiungi questa voce alla
configurazione MCP di VS Code:

```json
{
   "servers": {
      "magazzino": {
         "type": "http",
         "url": "http://127.0.0.1:8080/mcp"
      }
   }
}
```

In alternativa, con Node.js disponibile, avvia MCP Inspector:

```powershell
npx @modelcontextprotocol/inspector
```

Seleziona Streamable HTTP e l'URL del server, connettiti e verifica `tools/list`
e `tools/call` sui tool `trova_prodotto`, `prodotti_da_riassortire` e `dettaglio_ordine`.
Un semplice GET nel browser non verifica il protocollo MCP.

L'endpoint non implementa autenticazione o autorizzazione. Non modificare
`server.address` per esporlo in rete senza prima aggiungere una protezione adeguata
con autenticazione, autorizzazione e HTTPS. Nessun tool di scrittura e' registrato.

I test automatici avviano un server su porta casuale e un client MCP reale per
verificare handshake, schema, risultati, errori e conservazione della giacenza:

```powershell
.\mvnw.cmd "-Dtest=CatalogoMcpTest" test
```

## Come è fatto

```
src/main/java/com/epicode/magazzino
├── model/        entità e regole di business (Prodotto, Ordine, RigaOrdine, StatoOrdine, eccezioni)
├── repository/   Spring Data JPA
├── service/      casi d'uso transazionali (CatalogoService, OrdineService, NuovoOrdine)
├── web/          controller Thymeleaf, form, gestione errori
└── config/       orologio (Clock) e dati iniziali
src/main/resources/templates   le pagine Thymeleaf
src/test/java                  test di dominio (senza Spring) e di integrazione
```

Il flusso di una richiesta: **controller → service → model/repository**. Il controller
non contiene regole; le regole stanno nel model e i service le orchestrano in una
transazione.

## Regole di business già presenti

- Un ordine nasce **CONFERMATO**; può diventare **SPEDITO** oppure **ANNULLATO**, nient'altro.
- Ogni riga preleva i pezzi dal magazzino; se non bastano l'ordine non viene creato.
- Lo stesso prodotto inserito due volte finisce nella stessa riga.
- Il prezzo della riga è quello del prodotto al momento dell'ordine.
- Annullare restituisce i pezzi al magazzino; un ordine spedito non si annulla.
- Importi in `BigDecimal`, arrotondati al centesimo.

## Usarlo come template (docente)

1. Pubblica il repo su GitHub e attiva **Settings → Template repository**.
2. Ogni studente: **Use this template → Create a new repository**, proprietario il
   proprio account, visibilità **Public** (serve per CodeQL e secret scanning gratuiti
   nella giornata 3), senza "Include all branches".
3. Il branch `soluzione/m6-g1` e i tag restano nel repo del docente: un repository
   creato dal template parte dal solo `main`.

Lo scheletro **non** contiene `.github/copilot-instructions.md`: scriverlo è il primo
esercizio della giornata 1.

## Giornate

| Giornata | Consegna |
|---|---|
| M6 · G1 — Feature authoring e test generation | [docs/corso/M6-G1.md](docs/corso/M6-G1.md) |
