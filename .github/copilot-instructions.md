# Istruzioni per Copilot — Magazzino & Ordini

Gestionale di ordini e magazzino. Java 25, Spring Boot 4.1.1 (Spring MVC, Spring Data JPA),
Thymeleaf, H2 in memoria, JUnit 5.

## Architettura
- Il flusso è controller → service → model/repository. Il controller non contiene
  regole: traduce HTTP in chiamate al service e sceglie la pagina.
- Le regole di business stanno nelle entità del package `model`. I service le
  orchestrano in una transazione e non le duplicano.
- Gli errori di dominio estendono `MagazzinoException`: messaggio in italiano, per l'utente.

## Regole che non si indovinano dal codice
- Importi sempre in `BigDecimal`, mai `double`: al centesimo, con `RoundingMode.HALF_UP`.
- Stati dell'ordine: solo CONFERMATO → SPEDITO oppure CONFERMATO → ANNULLATO.
- Niente nuove dipendenze nel `pom.xml` senza chiederlo.
- Nomi di classi, metodi e test in italiano, come nel codice esistente.

## Build e verifica
- `./mvnw test` (Windows: `.\mvnw.cmd test`) resta verde dopo ogni modifica.
- Una feature non è finita senza test che ne verifichino le regole.
