---
description: "Genera i test JUnit che verificano le regole di una specifica"
agent: agent
argument-hint: "la specifica, es. #file:docs/corso/M6-G1.md"
---
Leggi la specifica indicata e il codice che la implementa.

1. Elenca le regole (R1, R2, ...) e per ognuna i casi di confine.
2. Per ogni regola scrivi almeno un test JUnit in `src/test/java`, seguendo le
   istruzioni dei test del repository.
3. Calcola i valori attesi dalla specifica, non dal codice.
4. Esegui `./mvnw test` e chiudi con una tabella: regola → test che la verifica.

Non modificare il codice di produzione: se un test fallisce, segnalalo e fermati.
