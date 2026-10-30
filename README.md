# Digital Library Management System

An enterprise-grade, secure, and college-mini-project-appropriate **Digital Library Management System** developed with **Java 17, Spring Boot 3, PostgreSQL (Supabase), Spring Security 6 (JWT + BCrypt), and Flutter (Dart + Riverpod)**.

The frontend user application has been designed specifically to match the modern dark-slate UI/UX layout depicted in the project blueprint:
- Dark slate/charcoal dashboard (`#131B24`) with warm amber accents (`#EBB036`) and soft teal highlights (`#26A69A`).
- Left vertical navigation icon rail.
- "Previous Reading" carousel with live progress bars and page counter.
- "Subjects section" cards (Science 1.2k, Arts 1.8k, Commerce 230, Design 80, Cooking 180, Others 900).
- "New books" row, "Popular books" grid, and "Writers and Authors" listing.
- Secure in-app PDF reader with bookmarking and progress tracking.

---

## 1. Project Directory Structure

```
digital-library-system/
├── .env.example
├── README.md
├── database/
│   ├── 01_schema.sql         # 12 Tables, constraints, full-text search, triggers
│   └── 02_sample_data.sql     # Seed data matching UI mockup (Abhishek Saha, books, etc.)
├── backend/
│   ├── pom.xml               # Maven configuration with Spring Boot 3.2.3 & Java 17
│   └── src/
│       ├── main/java/com/library/
│       │   ├── DigitalLibraryApplication.java
│       │   ├── config/       # SecurityConfig, CORS
│       │   ├── controller/   # Auth, Book, User, Request, Admin REST controllers
│       │   ├── dto/          # Data Transfer Objects with Jakarta Validation
│       │   ├── entity/       # 12 JPA Entities & Enums
│       │   ├── exception/    # Custom exceptions & GlobalExceptionHandler
│       │   ├── repository/   # Spring Data JPA repositories with custom queries
│       │   ├── security/     # JwtTokenProvider, JwtFilter, UserPrincipal
│       │   └── service/      # PermissionService, StorageService, BookService, etc.
│       └── test/             # Unit and integration test suites
└── frontend/
    ├── user_app/             # Flutter User App (Riverpod, Dark Slate Theme)
    │   ├── pubspec.yaml
    │   └── lib/
    │       ├── core/         # AppTheme, ApiClient, ApiConstants
    │       ├── data/         # Models & Repositories
    │       ├── providers/    # Riverpod state notifiers
    │       └── presentation/ # Dashboard, BookDetail, Reader, History, Profile
    └── admin_app/            # Flutter Administrator App
        ├── pubspec.yaml
        └── lib/
            ├── core/         # AdminTheme, AdminApiClient, AdminConstants
            ├── data/         # Models
            ├── providers/    # Riverpod providers
            └── presentation/ # Metrics, Catalog, User Governance, Requests, Logs
```

---

## 2. Technology Stack & Selected Versions

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Backend Runtime** | Java OpenJDK | 17 LTS | Central business logic, OOP, strict validation |
| **Framework** | Spring Boot | 3.2.3 | REST API services & dependency injection |
| **Security** | Spring Security | 6.2.2 | Token-based stateless authentication |
| **Token Format** | JJWT | 0.12.5 | HMAC-SHA256 signed access & refresh tokens |
| **Database** | PostgreSQL | 15+ (Supabase) | Relational database persistence |
| **ORM** | Spring Data JPA / Hibernate | 6.4.x | Repository abstractions and transactions |
| **Document Storage** | Supabase Storage | S3 API | Protected PDF streaming & public cover CDN |
| **Frontend Apps** | Flutter & Dart | 3.x / Dart 3.x | User & Admin cross-platform applications |
| **State Management**| Flutter Riverpod | 2.4.9 | Reactive dependency injection & state cache |

---

## 3. Step-by-Step Setup & Execution

### Step 1: Database Setup (PostgreSQL / Supabase)
1. Open your **Supabase Dashboard** or local PostgreSQL instance (`psql`).
2. Run the database schema DDL script:
   ```sql
   \i database/01_schema.sql
   ```
3. Populate the seed dataset matching the UI layout:
   ```sql
   \i database/02_sample_data.sql
   ```

### Step 2: Configure Environment Variables
Copy `.env.example` to `.env` or set environment variables:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://db.YOUR_SUPABASE_HOST:5432/postgres?sslmode=require
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=YourDatabasePassword
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
export SUPABASE_URL=https://YOUR_PROJECT_REF.supabase.co
export SUPABASE_ANON_KEY=your_supabase_anon_key
export SUPABASE_SERVICE_ROLE_KEY=your_supabase_service_role_key
```

### Step 3: Run the Spring Boot Backend
Navigate to `digital-library-system/backend`:
```bash
# Run unit and integration tests
mvn test

# Run the backend REST service
mvn spring-boot:run
```
The REST API will launch on `http://localhost:8080`.  
Swagger UI documentation: `http://localhost:8080/swagger-ui.html`.

### Step 4: Run the Flutter User App
Navigate to `digital-library-system/frontend/user_app`:
```bash
flutter pub get
flutter run -d chrome # or macos, windows, linux, android, ios
```

### Step 5: Run the Flutter Administrator App
Navigate to `digital-library-system/frontend/admin_app`:
```bash
flutter pub get
flutter run -d chrome # or desktop target
```

---

## 4. Test Credentials

| Account | Email | Password | Role | Features to Test |
| :--- | :--- | :--- | :--- | :--- |
| **Chief Librarian** | `admin@library.com` | `Password@123` | `ROLE_ADMIN` | Metric dashboard, book CRUD, user suspension, approving requests |
| **Abhishek Saha** | `abhishek.saha@library.com` | `Password@123` | `ROLE_USER` | Full dashboard view, in-app reading, download permitted PDFs |
| **Sarah Jenkins** | `sarah.jenkins@library.com` | `Password@123` | `ROLE_USER` | Standard reading & downloading, submitting book requests |
| **Marcus Vance** | `marcus.vance@library.com` | `Password@123` | `ROLE_USER` | Has active `BLOCK_DOWNLOADING` sanction; verify download denial |
| **John Suspended** | `john.suspended@library.com` | `Password@123` | `ROLE_USER` | Suspended account; verify immediate `403 AccountSuspendedException` |

---

## 5. Sample API Requests & Responses

### 1. User Authentication
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"abhishek.saha@library.com","password":"Password@123"}'
```
**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Authentication successful.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresInMs": 86400000,
    "userId": 2,
    "name": "Abhishek Saha",
    "email": "abhishek.saha@library.com",
    "role": "ROLE_USER",
    "avatarUrl": "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d"
  }
}
```

### 2. Retrieve User Dashboard (Powers UI Mockup)
```bash
curl -X GET http://localhost:8080/api/v1/me/dashboard \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```
**Response (200 OK)** returns the `previousReading`, `subjects`, `newBooks`, `popularBooks`, and `writersAndAuthors` sections.

### 3. Request Authorized PDF Stream
```bash
curl -X POST http://localhost:8080/api/v1/books/1/read \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```
**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Read authorization granted. Temporary stream URL generated.",
  "data": {
    "accessUrl": "https://xyzcompany.supabase.co/storage/v1/object/sign/library-pdfs/steal_like_an_artist.pdf?expiresIn=900&action=read",
    "expiresInSeconds": 900,
    "bookTitle": "Steal Like an Artist",
    "action": "READ"
  }
}
```

---

## 6. Demonstration Steps for Project Evaluation

1. **Demonstrate User Experience**:
   - Log in as **Abhishek Saha**.
   - Show the dashboard matching the design attachment: the "Previous Reading" carousel with 75% progress on *Steal Like an Artist*, subject chips (Science, Arts, Commerce, Design, Cooking in amber, Others), "New books", "Popular books", and "Writers and Authors".
   - Click *Steal Like an Artist*, open the **In-App PDF Reader**, turn pages to 125, and save bookmark.
   - Return to the dashboard and observe the reading progress dynamically updated.
2. **Demonstrate Permission Denial**:
   - Log in as **Marcus Vance** (`marcus.vance@library.com`).
   - Attempt to download a PDF document.
   - Verify that the backend rejects the action with `403 Forbidden: AccountRestrictedException` explaining the active timed policy block.
3. **Demonstrate Account Suspension**:
   - Attempt login as **John Suspended** (`john.suspended@library.com`).
   - Observe immediate access denial with `AccountSuspendedException`.
4. **Demonstrate Request Elevation Workflow**:
   - In the User App, click on a restricted title (*Murder on the Orient Express*).
   - Submit an Access Elevation Request for academic research.
   - Log into the **Administrator App** as **Chief Librarian Admin**.
   - Navigate to **User Requests**, inspect the submission, and click **Grant Access (30d)**.
   - Verify that the backend automatically provisions a valid `book_permissions` record.
   - Return to the User App; verify that Abhishek Saha can now read the restricted book.

