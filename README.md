# Document Manager - Kotlin Demo

![Kotlin](https://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/Kotlin_logo_%282016-2021%29.svg/1280px-Kotlin_logo_%282016-2021%29.svg.png)

This repository contains a simple demo project for a secure document management API built with Kotlin and Spring Boot. It functions an OAuth 2.0 Resource Server, securing endpoints with JSON Web Tokens (JWTs). The project highlights best practices for configuring JWT validation, including custom audience checks, and mapping JWT claims (scopes, roles, permissions) to Spring Security authorities for fine-grained access control.

## Features

*   **Framework:** Built with Spring Boot and Kotlin.
*   **Security:** Implements an OAuth 2.0 Resource Server using Spring Security.
*   **Authentication:** Secures endpoints using JWT Bearer Tokens.
*   **JWT Validation:** Features a custom JWT decoder with validation for both `issuer` and `audience` claims.
*   **Authorization:**
    *   Demonstrates claim mapping from a JWT (`scope`, `roles`, `permissions`) to Spring Security `GrantedAuthority` objects.
    *   Uses method-level security (`@PreAuthorize`) for fine-grained endpoint protection.

## Prerequisites

*   JDK 17 or newer
*   Gradle

## Getting Started

1. **Clone the repository:**

   ```sh
   git clone https://github.com/origamifolds/doc-manager-kotlin-demo.git
   ```

2. **Navigate to the project directory:**

   ```sh
   cd doc-manager-kotlin-demo
   ```

3. **Run the application:**

   * On macOS/Linux:

     ```sh
     ./gradlew bootRun
     ```

   * On Windows:

     ```sh
     gradlew.bat bootRun
     ```

The server will start on `http://localhost:8080`.

## Configuration Details

### Security Configuration (`OAuth2ResourceServerSecurityConfiguration.kt`)

*   The application is configured as a stateless resource server, disabling sessions, CSRF, and basic/form login.
*   A custom `jwtDecoder` bean is configured to validate JWTs against a specific `issuer` (`https://myapp.com/auth`) and `audience` (`http://localhost:8080/api/`).
*   A `jwtAuthenticationConverter` bean maps claims from the JWT payload to Spring Security authorities. It processes `scope`, `roles`, and `permissions` claims, prefixing scopes with `SCOPE_` and roles with `ROLE_`.

### Public Key (`application.properties`)

The server is configured to use the `public.pem` file located in `src/main/resources` to verify the signature of incoming JWTs. The corresponding `private.pem` is also included in the repository for local testing and token generation.

## API Endpoints

### Fetch Documents

*   **Endpoint:** `GET /api/fetchDocuments`
*   **Description:** A protected endpoint to retrieve documents.
*   **Authorization:** Requires a valid JWT Bearer token. To gain access, the token must be valid and grant all of the following authorities:
    *   `SCOPE_read:documents` (checked at the filter chain level)
    *   `ROLE_admin` (checked via `@PreAuthorize`)
    *   `documents:read:all` (checked via `@PreAuthorize`)

## Testing the Endpoint

To call the protected `/api/fetchDocuments` endpoint, you must generate a JWT that meets the following criteria:

1.  **Signature:** Signed with the `private.pem` key provided in this repository.
2.  **Issuer (`iss` claim):** The value must be `https://myapp.com/auth`.
3.  **Audience (`aud` claim):** The value must be `http://localhost:8080/api/`.
4.  **Payload Claims:** The payload must include the claims required to grant the necessary authorities:
    *   `scope`: A string containing `"read:documents"`
    *   `roles`: An array containing `"admin"`
    *   `permissions`: An array containing `"documents:read:all"`

**Example cURL Request:**

```sh
# Replace <YOUR_GENERATED_JWT> with a valid token
curl -X GET http://localhost:8080/api/fetchDocuments \
-H "Authorization: Bearer <YOUR_GENERATED_JWT>"
```

You can use online tools like `jwt.io` or a local script to generate a token using the provided RSA keys and the required payload claims.
