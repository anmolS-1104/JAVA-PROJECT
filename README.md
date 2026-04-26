# 🗂️ Intelligent Complaint Resolution System

A full-stack complaint routing and tracking application built with a **Java Spring Boot** backend and a **JavaFX** frontend. Complaints are automatically categorized, assigned a priority level, and routed to the appropriate department.

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
| MySQL | — | Persistent storage |
| JavaFX | 21.0.2 | Desktop UI (optional client) |
| Jackson | — | JSON serialization |
| Maven | — | Build tool |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| JavaFX | — | Desktop UI (FXML-based screens) |
| HTML / CSS / JavaScript | — | Web-facing pages |

---

## Project Structure

```
Directory structure:
└── anmols-1104-java-project/
    ├── src/
    │   └── main/
    │       ├── java/
    │       │   └── com/
    │       │       └── complaint/
    │       │           └── system/
    │       │               ├── BackendApplication.java
    │       │               ├── ClientApp.java
    │       │               ├── Launcher.java
    │       │               ├── controllers/
    │       │               │   ├── AgentController.java
    │       │               │   ├── AgentDashboardController.java
    │       │               │   ├── AnalyticsController.java
    │       │               │   ├── AuthController.java
    │       │               │   ├── ComplaintController.java
    │       │               │   ├── ComplaintRestController.java
    │       │               │   ├── HistoryController.java
    │       │               │   ├── LoginController.java
    │       │               │   ├── RegisterController.java
    │       │               │   └── UserController.java
    │       │               ├── dao/
    │       │               │   ├── AgentDAO.java
    │       │               │   ├── AgentDAOImpl.java
    │       │               │   ├── ComplaintDAO.java
    │       │               │   ├── ComplaintDAOImpl.java
    │       │               │   ├── UserDAO.java
    │       │               │   └── UserDAOImpl.java
    │       │               ├── dto/
    │       │               │   ├── ComplaintDTO.java
    │       │               │   ├── LoginRequest.java
    │       │               │   └── LoginResponse.java
    │       │               ├── model/
    │       │               │   ├── Agent.java
    │       │               │   ├── Complaint.java
    │       │               │   ├── Department.java
    │       │               │   ├── FinanceDepartment.java
    │       │               │   ├── LogisticsDepartment.java
    │       │               │   ├── TechnicalDepartment.java
    │       │               │   └── User.java
    │       │               ├── service/
    │       │               │   ├── AgentService.java
    │       │               │   ├── ClassificationEngine.java
    │       │               │   ├── ComplaintService.java
    │       │               │   ├── ResolutionManager.java
    │       │               │   └── UserService.java
    │       │               └── util/
    │       │                   ├── ApiClient.java
    │       │                   ├── DBConnection.java
    │       │                   ├── FileLogger.java
    │       │                   └── Session.java
    │       └── resources/
    │           ├── agent_dashboard.fxml
    │           ├── analytics.fxml
    │           ├── application.properties
    │           ├── dashboard.fxml
    │           ├── history.fxml
    │           ├── login.fxml
    │           ├── register.fxml
    │           └── styles.css
    └── .ai/
        └── mcp/
            └── mcp.json


complaints_log.txt
pom.xml
README.md
```

---

## Getting Started

### Prerequisites

- Java 21 — https://adoptium.net/
- Maven 3.8+ — https://maven.apache.org/
- MySQL — running locally

### Database Setup

Open MySQL and run:

CREATE DATABASE complaints_db;
USE complaints_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    phone VARCHAR(50),
    role VARCHAR(20) DEFAULT 'CUSTOMER'
);

CREATE TABLE complaints (
    id INT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    priority VARCHAR(50),
    department VARCHAR(50),
    status VARCHAR(50),
    user_id INT,
    notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE agents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    department VARCHAR(50)
);

### Backend Setup

1. Clone the repository:
git clone -b rescue-backup https://github.com/anmolS-1104/JAVA-PROJECT.git
cd JAVA-PROJECT

2. Update DBConnection.java with your MySQL credentials:
private static final String URL = "jdbc:mysql://localhost:3306/complaints_db";
private static final String USER = "root";
private static final String PASSWORD = "your_password";

3. Compile and run:
cd src/main/java
mvn spring-boot:run

### Frontend Setup

cd src/main/java
Open login.fxml in IntelliJ 
run Launcher.java

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
| GET | /api/complaints/department/{dept} | Get complaints by department |
| GET | /api/complaints/user/{userId} | Get complaints by user |
| GET | /api/complaints/user/{userId}/analytics | Get analytics for a user |
| GET | /api/complaints/filter | Filter complaints (dept/status/priority/sortBy) |
| PUT | /api/complaints/{id}/status | Update complaint status |
| PUT | /api/complaints/{id}/notes | Update resolution notes |
| DELETE | /api/complaints/{id} | Delete a complaint |

---

## Contributing

1. Fork the repository
2. Create a feature branch: git checkout -b feature/your-feature
3. Commit your changes: git commit -m "Add your feature"
4. Push to the branch: git push origin feature/your-feature
5. Open a Pull Request

---

Built with Java 21, Spring Boot 3 & React 19


