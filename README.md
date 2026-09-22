# WordQuartet

Progetto di Laboratorio III di **Antonio Fatticcioni**: un gioco multiplayer a riga di comando, realizzato in Java con architettura client/server e ispirato a Connections. Lo scopo è riconoscere quattro gruppi di quattro parole accomunate da un tema, partendo da 16 parole.

Il server gestisce utenti, partite a tempo, proposte, statistiche e classifiche. Client e server scambiano messaggi JSON via TCP; le notifiche asincrone, come l'inizio di una nuova partita, viaggiano via UDP.

## Requisiti

- **JDK 21 o successivo**, con `java`, `javac` e `jar` disponibili nel terminale.
- **Gson 2.13.2**, già inclusa in `lib/gson-2.13.2.jar`.

Non sono necessari Maven o Gradle. Eseguire tutti i comandi dalla cartella principale del progetto, perché i percorsi di configurazione e dati sono relativi a questa directory.

## Struttura

| Percorso | Contenuto |
| --- | --- |
| `src/client/` | Client interattivo, configurazione e ricezione notifiche UDP |
| `src/server/network/` | Avvio del server, gestione connessioni e notifiche |
| `src/server/services/` | Gestione utenti, caricamento dati, partite e timer |
| `src/server/model/` | Modelli di utenti, partite e classifiche |
| `src/shared/` | Formato dei messaggi condiviso tra client e server |
| `src/test/` | Programmi di prova eseguibili tramite i rispettivi `main` |
| `config/` | Configurazioni JSON di client e server |
| `data/` | Dataset delle parole e stato persistente |
| `lib/` | Libreria Gson |
| `Documentazione/` | Relazione del progetto |
| `manifest-client.txt`, `manifest-server.txt` | Manifest per creare i JAR eseguibili |

## Compilazione e avvio

Su macOS e Linux, compilare i sorgenti:

```sh
mkdir -p out
javac --release 21 -encoding UTF-8 -cp "lib/gson-2.13.2.jar" -d out src/client/*.java src/shared/*.java src/server/model/*.java src/server/network/*.java src/server/services/*.java src/test/*.java
```

Avviare prima il server:

```sh
java -cp "out:lib/gson-2.13.2.jar" server.network.ServerMain
```

In un altro terminale, aperto nella stessa cartella, avviare il client:

```sh
java -cp "out:lib/gson-2.13.2.jar" client.ClientMain
```

È possibile aprire più client in terminali separati. Su Windows il separatore del classpath è `;` al posto di `:`.

### Creare i JAR eseguibili

Dopo la compilazione:

```sh
jar cfm WordQuartetServer.jar manifest-server.txt -C out server -C out shared
jar cfm WordQuartetClient.jar manifest-client.txt -C out client -C out shared
```

Avviarli in due terminali separati, sempre dalla cartella principale:

```sh
java -jar WordQuartetServer.jar
```

```sh
java -jar WordQuartetClient.jar
```

I JAR richiedono anche la cartella `lib/` e i file di configurazione; il server richiede inoltre `data/`.

## Configurazione

In `config/server_config.json`:

| Campo | Valore incluso | Significato |
| --- | --- | --- |
| `port` | `8080` | Porta TCP di ascolto |
| `gamesFilePath` | `data/Connections_Data.json` | Dataset delle partite |
| `gameDuration` | `5` | Durata di ogni partita in minuti |

In `config/client_config.json`, `serverAddress` vale `127.0.0.1` e `serverPort` vale `8080`. Per collegarsi da un altro computer, impostare l'indirizzo del server e verificare che la porta corrisponda. Il client sceglie automaticamente una porta UDP per le notifiche.

## Comandi del client

Per iniziare, registrarsi, effettuare il login e richiedere le parole della partita corrente:

```text
REGISTER giocatore passwordDiProva
LOGIN giocatore passwordDiProva
REQUESTGAMEINFO -1
```

| Comando | Funzione |
| --- | --- |
| `HELP` | Mostra i comandi disponibili |
| `REGISTER <user> <password>` | Registra un utente |
| `LOGIN <user> <password>` | Accede al gioco |
| `LOGOUT` | Termina la sessione dell'utente |
| `UPDATECREDENTIALS <user> <password> <nuovoUser> <nuovaPassword>` | Modifica le credenziali |
| `SUBMITPROPOSAL <parola1> <parola2> <parola3> <parola4>` | Propone un gruppo di quattro parole |
| `REQUESTGAMEINFO <gameId>` | Richiede informazioni sulla partita; `-1` indica quella corrente |
| `REQUESTGAMESTATS <gameId>` | Richiede statistiche sulla partita; `-1` indica quella corrente |
| `REQUESTPLAYERSTATS` | Mostra le proprie statistiche |
| `REQUESTLEADERBOARD <user oppure k oppure -1>` | Mostra la posizione di un utente, i primi `k` giocatori o tutta la classifica |
| `EXIT` | Chiude il client |

## Dati e programmi di prova

- `data/Connections_Data.json` contiene il dataset delle partite.
- `data/users.json` contiene gli utenti e le relative statistiche.
- `data/games.json` conserva lo stato delle partite, recuperato dal server al riavvio.

I file di stato vengono aggiornati durante l'uso e sono già versionati nel repository: le loro modifiche rimangono visibili in Git. Il `.gitignore` esclude invece output compilati, JAR generati, impostazioni locali degli editor e file temporanei, mantenendo la dipendenza in `lib/`.

Le classi in `src/test/` sono programmi di prova manuali, non una suite JUnit. Dopo la compilazione si possono avviare, ad esempio, con:

```sh
java -cp "out:lib/gson-2.13.2.jar" test.LoginTester
```

I test di rete richiedono il server attivo. Alcuni programmi di prova modificano utenti o partite, oppure avviano timer: eseguirli su una copia dei dati se si vuole conservare lo stato esistente e interromperli con `Ctrl+C` quando necessario.
