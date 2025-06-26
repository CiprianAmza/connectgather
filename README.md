# ConnectGather-Microservices

**Proiect pentru disciplina: Aplicații Web pentru Baze de Date - Master Anul I, 2025**

**Dezvoltat de:**
* Andrei Dina
* Ciprian Amza

<img width="1214" alt="image" src="https://github.com/user-attachments/assets/8151fe1d-ada5-4e67-aeed-b717d1d297a9" />

## Descriere Proiect

`ConnectGather-Microservices` este o aplicație web complexă, concepută ca o **platformă de management al evenimentelor**, facilitând crearea, organizarea și participarea la diverse tipuri de evenimente. Proiectul este structurat în două faze majore, reflectând cerințele examenului de master:

1.  **Faza I: Aplicație Monolitică (Spring MVC)**
    O aplicație web robustă, bazată pe arhitectura Model-View-Controller (MVC) și implementată cu Spring Framework. Această fază se concentrează pe oferirea de funcționalități CRUD (Create, Read, Update, Delete) pentru entitățile cheie legate de evenimente și participanți, gestionând relațiile complexe dintre acestea.

2.  **Faza II: Migrare la Microservicii**
    Transformarea aplicației monolitice într-o arhitectură modernă bazată pe microservicii. Această fază va demonstra concepte esențiale de sisteme distribuite, scalabilitate orizontală, reziliență și comunicare inter-servicii, permițând o gestionare mai eficientă a componentelor platformei de evenimente.

## Funcționalități Implementate

### Faza I: Aplicație Monolitică

Această fază se concentrează pe o implementare solidă a unei aplicații web monolitice cu următoarele caracteristici cheie:

* **Management Entități pentru Evenimente:**
    * **Utilizatori (Users):** Înregistrare, autentificare, gestionarea profilurilor de utilizatori (organizatori de evenimente și participanți).
    * **Evenimente (Events):** Crearea, editarea, vizualizarea și ștergerea evenimentelor, inclusiv detalii precum nume, descriere, dată/oră, locație.
    * **Locații (Locations):** Gestionarea detaliilor locațiilor unde se desfășoară evenimentele.
    * **Categorii Evenimente (Event Categories):** Clasificarea evenimentelor pe baza tipului lor (ex. conferințe, workshop-uri, concerte, întâlniri).
    * **Înscrieri/Participări (Registrations/Attendances):** Sistem de înregistrare pentru participanți la evenimente și monitorizarea prezenței.
    * **Recenzii/Feedback (Reviews/Feedback):** Colectarea și afișarea recenziilor sau feedback-ului pentru evenimente.
    * **Relații între Entități:** Implementarea relațiilor de tip One-to-One, One-to-Many și Many-to-Many (ex. User-Event (creator), User-Event (participant), Event-Location, Event-Category).
* **Operații CRUD:** Implementarea completă a operațiilor de creare, citire, actualizare și ștergere pentru toate entitățile principale (Utilizatori, Evenimente, Locații, etc.).
* **Validare Formulare:** Validarea riguroasă a datelor introduse prin formulare, inclusiv tratarea excepțiilor pentru a asigura integritatea datelor.
* **Testare Extensivă:** Utilizarea profilurilor Spring pentru a rula aplicația cu baze de date distincte (una pentru testare - H2 în-memory, alta pentru dezvoltare/producție - de ex. MySQL/PostgreSQL). Includerea de `unit-tests` și `integration-tests` pentru o acoperire robustă.
* **Securitate Robustă:** Implementare Spring Security cu autentificare JDBC minimă pentru controlul accesului utilizatorilor și protejarea resurselor platformei.
* **Paginare și Sortare:** Opțiuni intuitive pentru vizualizarea listelor de evenimente și participanți pe pagini și sortarea acestora, îmbunătățind experiența utilizatorului.
* **Logging:** Utilizarea unui sistem de logging (ex: Logback/SLF4J) pentru a înregistra evenimentele aplicației, facilitând depanarea și monitorizarea.

### Faza II: Migrare la Microservicii

Această fază avansată va transforma arhitectura aplicației, punând accent pe următoarele aspecte:

* **Arhitectură Microservicii:** Refactorizarea proiectului monolitic într-o arhitectură distribuită bazată pe microservicii (utilizând Spring Cloud sau K8s pentru orchestrare), cum ar fi servicii dedicate pentru Utilizatori, Evenimente, Înregistrări și Notificări.
* **Configurare Unitară:** Implementarea unei soluții de configurare centralizată pentru toate microserviciile (ex: Spring Cloud Config), asigurând coerența și ușurința în management.
* **Comunicare Inter-servicii:** Asigurarea unei comunicări eficiente, asincrone și securizate între microservicii (ex: cu FeignClient, WebClient, Message Brokers).
* **Service Discovery:** Funcționalitate de descoperire a serviciilor pentru a permite microserviciilor să se înregistreze și să se localizeze reciproc (ex: Eureka, Consul).
* **Scalabilitate și Load-Balancing:** Demonstrarea capacității de scalare orizontală a serviciilor și utilizarea balanțării încărcării pentru distribuirea traficului către instanțe multiple ale serviciilor.
* **Monitorizare, Metrică și Logging Centralizat:** Implementarea unor soluții comprehensive de monitorizare (ex: Actuator, Prometheus, Grafana), colectare de metrici și logging centralizat (ex: ELK Stack) pentru vizibilitate completă asupra sistemului distribuit.
* **Elemente de Securitate pentru Medii Distribuite:** Consolidarea securității în mediul distribuit, inclusiv autentificare și autorizare inter-servicii (ex: OAuth2, JWT).
* **Reziliență:** Asigurarea disponibilității serviciilor în cazul erorilor și implementarea unor strategii de reziliență (ex: Circuit Breaker cu Resilience4j sau Hystrix).
* **Design Patterns Specifice:** Utilizarea de design patterns specifice arhitecturilor distribuite (ex: API Gateway, Saga Pattern, Event Sourcing).

## Tehnologii Utilizate

* **Backend:** Java, Spring Boot, Spring MVC, Spring Data JPA, Spring Security, Spring Cloud (pentru microservicii)
* **Baze de Date:** H2 (pentru testare), MySQL/PostgreSQL (pentru dezvoltare/producție)
* **Build Tool:** Maven
* **Frontend:** Thymeleaf (sau similar pentru MVC), HTML, CSS, JavaScript (minimal)
* **Containerizare/Orchestrație:** Docker, Kubernetes (pentru Faza II, opțional)
* **Version Control:** Git

## Cum să Rulezi Proiectul (Faza I - Exemplu)

1.  **Clonează Repository-ul:**
    ```bash
    git clone https://github.com/[NumeleTauDeUtilizator]/ConnectGather-Microservices.git
    cd ConnectGather-Microservices
    ```
2.  **Configurare Bază de Date:**
    * Pentru dezvoltare, poți folosi baza de date H2 în-memory (configurată implicit în `application-dev.properties` sau similar).
    * Pentru o bază de date persistentă (ex. MySQL), asigură-te că ai o instanță activă și actualizează `application.properties` sau `application-prod.properties` cu detaliile conexiunii.
3.  **Compilează și Rulează:**
    ```bash
    # Folosind Maven
    mvn clean install
    mvn spring-boot:run
    ```
4.  **Accesează Aplicația:**
    Deschide browser-ul și navighează la `http://localhost:8080` (sau portul configurat în `application.properties`).

*(Detalii specifice pentru rularea Fazei II cu microservicii vor fi adăugate pe măsură ce dezvoltarea progresează.)*

## Contribuții

Acest proiect este dezvoltat ca parte a unui proiect academic. Orice sugestii sau contribuții sunt binevenite!

---

Acest `README.md` este acum perfect aliniat cu ideea de "Event Management Platform" și numele de proiect `ConnectGather-Microservices`. Mult succes!
