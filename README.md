# Product Service

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Setup Instructions](#setup-instructions)
  - [Prerequisites](#prerequisites)
  - [Steps](#steps)
- [Docker Setup](#docker-setup)
  - [Prerequisites](#docker-prerequisites)
  - [Quick Start](#quick-start)
  - [Running with Other Services](#running-with-other-services)
  - [Useful Commands](#useful-commands)
- [API Endpoints](#api-endpoints)
  - [Category Management](#category-management)
  - [Product Management](#product-management)
  - [Product Search](#product-search)
- [Database Migrations](#database-migrations)
- [Testing](#testing)
  - [Controller Tests](#controller-tests)
  - [Service Tests](#service-tests)
  - [Coverage Report](#coverage-report)
- [Code Quality](#code-quality)
- [Error Handling](#error-handling)
- [License](#license)

## Overview
Product Service is a Spring Boot application designed to manage product and category data for an e-commerce backend application "Vibe Vault". It provides APIs for creating, updating, retrieving, and deleting products and categories. The service uses Flyway for database migrations and supports multiple implementations for product services, including database and external fake store integrations.

## Features
- CRUD operations for products
- CRUD operations for categories
- **Product Search** with filtering, pagination, sorting, and autocomplete
- OAuth2/JWT-based authentication and authorization
- Role-based access control (RBAC) with ADMIN, SELLER, and USER roles
- Integration with external fake store APIs
- Database migrations using Flyway
- Configurable service implementations (database or fake store)
- XSS protection with OWASP Encoder

## Technologies Used
- Java 21
- Spring Boot 4.0.1
- Spring Data JPA (Hibernate) with JPA Specifications for dynamic queries
- Spring Security with OAuth2 Resource Server
- Spring Boot Actuator for health checks and metrics
- Flyway for database migrations
- MySQL (Elasticsearch planned for search)
- Docker & Docker Compose for containerization
- Lombok
- OWASP Encoder for XSS prevention
- Spring RestClient for external API integration
- Mockito and Spring Security Test for testing

## Setup Instructions

### Prerequisites
- Java 21 or higher
- Maven
- MySQL database (or use Docker - see [Docker Setup](#docker-setup))

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/spa-raj/productservice.git
   ```
2. Navigate to the project directory:
   ```bash
   cd productservice
   ```
3. Configure the environment variables:
   - The application uses environment variables for configuration. Set the following:
     ```bash
     export PORT=8080                                    # Server port
     export DB_URL=jdbc:mysql://localhost:3306/productservice  # Database URL
     export DB_USERNAME=root                             # Database username
     export DB_PASSWORD=yourpassword                     # Database password
     export ISSUER_URI=http://localhost:8081             # OAuth2 issuer URI (your auth server)
     ```
   - Alternatively, you can create an `.env` file or configure these in your IDE.
   - The `productServiceType` property in `application.properties` controls which service implementation to use:
     - `productServiceDBImpl` - Uses the database implementation (default)
     - `productServiceFakeStoreImpl` - Uses the external fake store API
4. Run Flyway migrations:
   ```bash
   mvn flyway:migrate
   ```
5. Build and run the application:
   ```bash
   mvn spring-boot:run
   ```

## Docker Setup

Run the Product Service in containers for consistent development and deployment.

### Docker Prerequisites
- Docker Engine 20.10+
- Docker Compose v2.0+

### Quick Start

1. **Start the service with MySQL:**
   ```bash
   docker compose up -d
   ```
   This starts:
   - MySQL 8.0 on port 3307 (host) → 3306 (container)
   - Product Service on port 8080

2. **Verify the service is running:**
   ```bash
   # Check container status
   docker compose ps

   # Check health endpoint
   curl http://localhost:8080/actuator/health
   ```

3. **View logs:**
   ```bash
   docker compose logs -f productservice
   ```

4. **Stop the service:**
   ```bash
   docker compose down
   ```

5. **Stop and remove volumes (clean slate):**
   ```bash
   docker compose down -v
   ```

### Running with Other Services

The Product Service needs to communicate with the User Service for JWT validation. To enable inter-service communication:

1. **Create a shared Docker network:**
   ```bash
   docker network create vibevault-network
   ```

2. **Start User Service** (in the userservice project):
   ```bash
   # Ensure userservice's docker-compose.yml includes:
   # networks:
   #   - vibevault-network (external: true)
   docker compose up -d
   ```

3. **Start Product Service:**
   ```bash
   docker compose up -d
   ```

The Product Service is configured to reach User Service at `http://userservice:8081` via the shared network.

**Override the issuer URI if needed:**
```bash
ISSUER_URI=http://host.docker.internal:8081 docker compose up -d
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 8080 | Server port |
| `DB_URL` | jdbc:mysql://mysql:3306/productservice | Database connection URL |
| `DB_USERNAME` | productuser | Database username |
| `DB_PASSWORD` | productpass | Database password |
| `ISSUER_URI` | http://userservice:8081 | OAuth2 issuer URI for JWT validation |
| `JAVA_OPTS` | -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 | JVM options |

### Useful Commands

```bash
# Build the image without cache
docker compose build --no-cache

# View container resource usage
docker stats productservice-app productservice-mysql

# Execute commands inside the container
docker exec -it productservice-app sh

# Connect to MySQL
docker exec -it productservice-mysql mysql -u productuser -p productservice
```

### Production Considerations

- **Secrets Management:** The credentials in `docker-compose.yml` are for local development only. For production (Kubernetes/EKS), use AWS Secrets Manager, Kubernetes Secrets, or HashiCorp Vault.
- **Image Size:** The multi-stage Dockerfile produces an optimized image (~250MB) using JRE Alpine.
- **Health Checks:** The container includes health checks at `/actuator/health` for orchestration platforms.
- **Non-root User:** The container runs as a non-root user (`spring`) for security.

## API Endpoints

### Category Management
- **POST /categories**
  - Description: Create a new category. Only ADMINs can create categories.
  - Request Headers:
    ```json
    {
      "Authorization": "Bearer <jwt_token>"
    }
    ```
  - Request Body:
    ```json
    {
      "name": "Electronics",
      "description": "Devices and gadgets"
    }
    ```
  - Response:
    ```json
    {
      "id": "f23d2b1c-4e5f-4a8b-9c3e-2f1a2b3c4d5e",
      "categoryName": "Electronics",
      "description": "Devices and gadgets"
    }
    ```

- **GET /categories**
  - Description: Retrieve all categories.
  - Response:
    ```json
    [
      {
        "id": "f23d2b1c-4e5f-4a8b-9c3e-2f1a2b3c4d5e",
        "name": "Electronics",
        "description": "Devices and gadgets"
      },
      {
        "id": "b12c3d4e-5f6g-7h8i-9j0k-1l2m3n4o5p6q",
        "name": "Books",
        "description": "Various genres of books"
      }
    ]
    ```
- **GET /categories/id/{categoryId}**
- Description: Retrieve a category by its ID.
  - Path Parameter: `categoryId` (UUID of the category)
  - Example:
    ```bash
    GET /categories/id/f23d2b1c-4e5f-4a8b-9c3e-2f1a2b3c4d5e
    ```
  - Response:
  ```json
  {
    "id": "f23d2b1c-4e5f-4a8b-9c3e-2f1a2b3c4d5e", 
    "name": "Electronics",
    "description": "Devices and gadgets"
  }
  ```
    
- **GET /categories/name/{categoryName}**
  - Description: Retrieve a category by its name.
  - Path Parameter: `categoryName` (name of the category)
  - Example:
    ```bash
    GET /categories/name/Electronics
    ```
  - Response:
    ```json
    {
      "id": "f23d2b1c-4e5f-4a8b-9c3e-2f1a2b3c4d5e",
      "name": "Electronics",
      "description": "Devices and gadgets"
    }
    ```
- **GET /categories/products/{category}**
  - Description: Retrieve all products in a specific category.
  - Path Parameter: `category` (name of the category)
  - Example:
    ```bash
    GET /categories/products/Electronics
    ```
  - Response:
    ```json
    [
      {
        "id": "fdsa1234-5678-90ab-cdef12345678",
        "name": "iPhone 14",
        "description": "Latest Apple smartphone",
        "imageUrl": "https://example.com/iphone14.jpg",
        "categoryName": "Electronics",
        "price": {
          "price": 699.99,
          "currency": "USD"
        }
      },
      {
        "id": "abcd1234-5678-90ab-cdef12345678",
        "name": "Samsung Galaxy S21",
        "description": "Latest Samsung smartphone",
        "imageUrl": "https://example.com/galaxys21.jpg",
        "categoryName": "Electronics",
        "price": {
          "price": 799.99,
          "currency": "USD"
        }
      }
    ]
    ```
- **GET /categories/products**
  - Description: Retrieve all products in a list of category UUIDs.
  - Query Parameter: `categoryUuid` (list of category UUIDs)
  - Example:
    ```bash
    GET /categories/products?categoryUuid=f23d2b1c-4e5f-4a8b-9c3e-2f1a2b3c4d5e&categoryUuid=b12c3d4e-5f6g-7h8i-9j0k-1l2m3n4o5p6q
    ```
  - Response:
    ```json
    [
      {
        "id": "fdsa1234-5678-90ab-cdef12345678",
        "name": "iPhone 14",
        "description": "Latest Apple smartphone",
        "imageUrl": "https://example.com/iphone14.jpg",
        "categoryName": "Electronics",
        "price": {
          "price": 699.99,
          "currency": "USD"
        }
      },
      {
        "id": "abcd1234-5678-90ab-cdef12345678",
        "name": "The Great Gatsby",
        "description": "Classic novel by F. Scott Fitzgerald",
        "imageUrl": "https://example.com/gatsby.jpg",
        "categoryName": "Books",
        "price": {
          "price": 12.99,
          "currency": "USD"
        }
      }
    ]
    ```

### Product Management
- **POST /products**
  - Description: Create a new product. Only SELLERs and ADMINs can create products.
  - Request Headers:
    ```json
    {
      "Authorization": "Bearer <jwt_token>"
    }
    ```
  - Request Body:
    ```json
    {
      "name": "iPhone 14",
      "description": "Latest Apple smartphone",
      "imageUrl": "https://example.com/iphone14.jpg",
      "price": 699.99,
      "currency": "USD",
      "categoryName": "Electronics"
    }
    ```
  - Response:
    ```json
    {
      "id": "fdsa1234-5678-90ab-cdef12345678",
      "name": "iPhone 14",
      "description": "Latest Apple smartphone",
      "imageUrl": "https://example.com/iphone14.jpg",
      "price": {
        "price": 699.99,
        "currency": "USD"
      },
      "categoryName": "Electronics"
    }
    ```
- **PATCH /products/{productId}**
  - Description: Update an existing product. Only SELLERs and ADMINs can update products.
  - Path Parameter: `productId` (ID of the product to update)
  - Request Headers:
    ```json
    {
      "Authorization": "Bearer <jwt_token>"
    }
    ```
  - Request Body:
    ```json
    {
      "id": "fdsa1234-5678-90ab-cdef12345678",
      "name": "iPhone 14 Pro",
      "description": "Latest Apple smartphone with Pro features",
      "imageUrl": "https://example.com/iphone14pro.jpg",
      "price": 999.99,
      "currency": "USD",
      "categoryName": "Electronics"
    }
    ```
  - Response:
    ```json
    {
      "id": "fdsa1234-5678-90ab-cdef12345678",
      "name": "iPhone 14 Pro",
      "description": "Latest Apple smartphone with Pro features",
      "imageUrl": "https://example.com/iphone14pro.jpg",
      "price": {
        "price": 999.99,
        "currency": "USD"
      },
      "categoryName": "Electronics"
    }
    ```

- **GET /products**
  - Description: Retrieve all products.
  - Response:
    ```json
    [
        {
            "id": "fdsa1234-5678-90ab-cdef12345678",
            "name": "iPhone 14",
            "description": "Latest Apple smartphone",
            "imageUrl": "https://example.com/iphone14.jpg",
            "categoryName": "Electronics",
            "price": {
              "price": 699.99,
              "currency": "USD"
            }
        },
        {
            "id": "abcd1234-5678-90ab-cdef12345678",
            "name": "Samsung Galaxy S21",
            "description": "Latest Samsung smartphone",
            "imageUrl": "https://example.com/galaxys21.jpg",
            "categoryName": "Electronics",
            "price": {
              "price": 799.99,
              "currency": "USD"
            }
        }
    ]
    ```
- **GET /products/{productId}**
  - Description: Retrieve a product by its ID.
  - Path Parameter: `productId` (ID of the product)
  - Example:
    ```bash
    GET /products/fdsa1234-5678-90ab-cdef12345678
    ```
  - Response:
    ```json
    {
      "id": "fdsa1234-5678-90ab-cdef12345678",
      "name": "iPhone 14",
      "description": "Latest Apple smartphone",
      "imageUrl": "https://example.com/iphone14.jpg",
      "categoryName": "Electronics",
      "price": {
        "price": 699.99,
        "currency": "USD"
      }
    }
    ```
- **DELETE /products/{productId}**
  - Description: Delete a product by its ID. Only SELLERs and ADMINs can delete products.
  - Path Parameter: `productId` (ID of the product)
  - Request Headers:
    ```json
    {
      "Authorization": "Bearer <jwt_token>"
    }
    ```
    - Example:
      ```bash
      DELETE /products/fdsa1234-5678-90ab-cdef12345678
      ```
      - Response:
      ```json
      {
        "id": "fdsa1234-5678-90ab-cdef12345678",
        "name": "iPhone 14",
        "description": "Latest Apple smartphone",
        "imageUrl": "https://example.com/iphone14.jpg",
        "categoryName": "Electronics",
        "price": {
          "price": 699.99,
          "currency": "USD"
         }
      }
      ```
- **PUT /products/{productId}**
- Description: Update a product by its ID. Only SELLERs and ADMINs can update products.
  - Path Parameter: `productId` (ID of the product)
  - Request Headers:
    ```json
    {
      "Authorization": "Bearer <jwt_token>"
    }
    ```
  - Request Body:
    ```json
    {
      "id": "fdsa1234-5678-90ab-cdef12345678",
      "name": "iPhone 14 Pro",
      "description": "Latest Apple smartphone with Pro features",
      "imageUrl": "https://example.com/iphone14pro.jpg",
      "categoryName": "Electronics",
      "price": 999.99,
      "currency": "USD"
    }
    ```
  - Response:
    ```json
    {
      "id": "fdsa1234-5678-90ab-cdef12345678",
      "name": "iPhone 14 Pro",
      "description": "Latest Apple smartphone with Pro features",
      "imageUrl": "https://example.com/iphone14pro.jpg",
      "categoryName": "Electronics",
      "price": {
        "price": 999.99,
        "currency": "USD"
      }
    }
    ```
### Product Search

The search functionality provides flexible product discovery with multiple filter options. Currently implemented using MySQL with JPA Specifications, with Elasticsearch integration planned for enhanced full-text search capabilities.

> **Architecture Note:** The search feature uses a Strategy pattern (`SearchService` interface) allowing seamless switching between MySQL and Elasticsearch implementations.

- **GET /search/products**
  - Description: Search products with optional filters, pagination, and sorting.
  - Query Parameters:
    | Parameter | Type | Description |
    |-----------|------|-------------|
    | `query` | String | Text search on product name |
    | `minPrice` | Double | Minimum price filter |
    | `maxPrice` | Double | Maximum price filter |
    | `currency` | String | Currency filter (USD, EUR, GBP, INR) |
    | `categoryId` | UUID | Filter by category ID |
    | `categoryName` | String | Filter by category name (case-insensitive) |
    | `createdAfter` | Date | Products created after this date (ISO format) |
    | `createdBefore` | Date | Products created before this date (ISO format) |
    | `page` | int | Page number (default: 0) |
    | `size` | int | Page size (default: 10, max: 100) |
    | `sortBy` | String | Sort field: name, price, createdAt, lastModifiedAt |
    | `sortDir` | String | Sort direction: asc, desc (default: desc) |
  - Example:
    ```bash
    GET /search/products?query=iphone&minPrice=100&maxPrice=1000&currency=USD&sortBy=price&sortDir=asc
    ```
  - Response:
    ```json
    {
      "products": [
        {
          "id": "fdsa1234-5678-90ab-cdef12345678",
          "name": "iPhone 14",
          "description": "Latest Apple smartphone",
          "imageUrl": "https://example.com/iphone14.jpg",
          "categoryName": "Electronics",
          "price": {
            "price": 699.99,
            "currency": "USD"
          }
        }
      ],
      "currentPage": 0,
      "totalPages": 1,
      "totalElements": 1,
      "pageSize": 10,
      "first": true,
      "last": true,
      "hasNext": false,
      "hasPrevious": false
    }
    ```

- **GET /search/products/suggest**
  - Description: Get autocomplete suggestions based on product name prefix.
  - Query Parameters:
    | Parameter | Type | Description |
    |-----------|------|-------------|
    | `prefix` | String | **Required.** Name prefix to search |
    | `limit` | int | Max suggestions (default: 5, max: 10) |
  - Example:
    ```bash
    GET /search/products/suggest?prefix=iph&limit=5
    ```
  - Response:
    ```json
    [
      {
        "id": "fdsa1234-5678-90ab-cdef12345678",
        "name": "iPhone 14",
        "categoryName": "Electronics"
      },
      {
        "id": "abcd1234-5678-90ab-cdef12345678",
        "name": "iPhone 13",
        "categoryName": "Electronics"
      }
    ]
    ```

#### Current Implementation (MySQL)
- Uses JPA Specifications for dynamic query building
- Supports all filter combinations
- Case-insensitive text search on product name
- Proper escaping of SQL wildcards to prevent injection

#### Future Enhancement (Elasticsearch)
- Full-text search across name and description
- Fuzzy matching and typo tolerance
- Relevance scoring
- Faster search performance at scale
- Advanced autocomplete with suggestions

## Database Migrations
Flyway is used for managing database schema migrations. Migration scripts are located in `src/main/resources/db/migration`.

## Testing

The application includes comprehensive unit and integration tests to ensure functionality and reliability. Below are the key testing points:

### Controller Tests
- **CategoryControllerTest**: Unit tests for category controller logic.
- **ProductControllerTest**: Unit tests for product controller logic.
- **CategoryControllerMVCTest**: Integration tests for category endpoints using MockMvc.
- **ProductControllerMVCTest**: Integration tests for product endpoints using MockMvc with security context.
- **SearchControllerMVCTest**: Integration tests for search endpoints including pagination, filtering, and sorting.

### Service Tests
- **CategoryServiceDBImplTest**: Tests the category service logic for database operations.
- **ProductServiceDBImplTest**: Tests the product service logic for database operations.
- **ProductServiceFakeStoreImplTest**: Tests the product service logic for external fake store integration.
- **SearchServiceDBImplTest**: Tests the search service logic including validation and query building.

### Specification Tests
- **ProductSpecificationTest**: Tests JPA Specification builders for dynamic query composition.

### Coverage Report
To view the coverage report:
1. Navigate to the `coverageReport` directory.
2. Open the `index.html` file in a browser.
3. Explore the detailed coverage metrics sorted by blocks, classes, lines, methods, and names.

## Code Quality
SonarQube integration is planned for code quality and coverage analysis.

## Error Handling

The Product Service includes robust error handling mechanisms to ensure smooth operation and user-friendly feedback. Below are the key aspects of error handling:

### Exception Advices
- **Global Exception Handling**: The `ExceptionAdvices` class provides centralized exception handling for the application.
- **Custom Error Responses**: Exceptions are mapped to user-friendly error messages and appropriate HTTP status codes.

### Common Exceptions
- **Category Exceptions**:
  - `CategoryAlreadyExistsException`: Thrown when attempting to create a category that already exists.
  - `CategoryNotCreatedException`: Thrown when a category fails to be created.
  - `CategoryNotFoundException`: Thrown when a requested category is not found.
- **Product Exceptions**:
  - `ProductNotCreatedException`: Thrown when a product fails to be created.
  - `ProductNotDeletedException`: Thrown when a product fails to be deleted.
  - `ProductNotFoundException`: Thrown when a requested product is not found.
- **Authentication Exceptions**:
  - `InvalidTokenException`: Thrown when an invalid token is provided during authentication.

### Error Response Format
All error responses follow a consistent format:
```json
{
  "message": "Category not found"
}
```

## License
This project is licensed under the Apache-2.0 License. See the LICENSE file for details.
