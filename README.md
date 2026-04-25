# 🗂️ Complaint Management System

A full-stack complaint routing and tracking application built with a **Java Spring Boot** backend and a **React** frontend. Complaints are automatically categorized, assigned a priority level, and routed to the appropriate department.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Complaint Log Format](#complaint-log-format)
- [API Overview](#api-overview)
- [Contributing](#contributing)

---

## Overview

This system allows users to submit complaints which are then:

- **Classified** by type (e.g. payment issue, delivery issue)
- **Prioritized** (e.g. `NORMAL`, `HIGH`, `URGENT`)
- **Routed** to the responsible department (e.g. `FinanceDepartment`, `LogisticsDepartment`)
- **Deletable** by the user who submitted them

---

## Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Core language |
| Spring Boot | 3.2.4 | REST API & application framework |
| Spring Data JPA | — | Database ORM |
| MySQL | — | Persistent storage |
| JavaFX | 21.0.2 | Desktop UI (optional client) |
| Jackson | — | JSON serialization |
| Maven | — | Build tool |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| React | 19 | UI framework |
| React Scripts | 5.0.1 | Build & dev tooling |
| Node.js / npm | — | Package management |

---

## Project Structure

```
BACKEND (FULL)/
└── src/
    └── com/
        ├── icrs/
        │   ├── controller/
        │   │   ├── ComplaintController.java
        │   │   └── UserController.java
        │   ├── dao/
        │   │   ├── ComplaintDAO.java
        │   │   ├── ComplaintDAOImpl.java
        │   │   ├── UserDAO.java
        │   │   └── UserDAOImpl.java
        │   ├── model/
        │   │   ├── Complaint.java
        │   │   └── User.java
        │   ├── service/
        │   │   ├── ComplaintService.java
        │   │   └── UserService.java
        │   └── util/
        │       └── DBConnection.java
        └── Main.java

Frontend/
└── webapp/
    └── src/
        ├── components/
        ├── App.js
        └── index.js

complaints_log.txt
pom.xml
package.json
README.md
```

---

## Getting Started

### Prerequisites

- Java 21 — https://adoptium.net/
- Maven 3.8+ — https://maven.apache.org/
- Node.js 18+ & npm — https://nodejs.org/
- MySQL — running locally

### Database Setup

Open MySQL and run:

CREATE DATABASE complaints_db;

USE complaints_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20)
);

CREATE TABLE complaints (
    id INT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255),
    priority VARCHAR(20),
    department VARCHAR(50),
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

### Backend Setup

1. Clone the repository:
git clone https://github.com/anmolS-1104/JAVA-PROJECT.git
cd JAVA-PROJECT

2. Update DBConnection.java with your MySQL credentials:
private static final String URL = "jdbc:mysql://localhost:3306/complaints_db";
private static final String USER = "root";
private static final String PASSWORD = "your_password";

3. Compile and run:
cd "BACKEND (FULL)/src"
curl -o mysql-connector.jar "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"
javac (Get-ChildItem -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName)
java -cp ".;mysql-connector.jar" com.Main

### Frontend Setup

cd Frontend/webapp
npm install
npm start

---

## Complaint Log Format

Complaints are logged to complaints_log.txt in pipe-delimited format:

<description> | <priority> | <department>

Example:
payment failed | NORMAL | FinanceDepartment
delivery not received | NORMAL | LogisticsDepartment

---

## API Overview

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/complaints | Submit a new complaint |
| GET | /api/complaints | Get all complaints |
| GET | /api/complaints/{id} | Get a specific complaint |
| DELETE | /api/complaints/{id} | Delete your own complaint |
| GET | /api/complaints/department/{dept} | Filter by department |

---

## Contributing

1. Fork the repository
2. Create a feature branch: git checkout -b feature/your-feature
3. Commit your changes: git commit -m "Add your feature"
4. Push to the branch: git push origin feature/your-feature
5. Open a Pull Request

---

Built with Java 21, Spring Boot 3 & React 19


