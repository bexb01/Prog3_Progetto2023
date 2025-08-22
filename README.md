# 📧 Sistema di Posta Elettronica in Java

[![Java](https://img.shields.io/badge/Java-17-blue?logo=java&logoColor=white&style=flat)](https://www.java.com/)
[![JavaFX](https://img.shields.io/badge/JavaFX-GUI-61DAFB?logo=java&logoColor=white&style=flat)](https://openjfx.io/)
[![TCP Sockets](https://img.shields.io/badge/TCP%20Sockets-Network-orange?style=flat)]()
[![MVC](https://img.shields.io/badge/MVC-Pattern-green?style=flat)]()
[![Threading](https://img.shields.io/badge/Threading-Concurrency-red?style=flat)]()
[![File Persistence](https://img.shields.io/badge/File%20Persistence-Storage-yellow?style=flat)]()
[![License MIT](https://img.shields.io/badge/License-MIT-brightgreen?style=flat)]()

## Descrizione
Progetto Java distribuito con **Mail Server** e **Mail Client**. Permette di inviare, ricevere, rispondere e inoltrare messaggi tramite interfacce grafiche **JavaFX**.

---

## Struttura del progetto

```
Progetto_Prog3/
├── client/                # Codice e risorse Mail Client
├── server/                # Codice e risorse Mail Server
├── docs/screenshots/      # Screenshot interfacce
├── README.md
└── .gitignore
```

---

## Interfacce principali

**Mail Server**: log di connessioni e azioni dei client, gestione errori.

![Mail Server](docs/screenshots/mail_server_main.png)

**Mail Client**: lista messaggi, invio, Reply/Reply All, Forward, rimozione messaggi, notifiche nuove email.

![Mail Client - Inbox](docs/screenshots/mail_client_inbox.png)  
![Mail Client - Composizione](docs/screenshots/mail_client_compose.png)  
![Mail Client - Reply/Forward](docs/screenshots/mail_client_reply_forward.png)

---

## Requisiti

- Java 17+, JavaFX  
- Architettura MVC + Observer/Observable  
- Persistenza tramite file (txt)  
- Socket TCP per comunicazione client-server  
- Operazioni parallele tramite thread  

---

## Avvio

### Server
```bash
cd server
javac -d bin src/server/*.java
java -cp bin server.MailServer
```

### Client
```bash
cd client
javac -d bin src/client/*.java
java -cp bin client.MailClient
```

> Il client può essere avviato più volte, ciascuno associato a un account diverso, e si connette al server in modo indipendente. Il server gestisce più client in parallelo tramite thread.


---

## Licenza
Distribuito sotto **licenza MIT**. Vedi LICENSE per dettagli.
