# Smart Water Complaint Management System

SWCMS contains a Spring Boot API and a React/Vite client for citizen, worker, and admin complaint workflows.

## Local setup

### Quick local run (no MySQL setup)

The screenshot's `Network Error` appears when the API is not running. For a local demo, open one terminal in the project root and run `mvn "-Dspring-boot.run.profiles=local" spring-boot:run`. This starts the API with a local H2 database and creates the development admin `admin@swcms.local` / `LocalAdmin123!`. Then open a second terminal in `swcms-frontend`, run `npm install` once and `npm run dev`, and visit the URL Vite prints (usually `http://localhost:5173`). The local profile and credentials are for development only.

The frontend sends API requests through Vite's `/api` proxy to `http://localhost:8080`. Set `SWCMS_API_PROXY` when your backend runs on another address, or `VITE_API_BASE_URL` for a deployed frontend.

### MySQL setup

Create a MySQL database named `smart_water_db`, then configure these environment variables before starting the backend:

- `DB_URL` (defaults to `jdbc:mysql://localhost:3306/smart_water_db`)
- `DB_USERNAME` (defaults to `root`)
- `DB_PASSWORD` (set this to the local database password)
- `JWT_SECRET` (Base64 encoded random key of at least 32 bytes)
- `ADMIN_EMAIL` and `ADMIN_PASSWORD` (used once to bootstrap the initial admin account)
- `ADMIN_NAME` (optional; defaults to `SWCMS Administrator`)

Start the API with `mvn spring-boot:run`. In `swcms-frontend`, run `npm install` and `npm run dev`.

Uploaded images are stored under the configured `app.upload.dir`. Only JPEG, PNG, and WEBP image signatures are accepted, with a 5 MB per-file limit.

## Main workflows

- Citizens submit complaints, track status history, receive status activity updates, manage their profile, and submit one rating/comment/photo after resolution.
- Workers manage their profile, see assigned work and citizen feedback, submit completion proof, and submit one worker feedback entry per resolved complaint.
- Admins view complaint and feedback analytics, manage workers, reassign work, review overdue cases, and escalate overdue complaints.

## Checks

Run `mvn test` for backend unit tests and the H2-backed citizen/worker/admin API workflow. Run `npm run build` and `npm run lint` in `swcms-frontend` for frontend checks. Manual browser workflows require a running MySQL instance and configured environment variables.
