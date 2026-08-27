# Drone-Based Farm Service Management System

## Backend Setup

This section explains how to run the DMFS backend after cloning the repository.

---

## Requirements

Install:

- Git
- Java JDK 21
- Docker Desktop

Verify the installations:

```bash
git --version
java -version
javac -version
docker --version
docker compose version

Java must be JDK 21.

1. Clone the Repository
git clone <REPOSITORY_URL>

Enter the project:

cd drone-farm-system-management

Switch to develop:

git checkout develop

Pull the latest changes:

git pull origin develop
2. Start Docker

Make sure Docker Desktop is running.

From the project root:

docker compose up -d

Check the containers:

docker compose ps

PostgreSQL/PostGIS and RabbitMQ should be running.

3. Run the Backend

Go to the backend directory:

cd backend/dmfs-backend
Windows

Build the project:

.\mvnw.cmd clean install

Run the backend:

.\mvnw.cmd spring-boot:run
Linux/macOS
./mvnw clean install

Run:

./mvnw spring-boot:run
4. Backend

The backend runs on:

http://localhost:8080
5. Stop the Backend

Press:

Ctrl + C
6. Stop Docker Services

From the project root:

docker compose down

Start them again:

docker compose up -d
7. Useful Commands

Check Docker containers:

docker compose ps

View Docker logs:

docker compose logs -f

Check Maven version:

Windows
.\mvnw.cmd -version
Linux/macOS
./mvnw -version
Development Workflow

Do not work directly on main.

Use feature branches:

develop
   ↓
feature/<feature-name>
   ↓
Pull Request
   ↓
develop

Create a feature branch:

git checkout develop
git pull origin develop
git checkout -b feature/<feature-name>

Example:

git checkout -b feature/authentication

Commit changes:

git add .
git commit -m "feat: implement authentication"

Push the feature:

git push -u origin feature/authentication
