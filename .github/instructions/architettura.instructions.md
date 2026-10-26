---
applyTo: "src/main/java/**"
description: "Regole di architettura da rispettare quando si scrive codice e da controllare in review"
---
# Architettura

Dipendenze ammesse tra i package di `com.epicode.magazzino`:

- `web` → `service`, `model`. **Mai** `repository`: un controller non apre il database.
- `service` → `model`, `repository`. Un metodo pubblico = un caso d'uso `@Transactional`.
- `model` → nessun altro package dell'applicazione, niente Spring MVC.
- `repository` → `model`. Solo interfacce Spring Data, nessuna logica.

Regole:
- Le regole di business stanno nel `model`; i service le orchestrano; controller e
  template non decidono nulla.
- L'ora corrente arriva dal bean `Clock` (`LocalDateTime.now(clock)`), mai da
  `LocalDateTime.now()` senza argomenti: altrimenti i test non la controllano.
- Errori di dominio: sottoclassi di `MagazzinoException`, messaggio in italiano.

In review segnala sempre:
- un `import com.epicode.magazzino.repository` dentro `web`;
- un `if` con una regola di business dentro un controller o un template;
- una regola nuova senza un test che ne copra i confini.
