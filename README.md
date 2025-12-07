# Products Manager - Spring Boot + HTMX + Kotlin

A modern product management application built with Spring Boot, Kotlin, HTMX, Thymeleaf, and Web Awesome.

## Tech Stack

- **Spring Boot 3.5.7** - Backend framework
- **Kotlin** - Programming language
- **PostgreSQL** - Database
- **Redis** - Caching
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

- Java 24+
- Docker and Docker Compose (for PostgreSQL)
- Gradle (optional, uses wrapper)

## Setup Instructions

### 1. Start Docker Desktop

The project runs in docker containers so you have to have Docker engine up and running.

### 2. Build and start up the application

There is a convenient script that:
1. Runs all tests
2. Builds the app executable
3. Shutting current docker compose set and cleans it up
4. Rebuild the app image and starts up the whole setup - posgres, redis, app containers
 
```bash
 .\build-and-restart.ps1
```

### 4. Using the Application

1. Navigate to `http://localhost:8080`
2. Click the "Products" button to fetch products from the database
3. Click on "View variants" button to fetch all product variants available
4. You can edit/delete each product/product variant
3. Fill in the form to add a new product:
   - Title 
   - Vendor
   - Product type


## Database

The database schema is managed by Flyway migrations. 

The `products` table includes:
- `id` - Primary key (auto-generated)
- `title`
- `vendor`
- `product_type` 

The `products_variant` table includes:
- `id` - Primary key (auto-generated)
- `product_id` - foreign key referencing the product id from product table
- `color_option` 
- `size_option`
- `price`
- `available`
