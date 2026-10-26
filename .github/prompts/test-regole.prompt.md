---
description: "Controlla la copertura dei test per le regole di sconto e spedizione R1-R5, i confini e i test tautologici."
name: "Verifica test delle regole"
---

Verifica i test della feature sconti per quantità secondo la specifica in [M6-G1](../../docs/corso/M6-G1.md) e le convenzioni in [test.instructions.md](../instructions/test.instructions.md).

Controlla in modo esplicito:

- R1: sconti 0% per 1–9, 5% per 10–49 e 10% da 50 pezzi; i confini 9/10 e 49/50 devono essere coperti.
- R2: subtotale di riga scontato e arrotondamento al centesimo con `RoundingMode.HALF_UP`.
- R3: due aggiunte dello stesso prodotto vengono accorpate prima di determinare lo sconto.
- R4: il totale merce è la somma dei subtotali scontati.
- R5: spedizione di 9,90 € sotto 150,00 € e gratuita da 150,00 € incluso; copri 149,99/150,00 e verifica il totale da pagare.

Per ogni regola indica i test che la verificano. Valuta se le asserzioni controllano risultati osservabili, se distinguono valori corretti e non corretti, e se qualche test è tautologico o non esercita davvero il comportamento dichiarato.

Se trovi lacune o asserzioni errate, correggi o aggiungi test mirati in italiano seguendo i pattern esistenti. Non modificare la logica di produzione solo per far passare un test; segnala separatamente eventuali difetti di produzione. Esegui `.\mvnw.cmd test` e riporta il risultato, le coperture R1–R5, i confini verificati e ogni problema residuo.
