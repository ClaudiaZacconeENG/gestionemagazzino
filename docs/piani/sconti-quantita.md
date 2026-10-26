# Piano: sconti per quantità

## Obiettivo

Applicare lo sconto in base alla quantità complessiva di ciascuna riga, calcolare la spedizione sulla merce scontata e mostrare i totali richiesti nell'interfaccia ordini.

## Piano approvato

1. **Regole nel dominio**
   - In `RigaOrdine`, calcolare la percentuale di sconto dalla quantità aggregata: 0% da 1 a 9, 5% da 10 a 49, 10% da 50 in su.
   - Calcolare il subtotale scontato della riga con `BigDecimal`, arrotondando al centesimo con `RoundingMode.HALF_UP`.
   - Esporre la percentuale di sconto per mostrarla nel dettaglio.
   - In `Ordine`, sommare i subtotali scontati per il totale merce; applicare 9,90 € di spedizione sotto 150,00 € e zero da 150,00 € in su; esporre il totale da pagare.
2. **Interfaccia**
   - Nel dettaglio ordine mostrare la percentuale per riga, il totale merce, le spese di spedizione e il totale da pagare.
   - Nell'elenco ordini mostrare il totale da pagare.
3. **Verifica**
   - Estendere i test di dominio per le soglie 9/10 e 49/50, per l'aggregazione di due aggiunte dello stesso prodotto e per i totali di spedizione ai valori 149,99/150,00.
   - Aggiungere una verifica di formattazione/presentazione per dettaglio ed elenco.
   - Eseguire `.\mvnw.cmd test`.

## Correzione di un possibile fraintendimento

Lo sconto va determinato sulla quantità totale della riga dopo aver accorpato le aggiunte dello stesso prodotto (R3), non applicato separatamente a ogni aggiunta. Inoltre la soglia della spedizione si applica al totale merce già scontato (R4-R5), non al subtotale lordo.
