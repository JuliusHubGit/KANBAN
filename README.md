# KANBAN

Ein einfaches Kanban-Board als Lernprojekt auf Basis von Spring Boot und einer MS SQL Server Datenbank.

## Funktionen

- Drei Spalten (To Do, In Arbeit, Erledigt) mit Drag & Drop Verschiebung.
- Aufgaben besitzen Beschreibung, verantwortliche Person, E-Mail-Adresse und ein Fälligkeitsdatum.
- Erledigte Aufgaben werden automatisch im Archiv gespeichert.
- REST-API für die Integration mit anderen Systemen (z. B. n8n Webhook für E-Mail-Benachrichtigungen).
- Einfache Weboberfläche, die direkt vom Spring Boot Server ausgeliefert wird.

## Voraussetzungen

- Java 17
- Maven 3.9+
- Docker (für die SQL Server Instanz)

## Datenbank starten

```bash
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=YourStrong!Passw0rd" \
  -p 1433:1433 --name kanban-sql \
  -d mcr.microsoft.com/mssql/server:2022-latest
```

Die Anwendung ist so konfiguriert, dass sie sich gegen `localhost:1433` verbindet. Die Zugangsdaten können in `src/main/resources/application.properties` angepasst werden.

## Anwendung starten

```bash
mvn spring-boot:run
```

Die Oberfläche ist danach unter [http://localhost:8080](http://localhost:8080) erreichbar.

## Tests ausführen

Die Anwendung enthält Basis-Tests für Service- und Controller-Ebene. Sie laufen gegen eine eingebettete H2-Datenbank und können mit folgendem Befehl gestartet werden:

```bash
mvn test
```

Die Testkonfiguration liegt unter `src/test/resources/application.properties`.

## REST-API Übersicht

| Methode | Pfad | Beschreibung |
|--------|------|--------------|
| GET | `/api/tasks` | Alle aktiven Aufgaben abrufen |
| POST | `/api/tasks` | Neue Aufgabe anlegen |
| PUT | `/api/tasks/{id}` | Aufgabe aktualisieren |
| POST | `/api/tasks/{id}/move` | Status ändern (z. B. durch Drag & Drop) |
| DELETE | `/api/tasks/{id}` | Aufgabe löschen |
| GET | `/api/tasks/archived` | Archivierte Aufgaben abrufen |

## n8n Webhook Integration

Setze in `application.properties` die Eigenschaft `notification.n8n-webhook-url` auf die URL deines Webhooks. Wenn eine Aufgabe erstellt, aktualisiert oder abgeschlossen wird, sendet die Anwendung eine JSON-Nachricht mit den wichtigsten Daten an diesen Endpunkt.

## UML-Diagramm

Die wichtigsten Klassen und Beziehungen des Systems sind in `docs/kanban-class-diagram.puml` als PlantUML-Datei dokumentiert. Du kannst das Diagramm z. B. mit [PlantUML](https://plantuml.com/de/class-diagram) oder einer IDE-Erweiterung rendern.

## Frontend nutzen

1. Aufgaben über das Formular oben anlegen.
2. Karten zwischen den Spalten ziehen, um den Status anzupassen.
3. Über das Stift-Symbol Aufgaben bearbeiten, über das Mülleimer-Symbol löschen.
4. Der Button „Archiv anzeigen“ blendet erledigte Aufgaben ein.

Viel Erfolg beim Lernen und Ausprobieren!
