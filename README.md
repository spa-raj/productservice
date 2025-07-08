# Product Service

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Setup Instructions](#setup-instructions)
  - [Prerequisites](#prerequisites)
  - [Steps](#steps)
- [API Endpoints](#api-endpoints)
  - [Category Management](#category-management)
  - [Product Management](#product-management)
- [Database Migrations](#database-migrations)
- [Testing](#testing)
- [Error Handling](#error-handling)
- [License](#license)

## Overview
Product Service is a Spring Boot application designed to manage product and category data for an e-commerce backend application "Vibe Vault". It provides APIs for creating, updating, retrieving, and deleting products and categories. The service uses Flyway for database migrations and supports multiple implementations for product services, including database and external fake store integrations.

## Features
- CRUD operations for products
- CRUD operations for categories
- Integration with external fake store APIs
- Database migrations using Flyway
- Configurable service implementations

## Technologies Used
- Java 21
- Spring Boot
- Hibernate
- Flyway for database migrations
- MySQL

## Setup Instructions

### Prerequisites
- Java 21 or higher
- Maven
- MySQL database

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/spa-raj/productservice.git
   ```
2. Navigate to the project directory:
   ```bash
   cd productservice
   ```
3. Configure the database and environment variables:
   - Update `application.properties` with your database credentials and other environment variables.
   - Example:
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/productservice
     spring.datasource.username=root
     spring.datasource.password=yourpassword
     spring.application.name=productservice
     ## Choose a different port than the user service to avoid conflicts
     server.port=8080
     spring.jpa.hibernate.ddl-auto=validate
     spring.jpa.show-sql=true
     spring.flyway.enabled=true
     spring.flyway.locations=classpath:db/migration
     ## Configure the product service implementation
     ## productServiceType can be productServiceDBImpl or productServiceFakeStoreImpl
     ## To use the database implementation: set it to productServiceDBImpl
     ## To use the fake store implementation: set it to productServiceFakeStoreImpl
     productServiceType=productServiceDBImpl
     ```
4. Run Flyway migrations:
   ```bash
   mvn flyway:migrate
   ```
5. Build and run the application:
   ```bash
   mvn spring-boot:run
   ```

## API Endpoints

### Category Management
- **POST /categories**
  - Description: Create a new category.
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
        "id": "f23d2b1c-4e5f-4a8b-9c3e-2f1a2b3c4d5e",
        "name": "Electronics",
        "description": "Devices and gadgets",
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
      }
    ]
    ```
- **GET /categories/products**
  - Description: Retrieve all products in a list of category Ids.
    - Request Body:
        ```json
        {
        "categoryIds": ["f23d2b1c-4e5f-4a8b-9c3e-2f1a2b3c4d5e", "b12c3d4e-5f6g-7h8i-9j0k-1l2m3n4o5p6q"]
        }
        ```
    - Response:
        ```json
        [
          {
            "id": "f23d2b1c-4e5f-4a8b-9c3e-2f1a2b3c4d5e",
            "name": "Electronics",
            "description": "Devices and gadgets",
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
            ]
          },
          {
            "id": "b12c3d4e-5f6g-7h8i-9j0k-1l2m3n4o5p6q",
            "name": "Books",
            "description": "Various genres of books",
            "products": []
          }
        ]
        ```
### Product Management
- **POST /products**
  - Description: Create a new product. Only SELLERs and ADMINs can create products.
  - Request Headers:
    ```json
    {
      "Authorization":"eyhsdffa....."
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
      "Authorization":"eyhsdffa....."
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
- **GET /products/id/{productId}**
  - Description: Retrieve a product by its ID.
  - Path Parameter: `productId` (ID of the product)
  - Example:
    ```bash
    GET /products/id/fdsa1234-5678-90ab-cdef12345678
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
      "Authorization":"eyhsdffa....."
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
      "Authorization":"eyhsdffa....."
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
## Database Migrations
Flyway is used for managing database schema migrations. Migration scripts are located in `src/main/resources/db/migration`.

## Testing

The application includes comprehensive unit and integration tests to ensure functionality and reliability. Below are the key testing points:

### Controller Tests
- **CategoryControllerTest**: Validates category endpoints such as creation and retrieval.
- **ProductControllerTest**: Validates product endpoints such as creation and retrieval.

### Service Tests
- **CategoryServiceDBImplTest**: Tests the category service logic for database operations.
- **ProductServiceDBImplTest**: Tests the product service logic for database operations.
- **ProductServiceFakeStoreImplTest**: Tests the product service logic for external fake store integration.

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
