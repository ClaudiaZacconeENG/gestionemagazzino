# Istruzioni per il repository

## Stack

- Java 25 e Spring Boot 4.1.1.
- Applicazione web con Spring MVC e template Thymeleaf.
- Persistenza con Spring Data JPA e database H2 in memoria.
- Test con JUnit 5, Spring Boot Test e MockMvc.

## Architettura

Il codice applicativo si trova in `src/main/java/com/epicode/magazzino`:

- `model/`: entità, stati ed eccezioni di dominio; qui risiedono le regole di business.
- `repository/`: accesso ai dati tramite Spring Data JPA.
- `service/`: casi d'uso e orchestrazione transazionale.
- `web/`: controller, form e gestione degli errori.
- `config/`: configurazione e inizializzazione dei dati.

Il flusso è `controller → service → model/repository`: i controller non devono contenere regole di business. Le pagine Thymeleaf sono in `src/main/resources/templates`; i test in `src/test/java`.

## Regole di dominio e convenzioni

- Rappresenta sempre gli importi con `BigDecimal`, mai con `double` o `float`. Arrotonda al centesimo con `RoundingMode.HALF_UP`.
- Gli ordini possono passare solo da `CONFERMATO` a `SPEDITO` oppure da `CONFERMATO` ad `ANNULLATO`. Non introdurre altri stati o transizioni.
- Non aggiungere dipendenze a `pom.xml` senza averlo prima chiesto all'utente.
- Usa nomi italiani per classi, metodi e test, in coerenza con il codice esistente.

## Build e test

Esegui tutti i test con:

```powershell
.\mvnw.cmd test
```
