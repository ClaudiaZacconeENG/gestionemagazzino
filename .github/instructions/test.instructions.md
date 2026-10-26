---
description: "Indicazioni per scrivere e verificare test automatici in src/test."
applyTo: "src/test/**"
---

# Istruzioni per i test

- Scrivi nomi di classi e metodi di test in italiano, coerenti con i test esistenti.
- Verifica comportamenti osservabili con asserzioni sul risultato o sulle eccezioni attese; evita test tautologici o che ripetano l'implementazione.
- Per regole con soglie, copri i valori ai confini e appena prima e dopo, così una modifica errata della soglia fa fallire almeno un test.
- Per gli importi, usa `BigDecimal` costruiti da stringhe e verifica importi arrotondati al centesimo con `RoundingMode.HALF_UP`; non usare `double` o `float`.
- Segui il livello di test già usato: test di dominio senza Spring per le regole del model e test Spring/MockMvc per l'integrazione tra service, repository e pagine.
- Esegui la suite con `.\mvnw.cmd test`.
