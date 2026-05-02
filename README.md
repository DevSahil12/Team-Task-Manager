# TaskFlow — Team Task Manager

A full-stack collaborative task management web application built with **React + Spring Boot**.

---

## Live Demo
> Deploy to Railway following the steps below.
> https://team-task-manager-production-a573.up.railway.app/

---

## Features

### Authentication
- Signup with Name, Email, Password
- JWT-based secure login
- Protected routes — unauthenticated users are redirected to login

### Role-Based Access Control
| Feature | Admin | Member |
|---|---|---|
| Create tasks | ✅ | ❌ |
| Edit / Delete tasks | ✅ | ❌ |
| Add / Remove members | ✅ | ❌ |
| Update task status | ✅ | Only assigned tasks |
| View project tasks | ✅ | ✅ |
| View project members | ✅ | ✅ |

### Project Management
- Create projects (creator is automatically **Admin**)
- Admin can add members by **searching their email**
- Admin can remove members
- Admin can assign roles (Admin / Member)
- Members can view all projects they belong to

### Task Management
- Tasks have: Title, Description, Due Date, Priority (Low/Medium/High)
- Status workflow: **To Do → In Progress → Done**
- Tasks are assigned to project members
- Admin: full create/edit/delete/assign control
- Member: update status of their own assigned tasks only
- Overdue badge auto-calculated

### Dashboard
- Total tasks, In Progress, Completed, Overdue stats
- Tasks Per User chart (shown when you are Admin of any project)
- Recent tasks list

---

## Tech Stack

**Frontend:** React 18, React Router v6, Axios, Vite  
**Backend:** Spring Boot 3.2, Spring Security, Spring Data JPA  
**Database:** PostgreSQL (production) / H2 (local dev)  
**Auth:** JWT (jjwt 0.12.3)  
**Deployment:** Railway

---

## Local Development

### Prerequisites
- Node.js 18+
- Java 21+
- Maven 3.9+

### Backend

```bash
cd backend

# Optional: set env vars for local Postgres, or use H2 (default)
export JWT_SECRET=YourSuperSecretKeyAtLeast256BitsLong!!!!
export ALLOWED_ORIGINS=http://localhost:5173

mvn spring-boot:run
# Backend starts on http://localhost:8080
```

### Frontend

```bash
cd frontend

cp .env.example .env
# Set VITE_API_URL=http://localhost:8080/api in .env

npm install
npm run dev
# Frontend starts on http://localhost:5173
```

---

## Deployment (Railway)

### Step 1 — Create a Railway project
1. Go to [railway.app](https://railway.app) and create a new project
2. Add a **PostgreSQL** plugin to the project

### Step 2 — Deploy Backend

1. Add a new service → **Deploy from GitHub repo** → select the `backend` folder
2. Set environment variables:
   ```
   DATABASE_URL=postgresql://<user>:<pass>@<host>:<port>/<db>
   DB_USERNAME=<postgres_user>
   DB_PASSWORD=<postgres_password>
   DB_DRIVER=org.postgresql.Driver
   HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
   JWT_SECRET=<random-256bit-string>
   ALLOWED_ORIGINS=https://<your-frontend-url>.railway.app
   ```
3. Railway will use `railway.toml` → `mvn clean package -DskipTests` to build

### Step 3 — Deploy Frontend

1. Add another service → **Deploy from GitHub repo** → select the `frontend` folder
2. Set environment variable:
   ```
   VITE_API_URL=https://<your-backend-url>.railway.app/api
   ```
3. Railway will use `railway.toml` → `npm run build`

### Step 4 — Verify
- Visit your frontend Railway URL
- Register a new account → you are automatically Admin of any project you create
- Invite teammates by their email

---

## API Endpoints

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT |

### Users
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users/search?email=x` | Search user by email |
| GET | `/api/users/me` | Current user profile |

### Projects
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/projects` | All projects for current user |
| POST | `/api/projects` | Create project (creator = Admin) |
| GET | `/api/projects/:id` | Get project details |
| GET | `/api/projects/:id/members` | List members |
| POST | `/api/projects/:id/members` | Add member (Admin only) |
| DELETE | `/api/projects/:id/members/:userId` | Remove member (Admin only) |

### Tasks
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/tasks/my` | Tasks assigned to current user |
| GET | `/api/tasks/project/:projectId` | All tasks in a project |
| POST | `/api/tasks` | Create task (Admin only) |
| PATCH | `/api/tasks/:id` | Update task |
| DELETE | `/api/tasks/:id` | Delete task (Admin only) |

### Dashboard
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/dashboard` | Stats + tasks per user |

---

## Project Structure

```
team-task-manager/
├── backend/
│   ├── src/main/java/com/taskmanager/
│   │   ├── TaskManagerApplication.java
│   │   ├── controller/         # REST controllers
│   │   ├── model/              # JPA entities
│   │   ├── repository/         # Spring Data repos
│   │   ├── dto/                # Request/Response DTOs
│   │   ├── security/           # JWT + Spring Security
│   │   └── config/             # SecurityConfig, ExceptionHandler
│   ├── src/main/resources/application.properties
│   ├── pom.xml
│   └── railway.toml
└── frontend/
    ├── src/
    │   ├── api/client.js       # Axios API layer
    │   ├── components/Layout.jsx
    │   ├── context/AuthContext.jsx
    │   ├── pages/
    │   │   ├── LoginPage.jsx
    │   │   ├── RegisterPage.jsx
    │   │   ├── DashboardPage.jsx
    │   │   ├── ProjectsPage.jsx
    │   │   ├── ProjectDetailPage.jsx
    │   │   └── TasksPage.jsx
    │   └── utils/badges.jsx
    ├── package.json
    └── railway.toml
```
