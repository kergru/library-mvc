# Library Spring MVC application - example implementation for OAuth2 authentication

This is a simple library management system implemented using Spring MVC, Spring Security, and OAuth2.
It provides a web interface with user authentication and authorization using OAuth2.
Project is divided into three modules:

- library-commons - common classes and models
- library-backend - backend service configured as OAuth2 resource server
- library-frontend - frontend web application configured as OAuth2 client

Project uses Keycloak as OAuth2 provider and MySQL as database which will be started using Docker Compose.

## Project Structure

| Module               | Description                                             |
|----------------------|---------------------------------------------------------|
| **library-commons**  | Shared domain classes, DTOs, and utilities              |
| **library-backend**  | backend service acting as an OAuth2 resource server     |
| **library-frontend** | MVC frontend web application acting as an OAuth2 client | |

## Components

| Layer                                        | Component                                            | Purpose                                                           |
|----------------------------------------------|------------------------------------------------------|-------------------------------------------------------------------|
| **Frontend (`library-frontend`)**            |                                                      |
| -- OAuth2LoginSecurityChain                  | Spring SecurityChain + OAuth2 Client                 | Authenticates user via OIDC, <br/>calls backend with Bearer Token |
| -- LibraryController, LibraryAdminController | Spring Boot MVC + Thymeleaf                          | Renders Library UI                                                |
| -- LibraryApiRestController                  | RestController                                       | Handles Ajax requests to create loans or adds new Library users   |
| -- LibraryBackendClient                      | OAuth2 Client to execute requests to library-backend | Executes requests to library-backend                              |
| -- KeycloakAdminClient                       | OAuth2 Client to execute requests to keycloak admin  | Executes requests to keycloak to add user                         |
| **Backend (`library-backend`)**              | Spring Boot Resource Server                          | Validates JWT (Access Token), <br/>valididate Signatur über JWKS  |
| **Authorization Server (Docker)**            | Keycloak                                             | Executes user login, provides tokens and public keys via JWKS     |
| **Database (Docker)**                        | MySQL                                                | Stores library and Keycloak data                                  |
| **User (Browser)**                           | Webbrowser                                           | Access to protected data                                          |


## Architecture Diagram

```mermaid
flowchart TB
%% --- FRONTEND ---
    subgraph FRONTEND["💻 library-frontend (Spring Boot App – OAuth2 Client + Web UI)"]
        F1["@SpringBootApplication"]
        F2["SecurityFilterChain + oauth2Login()"]
        F3["Thymeleaf Web UI (Renders HTML pages)"]
        F4["RestTemplate (with Bearer Token)"]
        F5["ClientRegistrationRepository (OIDC Provider configuration)"]
    end

%% --- BACKEND ---
    subgraph BACKEND["⚙️ library-backend (Spring Boot App – Resource Server)"]
        B1["@SpringBootApplication"]
        B2["SecurityFilterChain + oauth2ResourceServer().jwt()"]
        B3["JwtDecoder (NimbusJwtDecoder)"]
        B4["JwtAuthenticationConverter"]
        B5["/api/books, /api/users ..."]
    end

%% --- AUTH SERVER ---
    subgraph AUTH["🛡️ Authorization Server (Keycloak)"]
        A1["/authorize"]
        A2["/token"]
        A3["/.well-known/jwks.json"]
        A4["/userinfo"]
    end

%% --- USER ---
    subgraph USER["👤 User / Browser"]
        U1["Browser / Web Client"]
    end

%% --- FLOWS ---
    U1 -->|" 1️⃣ GET /books (protected) "| FRONTEND
    FRONTEND -->|" 2️⃣ Redirect to /authorize "| AUTH
    AUTH -->|" 3️⃣ Login / Credentials "| U1
    AUTH -->|" 4️⃣ Authorization Code "| FRONTEND
    FRONTEND -->|" 5️⃣ Changes Code for Token (/token) "| AUTH
    AUTH -->|" 6️⃣ ID + Access Token "| FRONTEND
    FRONTEND -->|" 7️⃣ Calls REST-API with Bearer Token "| BACKEND
    BACKEND -->|" 8️⃣ Validates signature via JWKS "| AUTH
    AUTH -->|" 9️⃣ /.well-known/jwks.json (Public Keys) "| BACKEND
    BACKEND -->|" 🔟 Provides JSON-Data (books, users) "| FRONTEND
    FRONTEND -->|" 🏁 Renders HTML (Thymeleaf) "| U1

%% --- STYLES ---
    classDef comp fill: #f6f8fa, stroke: #ccc, stroke-width: 1px, rx: 8px, ry: 8px;
    class FRONTEND,BACKEND,AUTH comp;
```

## OAuth Flow Diagram
```mermaid
sequenceDiagram
    participant User
    participant Frontend as Library Frontend (OAuth2 Client, Thymeleaf)
    participant AuthServer as Keycloak Auth Server
    participant Backend as Library Backend (Resource Server)

    User->>Frontend: 1. Login / (no session)
    Frontend->>User: 2. 302 Redirect to /oauth2/authorization/keycloak
    User->>AuthServer: 3. GET /auth (with client_id, redirect_uri, response_type=code)
    Note right of User: User enters credentials
    AuthServer->>User: 4. 302 Redirect with authorization code
    User->>Frontend: 5. GET /login/oauth2/code/keycloak?code=...
    Frontend->>AuthServer: 6. Exchange code for tokens
    AuthServer->>Frontend: 7. {access_token, refresh_token, id_token}
    Frontend->>User: 8. Display protected page + Set-Cookie: JSESSIONID=...

    User->>Frontend: 9. Request another protected page (Cookie: JSESSIONID=...)
    Note right of Frontend: Retrieve access_token from SecurityContext

    Frontend->>+Frontend: 14. Get access_token from session
    Frontend->>Backend: 10. Request protected resource (Authorization: Bearer <access_token>)
    Backend->>AuthServer: 11. Validate token
    AuthServer->>Backend: 12. Token info (valid)
    Backend->>Frontend: 13. Send protected data
    Frontend->>+Frontend: 19. Render Thymeleaf template
    Frontend-->>-User: 20. Display protected page
```