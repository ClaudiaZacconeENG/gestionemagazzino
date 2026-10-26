# Magazzino & Ordini

L'applicazione del **Modulo 6** del corso *Agentic Software Development*: un piccolo
gestionale di ordini e magazzino su cui lavorano gli agenti di **GitHub Copilot**
(agent mode, plan mode, cloud agent, code review).

È volutamente semplice: il protagonista del modulo non è il codice ma come lo si fa
scrivere, verificare e rivedere agli agenti.

## Stack

| | |
|---|---|
| Linguaggio | Java 25 |
| Framework | Spring Boot 4.1.1 (Spring MVC, Spring Data JPA) |
| Pagine | Thymeleaf (template HTML lato server) |
| Database | H2 in memoria: riparte dal catalogo iniziale a ogni avvio |
| Test | JUnit 5, Spring Boot Test, MockMvc |
| Build | Maven Wrapper (`mvnw`) |

## Avvio

```powershell
.\mvnw.cmd test              # tutti i test devono essere verdi
.\mvnw.cmd spring-boot:run   # poi apri http://localhost:8080
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
