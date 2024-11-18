# Salary Management System

This project is a simple CRUD (Create, Read, Update, Delete) application built with **Java Spring Boot** on the backend
and **React** on the frontend. While it’s not a complex application, it is designed to demonstrate practical skills in
integrating various tools and technologies, including **Redis**, **Kafka/Zookeeper**, **PostgreSQL**, **Docker**, *
*Docker Compose**, **CI/CD pipelines**, and **AWS**.

---

## Features

- **Backend (Spring Boot):**
    - CRUD operations for managing salary entries.
    - REST API endpoints for frontend integration.
    - Integration with **Redis** for caching and improved performance.
    - Kafka messaging for asynchronous communication (ready for advanced use cases).
    - **PostgreSQL** as the relational database.
    - Configurable environment variables for flexibility.

- **Frontend (React):**
    - Displays salary entries in a user-friendly, paginated table.
    - Features include search, pagination, and actions for adding, editing, and deleting entries.
    - API integration with the backend.

- **Infrastructure:**
    - Fully containerized using **Docker** and orchestrated with **Docker Compose**.
    - **Redis** for caching and **Kafka/Zookeeper** for messaging.
    - Pre-configured health checks for all services.

- **Deployment & CI/CD:**
    - Configured to demonstrate **AWS** deployment capabilities through a CI/CD pipeline.
    - Deployment-ready Docker images for all services.

---

## Getting Started

### Prerequisites

Ensure you have the following installed:

- **Docker** & **Docker Compose**
- **Node.js** (if running the frontend outside of Docker)
- **Java 21** (if running the backend outside of Docker)

### Environment Variables

Before starting, create a `.env` file at the project root with the following keys:

```env
DB_NAME=myapp_dev
DB_USERNAME=your_postgres_username
DB_PASSWORD=your_postgres_password

SPRING_PROFILE=dev
API_URL=http://localhost:8080
KAFKA_CONSUMER_GROUP_ID=my-group
```

---

## Running the Project

### Using Docker Compose

1. **Start the application:**

```bash
docker-compose up --build
```

2. **Stop the application:**

```bash
docker-compose down
```

3. **Clean up volumes, images, and orphaned containers:**

```bash
docker-compose down --volumes --rmi all --remove-orphans
```

4. **Access the application:**

* Frontend: http://localhost
* Backend: http://localhost:8080/api/v1

---

## Project Structure

### Backend (``/backend``)

* Built with **Spring Boot**
* Follows standard MVC
    * ``Controller`` for handling HTTP request
    * ``Service`` for business logic
    * ``Repository`` for database access

### Frontend (``/frontend``)

* Built with **React**
* Manages UI for salary CRUD operations

### Docker Compose Setup (``docker-compose.yml``)

* Orchestrates containers for all services:
    * **Backend**
    * **Frontend**
    * **Redis**
    * **PostgreSQL**
    * **Kafka**
    * **Zookeeper**

---

## Technologies Used

* **Backend:**
    * Spring Boot (REST, JPA, Hibernate)
    * Redis (Caching)
    * Kafka (Messaging)
    * PostgreSQL (Database)
* **Frontend:**
    * React
    * Nginx (as a lightweight web server)
* **DevOps:**
    * Docker & Docker Compose
    * CI/CD pipelines (GitHub Actions, AWS configuration)

---

## CI/CD Configration

This project includes a **GitHub Actions** pipeline that can demonstrate deployment to **AWS**. While AWS setup isn't
included
in this repository, the pipeline can be extended with appropriate configurations for services like **ECS**, **S3**, or *
*Elastic
Beanstalk**.
---
## Future Improvements
While this project is designed as a simple showcase, it can be extended with:
* Advanced Kafka use cases like event-driven architectures.
* Role-based authentication and authorization.
* Real-time updates using WebSockets.
* Deployment to a fully configured AWS environment.