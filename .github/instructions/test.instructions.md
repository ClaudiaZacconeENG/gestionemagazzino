---
applyTo: "src/test/**"
description: "Come si scrivono i test di questo repository"
---
# Test
- JUnit 5 con `org.junit.jupiter.api.Assertions`. Le regole di dominio si testano
  senza Spring (`new Ordine(...)`); `@SpringBootTest` solo per i casi d'uso.
- Ogni test verifica una regola della specifica e il nome del metodo la dice
  (es. `daDieciAQuarantanovePezziScontoCinquePerCento`).
- I valori attesi si calcolano a mano dalla specifica, mai chiamando il codice
  sotto test.
- Coprire sempre i confini (9 e 10, 49 e 50) e almeno un caso di errore.
- Importi confrontati come `new BigDecimal("55.58")`, con due decimali.
