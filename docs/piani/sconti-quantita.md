# Piano — Sconti per quantità e spedizione (M6 · G1)

Piano prodotto con **Plan** a partire da `docs/corso/M6-G1.md`, poi corretto a mano.

## Correzioni del docente al piano proposto
- Il piano voleva salvare lo sconto in una nuova colonna di `RigaOrdine`: non serve,
  si calcola dalla quantità (R3) e così resta giusto quando la riga viene aumentata.
- Il piano confrontava la soglia dei 150 € con il totale **lordo**: la specifica
  dice merce scontata (R4-R5). Aggiunto il test `laSogliaSiConfrontaConLaMerceGiaScontata`.
- Il piano metteva il calcolo della spedizione nel controller: spostato in `Ordine`.

## Passi
1. `model/ScontoQuantita`: `percentuale(int quantita)` con gli scaglioni 0/5/10 (R1).
2. `RigaOrdine`: `percentualeSconto()`, `lordo()`, `subtotale()` scontato e arrotondato HALF_UP (R2, R3).
3. `Ordine`: `totale()` resta la merce (R4); nuovi `speseSpedizione()` e `totaleDaPagare()` (R5).
4. Pagine: colonna Sconto e tre righe di totale nel dettaglio, "Da pagare" nell'elenco (R6).
5. Test: `ScontoQuantitaTest` (confini) e `OrdineScontiTest` (importi, soglia, riga aumentata).
6. `./mvnw test` verde; mutazione 10 → 11: falliscono 3 test.
