# Products Manager - Spring Boot + HTMX + Kotlin

A modern product management application built with Spring Boot, Kotlin, HTMX, Thymeleaf, and Web Awesome.

## Tech Stack

- **Spring Boot 3.5.7** - Backend framework
- **Kotlin** - Programming language
- **PostgreSQL** - Database
- **Flyway** - Database migrations
- **HTMX** - Dynamic UI updates without page reloads
- **Thymeleaf** - Server-side rendering
- **Web Awesome** - UI components and design tokens
- **Gradle (Kotlin DSL)** - Build tool

## Features

- ✅ Load products from database with a single button click
- ✅ Add new products via form without page reload
- ✅ Dynamic table updates using HTMX
- ✅ Modern UI with Web Awesome design tokens
- ✅ Responsive design

## Prerequisites

- Java 25+
- Docker and Docker Compose (for PostgreSQL)
- Gradle (optional, uses wrapper)

## Setup Instructions

### 1. Start PostgreSQL Database

Start the PostgreSQL database using Docker Compose:

```bash
docker-compose up -d
```

This will start a PostgreSQL container on port 5432 with:
- Database: `productdb`
- Username: `postgres`
- Password: `postgres`

### 2. Build the Application

```bash
./gradlew build
```

Or on Windows:

```bash
gradlew.bat build
```

### 3. Run the Application

```bash
./gradlew bootRun
```

Or on Windows:

```bash
gradlew.bat bootRun
```

The application will be available at: `http://localhost:8080/products`

### 4. Using the Application

1. Navigate to `http://localhost:8080/products`
2. Click the "Load Products" button to fetch products from the database
3. Fill in the form to add a new product:
   - Name (required)
   - Description (optional)
   - Price (required)
4. Click "Add Product" to save and see the table update automatically

## Project Structure

```
src/
├── main/
│   ├── kotlin/com/respiroc/greg/fullstackdeveloperproject/
│   │   ├── FullstackDeveloperProjectApplication.kt  # Main application class
│   │   ├── controller/
│   │   │   └── ProductController.kt                 # REST endpoints
│   │   ├── model/
│   │   │   └── Product.kt                           # Data entity
│   │   └── repository/
│   │       └── ProductRepository.kt                 # JPA repository
│   └── resources/
│       ├── application.properties                   # App configuration
│       ├── db/
│       │   └── migration/
│       │       └── V1__Create_products_table.sql    # Flyway migration
│       └── templates/
│           ├── index.html                           # Main page
│           └── fragments/
│               └── product-table.html               # Table fragment
docker-compose.yml                                   # PostgreSQL setup
build.gradle.kts                                     # Build configuration
```

## Database

The database schema is managed by Flyway migrations. The products table includes:
- `id` - Primary key (auto-generated)
- `name` - Product name
- `description` - Product description
- `price` - Product price
- `created_at` - Timestamp

## Design System

The application uses Web Awesome design tokens for consistent styling:
- Colors (`--wa-color-*`)
- Spacing (`--wa-spacing-*`)
- Borders (`--wa-stroke-width-*`)
- Shadows (`--wa-shadow-*`)
- Corner radius (`--wa-corner-radius`)

## Development

### Run Tests

```bash
./gradlew test
```

### Stop Database

```bash
docker-compose down
```

To also remove volumes:

```bash
docker-compose down -v
```

## License

This project is open source and available under the MIT License.






