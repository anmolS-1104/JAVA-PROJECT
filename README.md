🗂️ Complaint Management System
A full-stack complaint routing and tracking application built with a Java Spring Boot backend and a React frontend. Complaints are automatically categorized, assigned a priority level, and routed to the appropriate department.

📋 Table of Contents

Overview
Tech Stack
Project Structure
Getting Started

Prerequisites
Backend Setup
Frontend Setup


Complaint Log Format
API Overview
Contributing


Overview
This system allows users to submit complaints which are then:

Classified by type (e.g. payment issue, delivery issue)
Prioritized (e.g. NORMAL, HIGH, URGENT)
Routed to the responsible department (e.g. FinanceDepartment, LogisticsDepartment)

Complaint history is persisted to a MySQL database and logged to a flat file (complaints_log.txt) for auditing purposes.

Tech Stack
Backend
TechnologyVersionPurposeJava21Core languageSpring Boot3.2.4REST API & application frameworkSpring Data JPA—Database ORMMySQL—Persistent storageJavaFX21.0.2Desktop UI (optional client)Jackson—JSON serializationMaven—Build tool
Frontend
TechnologyVersionPurposeReact19UI frameworkReact Scripts5.0.1Build & dev toolingNode.js / npm—Package management

Project Structure
/
├── src/                        # Java Spring Boot source
│   └── main/
│       ├── java/
│       │   └── com/complaint/system/
│       │       ├── AppLauncher.java       # Application entry point
│       │       ├── controller/            # REST controllers
│       │       ├── service/               # Business logic & routing
│       │       ├── model/                 # JPA entities
│       │       └── repository/            # Spring Data repositories
│       └── resources/
│           ├── application.properties     # DB config, server port, etc.
│           └── fxml/                      # JavaFX layouts (if used)
│
├── ai-frontend/                # React frontend
│   ├── public/
│   └── src/
│       ├── components/         # UI components
│       ├── App.js
│       └── index.js
│
├── complaints_log.txt          # Flat-file audit log of all complaints
├── pom.xml                     # Maven build configuration
├── package.json                # Frontend dependencies
└── README.md

Getting Started
Prerequisites

Java 21 — Download
Maven 3.8+ — Download
Node.js 18+ & npm — Download
MySQL — running locally or via Docker


Backend Setup

Clone the repository

bash   git clone <repo-url>
   cd <repo-folder>

Configure the database
Edit src/main/resources/application.properties:

properties   spring.datasource.url=jdbc:mysql://localhost:3306/complaint_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update

Build and run

bash   mvn clean install
   mvn spring-boot:run
The API will be available at http://localhost:8080.

Frontend Setup

Navigate to the frontend directory

bash   cd ai-frontend

Install dependencies

bash   npm install

Start the development server

bash   npm start
The app will open at http://localhost:3000.

Complaint Log Format
Complaints are appended to complaints_log.txt in the following pipe-delimited format:
<complaint description> | <priority> | <assigned department>
Example entries:
payment failed | NORMAL | FinanceDepartment
delivery not received | NORMAL | LogisticsDepartment
FieldDescriptionComplaint DescriptionShort text describing the issuePriorityNORMAL, HIGH, or URGENTDepartmentThe team responsible for resolution

API Overview
MethodEndpointDescriptionPOST/api/complaintsSubmit a new complaintGET/api/complaintsRetrieve all complaintsGET/api/complaints/{id}Get a specific complaintGET/api/complaints/department/{dept}Filter by department



