# Banking System Simulator — Microservices Architecture  
(Spring Boot + Spring Cloud + MongoDB + Docker)

## 📌 Project Overview
This project implements a **Banking System using Microservices**, following real-world distributed architecture principles. Each business domain is developed as an independent service with its own database, deployed and managed via Docker.

### ✔ Microservices included:
- **Eureka Server** (Service Discovery)
- **API Gateway** (Routing & Entry Point)
- **Account Service** (Account CRUD + balance updates)
- **Transaction Service** (Deposit, Withdraw, Transfer)
- **Notification Service** (Simulated email/log notifications)

### ✔ Tech Stack
- Java 17  
- Spring Boot 3.x  
- Spring Cloud (Eureka, Gateway, Config patterns)  
- MongoDB  
- RestTemplate (with LoadBalancer)  
- Resilience4j (Circuit Breaker)  
- SLF4J + MDC (Correlation ID Logging)  
- Docker & Docker Compose  
- JUnit5 + Mockito  

---

## 🏛 Architecture Diagram  
### High-Level Microservices Architecture  
(Place your PNG diagram here after exporting from PlantUML)

/architecture.png

pgsql
Copy code

### PlantUML Source (use for PNG export)
```plantuml
@startuml
skinparam componentStyle rectangle

title Banking System Simulator — Microservices

actor Client

node "API Gateway\n(Spring Cloud Gateway)" as gateway
node "Eureka Server\n(Service Discovery)" as eureka

cloud "banking-net (Docker)" {
  component "Account Service\n(accounts_db)\n(port:8081)" as account
  component "Transaction Service\n(transactions_db)\n(port:8082)" as transaction
  component "Notification Service\n(port:8083)" as notification
}

Client --> gateway : /api/accounts/**\n/api/transactions/**
gateway --> account : Route -> /api/accounts/**
gateway --> transaction : Route -> /api/transactions/**
transaction --> account : REST → Update Balance
transaction --> notification : REST → Send Notification
gateway --> eureka : Register
account --> eureka : Register
transaction --> eureka : Register
notification --> eureka : Register

@enduml
📁 Folder Structure (Deliverables)
pgsql
Copy code
banking-system-microservice/
│
├── eureka-server/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
│
├── api-gateway/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
│
├── account-service/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
│
├── transaction-service/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
│
├── notification-service/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
│
├── docker-compose.yml
└── README.md
✔ Source code for each microservice
✔ Dockerfile for each microservice
✔ README with setup + architecture (this file)

⚙️ How to Run the Entire System (Docker Compose)
1️⃣ Ensure Docker Desktop is running
2️⃣ From the project root, run:
bash
Copy code
docker-compose up -d
3️⃣ Verify services
Eureka Dashboard → http://localhost:8761

API Gateway → http://localhost:8085

4️⃣ Stop all services:
bash
Copy code
docker-compose down
🧪 API Endpoints
Account Service
Method	Endpoint	Description
POST	/api/accounts	Create account
GET	/api/accounts/{accNo}	Get account
PUT	/api/accounts/{accNo}/balance	Update balance
PUT	/api/accounts/{accNo}/status	Update status

Transaction Service
Method	Endpoint	Description
POST	/api/transactions/deposit	Deposit
POST	/api/transactions/withdraw	Withdraw
POST	/api/transactions/transfer	Transfer
GET	/api/transactions/account/{accNo}	Transaction history

Notification Service
Method	Endpoint
POST	/api/notifications/send

🔁 Example Test Flow (via Gateway)
1️⃣ Create Account
POST → http://localhost:8085/api/accounts
Body:

json
Copy code
{
  "accountNumber": "ACC1001",
  "holderName": "Sachin",
  "balance": 5000
}
2️⃣ Deposit
POST →

bash
Copy code
http://localhost:8085/api/transactions/deposit?accountNumber=ACC1001&amount=500
3️⃣ Withdraw
POST →

bash
Copy code
http://localhost:8085/api/transactions/withdraw?accountNumber=ACC1001&amount=200
4️⃣ Transfer
POST →

bash
Copy code
http://localhost:8085/api/transactions/transfer?sourceAccount=ACC1001&destinationAccount=ACC2001&amount=300
5️⃣ Check Transaction History
GET →

bash
Copy code
http://localhost:8085/api/transactions/account/ACC1001
🛡 Resilience & Logging
Circuit breaker added for Account Service calls

Fallback methods added for failures

Distributed logs using SLF4J + Correlation ID (X-Correlation-Id)

MDC used to trace requests across microservices

🧪 Unit Tests
Run tests per service:

bash
Copy code
mvn test
Includes:

Service layer tests

Mockito mocks

Dependency injection tests

👨‍💻 Contributors
Sachin Birajdar — Developer
Project built for academic + portfolio purposes.
