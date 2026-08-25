# Mail & Media Shop API

REST API for managing customers, products, and contracts in the Mail & Media Shop platform.
Built with **Spring Boot** and documented via **Springdoc OpenAPI (Swagger UI)**.

---

## Table of Contents
- [Technology Stack](#technology-stack)

- [Swagger UI](#swagger-ui)
- [API Overview](#api-overview)
  - [Customer API](#customer-api)
  - [Contract API](#contract-api)
  - [Product API](#product-api)
  - [Billing API](#billing-api)
- [Common Response Schemas](#common-response-schemas)
- [Error Handling](#error-handling)

- [Monitoring & Observability](#monitoring--observability)
  - [Monitoring Components](#monitoring-components)
  - [Sentry Error Tracking](#sentry-error-tracking)
  - [ELK Stack (Structured Log Pipeline)](#elk-stack-structured-log-pipeline)
  - [Log Persistence](#log-persistence)
  - [How to Visualize](#how-to-visualize)
  - [ELK Operations](#elk-operations)


- [Testing](#testing)

- [Dockerization](#dockerization)
- [CI/CD Pipeline & Deployment](#cicd-pipeline--deployment)
  - [Pipeline Stages](#pipeline-stages)
  - [Deployment Scripts](#deployment-scripts)
  - [Environment Configuration](#environment-configuration)
- [Security & Authentication (Spring Security + Keycloak)](#security--authentication-spring-security--keycloak)
- [Redis Caching](#redis-caching)
- [Maintain Code Cleanliness](#maintain-code-cleanliness)
  - [SonarQube](#6-sonarqube--static-analysis-and-quality-gate)
  - [Spotless](#1-spotless--automatic-code-formatting)
  - [PMD](#2-pmd--anti-pattern-and-code-duplication-detection)
  - [SpotBugs](#3-spotbugs--findsecbugs--bytecode-analysis-and-security)
  - [OWASP](#4-owasp-dependency-check--snyk--dependency-security)
  - [Git Pre-commit Hook](#5-git-pre-commit-hook--automatic-checks-before-commit)

- [Dependency Management & Build](#dependency-management--build)
- [Troubleshooting](#troubleshooting)

---

## Technology Stack
- **Core**: Java 25, Spring Boot 4.0.5
- **Database**: PostgreSQL 16, Flyway (Migrations)
- **Caching**: Redis (Jedis Client)
- **Quality**: PMD, SpotBugs, Spotless, JaCoCo, SonarQube
- **Documentation**: Swagger UI (OpenAPI 3.1)
- **Security**: Spring Security, OAuth2 Resource Server, Keycloak
- **Logging**: ELK Stack (Elasticsearch 9.4.2, Logstash 9.4.2, Kibana 9.4.2)

---

## Swagger UI

Interactive API documentation is available at:

[http://localhost:8090/swagger-ui/index.html](http://localhost:8090/swagger-ui/index.html)

---

## API Overview

### Base URL

```
http://localhost:8090/api/v1
```

## Customer API

**Tag:** `Customer`  
**Base path:** `/api/v1/customers`

Manages the full lifecycle of a customer: registration, retrieval, activation, deactivation, and removal.
Also exposes the product purchase operation that creates a contract.

---

### POST `/api/v1/shop/customers` — Register a New Customer

Creates a new customer record.

**HTTP Method:** `POST`  
**Request Content-Type:** `application/json`  
**Response Content-Type:** `application/json`

#### Request Body

| Field                  | Type                         | Required | Constraints                              |
|------------------------|------------------------------|----------|------------------------------------------|
| `firstName`            | `string`                     | Yes      | Not blank, max 100                       |
| `lastName`             | `string`                     | Yes      | Not blank, max 100                       |
| `birthDate`            | `string (LocalDate)`         | Yes      | Not null, must be in the past, format: `YYYY-MM-DD` |
| `address`              | `AddressRequestDTO`          | Yes      | Not null, see sub-fields below           |
| `invoiceAddress`       | `AddressRequestDTO` / `null` | No       | Nullable, same structure as `address`    |
| `communicationDetails` | `CommunicationDetailsRequestDTO` | Yes  | Not null, see sub-fields below           |
| `brand`                | `string (Brand enum)`        | Yes      | One of: `GMX`, `WEB_DE`, `MAIL_COM`     |

**`AddressRequestDTO` fields:**

| Field      | Type     | Constraints         |
|------------|----------|---------------------|
| `street`   | `string` | Not blank, max 250  |
| `number`   | `string` | Not blank, max 100  |
| `postcode` | `string` | Not blank, max 100  |
| `city`     | `string` | Not blank, max 100  |
| `country`  | `string` | Not blank, min 2, max 100 |

**`CommunicationDetailsRequestDTO` fields:**

| Field       | Type     | Constraints                    |
|-------------|----------|--------------------------------|
| `email`     | `string` | Not blank, valid email format, max 255 |
| `telephone` | `string` | Not blank, min 5, max 30       |

#### Example Request (curl)

```bash
curl -X POST http://localhost:8090/api/v1/shop/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Max",
    "lastName": "Mustermann",
    "birthDate": "1990-06-15",
    "address": {
      "street": "Musterstraße",
      "number": "12A",
      "postcode": "68161",
      "city": "Mannheim",
      "country": "Germany"
    },
    "invoiceAddress": null,
    "communicationDetails": {
      "email": "max.mustermann@gmx.de",
      "telephone": "+49 621 123456"
    },
    "brand": "GMX"
  }'
```

#### Example Response — `201 Created`

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "firstName": "Max",
  "lastName": "Mustermann",
  "birthDate": "1990-06-15",
  "address": {
    "street": "Musterstraße",
    "number": "12A",
    "postcode": "68161",
    "city": "Mannheim",
    "country": "Germany"
  },
  "invoiceAddress": null,
  "communicationDetails": {
    "email": "max.mustermann@gmx.de",
    "telephone": "+49 621 123456"
  },
  "brand": "GMX",
  "status": "ACTIVE"
}
```

#### Response Status Codes

| Status | Description                                  |
|--------|----------------------------------------------|
| `201`  | Customer registered successfully             |
| `400`  | Validation failed — invalid or missing fields |
| `500`  | Unexpected internal server error              |

---

### GET `/api/v1/shop/customers/{customerId}` — Load a Customer by ID

Returns the customer with the specified UUID.

**HTTP Method:** `GET`

#### Path Parameters

| Parameter    | Type   | Required | Description                   |
|--------------|--------|----------|-------------------------------|
| `customerId` | `UUID` | Yes      | Unique identifier of the customer |

#### Example Request (curl)

```bash
curl -X GET http://localhost:8090/api/v1/shop/customers/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

#### Example Response — `200 OK`

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "firstName": "Max",
  "lastName": "Mustermann",
  "birthDate": "1990-06-15",
  "address": {
    "street": "Musterstraße",
    "number": "12A",
    "postcode": "68161",
    "city": "Mannheim",
    "country": "Germany"
  },
  "invoiceAddress": null,
  "communicationDetails": {
    "email": "max.mustermann@gmx.de",
    "telephone": "+49 621 123456"
  },
  "brand": "GMX",
  "status": "ACTIVE"
}
```
#### Status Examples

In responses, the `status` field indicates the current lifecycle state:

**Active Customer:**
```json
{ "id": "...", "status": "ACTIVE", ... }
```

**Inactive Customer (after deactivation):**
```json
{ "id": "...", "status": "INACTIVE", ... }
```


#### Response Status Codes

| Status | Description                      |
|--------|----------------------------------|
| `200`  | Customer found and returned      |
| `404`  | Customer not found               |
| `500`  | Unexpected internal server error |

---

### DELETE `/api/v1/shop/customers/{customerId}` — Remove a Customer

Deletes the customer by their `customerId`.

**HTTP Method:** `DELETE`

#### Path Parameters

| Parameter    | Type   | Required | Description                   |
|--------------|--------|----------|-------------------------------|
| `customerId` | `UUID` | Yes      | Unique identifier of the customer |

#### Example Request (curl)

```bash
curl -X DELETE http://localhost:8090/api/v1/shop/customers/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

#### Example Response — `204 No Content`

No response body is returned.

#### Response Status Codes

| Status | Description                      |
|--------|----------------------------------|
| `204`  | Customer removed successfully    |
| `404`  | Customer not found               |
| `500`  | Unexpected internal server error |

---

### PUT `/api/v1/shop/customers/{customerId}/activate` — Activate a Customer

Changes the status of the specified customer to `ACTIVE`.

**HTTP Method:** `PUT`

#### Path Parameters

| Parameter    | Type   | Required | Description                   |
|--------------|--------|----------|-------------------------------|
| `customerId` | `UUID` | Yes      | Unique identifier of the customer |

#### Example Request (curl)

```bash
curl -X PUT http://localhost:8090/api/v1/shop/customers/3fa85f64-5717-4562-b3fc-2c963f66afa6/activate
```

#### Example Response — `204 No Content`

No response body is returned.

#### Response Status Codes

| Status | Description                      |
|--------|----------------------------------|
| `204`  | Customer activated successfully  |
| `404`  | Customer not found               |
| `500`  | Unexpected internal server error |

---

### PUT `/api/v1/customers/{customerId}/deactivate` — Deactivate a Customer

Changes the status of the specified customer to `INACTIVE`.

**HTTP Method:** `PUT`

#### Path Parameters

| Parameter    | Type   | Required | Description                   |
|--------------|--------|----------|-------------------------------|
| `customerId` | `UUID` | Yes      | Unique identifier of the customer |

#### Example Request (curl)

```bash
curl -X PUT http://localhost:8090/api/v1/customers/3fa85f64-5717-4562-b3fc-2c963f66afa6/deactivate
```

#### Example Response — `204 No Content`

No response body is returned.

#### Response Status Codes

| Status | Description                      |
|--------|----------------------------------|
| `204`  | Customer deactivated successfully |
| `404`  | Customer not found               |
| `500`  | Unexpected internal server error |

---

### PUT `/api/v1/customers/{customerId}/address` — Update Customer Address

Updates the primary address of the customer.

**HTTP Method:** `PUT`  
**Request Content-Type:** `application/json`

#### Path Parameters

| Parameter    | Type   | Required | Description                   |
|--------------|--------|----------|-------------------------------|
| `customerId` | `UUID` | Yes      | Unique identifier of the customer |

#### Request Body
Standard `AddressRequestDTO` structure.

#### Example Request (curl)

```bash
curl -X PUT http://localhost:8090/api/v1/customers/3fa85f64-5717-4562-b3fc-2c963f66afa6/address \
  -H "Content-Type: application/json" \
  -d '{
    "street": "Neustraße",
    "number": "5",
    "postcode": "10115",
    "city": "Berlin",
    "country": "Germany"
  }'
```

#### Response Status Codes

| Status | Description                      |
|--------|----------------------------------|
| `204`  | Address updated successfully     |
| `404`  | Customer not found               |
| `400`  | Invalid address data             |
| `500`  | Unexpected internal server error |

---

### PUT `/api/v1/customers/{customerId}/invoice-address` — Update Invoice Address

Updates the invoice address of the customer.

**HTTP Method:** `PUT`  
**Request Content-Type:** `application/json`

#### Path Parameters

| Parameter    | Type   | Required | Description                   |
|--------------|--------|----------|-------------------------------|
| `customerId` | `UUID` | Yes      | Unique identifier of the customer |

#### Request Body
Standard `AddressRequestDTO` structure.

#### Example Request (curl)

```bash
curl -X PUT http://localhost:8090/api/v1/customers/3fa85f64-5717-4562-b3fc-2c963f66afa6/invoice-address \
  -H "Content-Type: application/json" \
  -d '{
    "street": "Neustraße",
    "number": "5",
    "postcode": "10115",
    "city": "Berlin",
    "country": "Germany"
  }'
```

#### Response Status Codes

| Status | Description                      |
|--------|----------------------------------|
| `204`  | Invoice address updated successfully |
| `404`  | Customer not found               |
| `400`  | Invalid address data             |
| `500`  | Unexpected internal server error |

---

### PUT `/api/v1/customers/{customerId}/communication-details` — Update Communication Details

Updates the email and telephone for a customer.

**HTTP Method:** `PUT`  
**Request Content-Type:** `application/json`

#### Path Parameters

| Parameter    | Type   | Required | Description                   |
|--------------|--------|----------|-------------------------------|
| `customerId` | `UUID` | Yes      | Unique identifier of the customer |

#### Request Body
Standard `CommunicationDetailsRequestDTO` structure.

#### Example Request (curl)

```bash
curl -X PUT http://localhost:8090/api/v1/customers/3fa85f64-5717-4562-b3fc-2c963f66afa6/communication-details \
  -H "Content-Type: application/json" \
  -d '{
    "email": "new.email@gmx.de",
    "telephone": "+49 123 456789"
  }'
```

#### Response Status Codes

| Status | Description                                  |
|--------|----------------------------------------------|
| `204`  | Communication details updated successfully  |
| `404`  | Customer not found                           |
| `400`  | Invalid communication data                   |
| `500`  | Unexpected internal server error              |

---

### POST `/api/v1/shop/customers/{customerId}/purchases` — Purchase a Product

Creates a new contract linking the specified customer to a selected product.

**HTTP Method:** `POST`  
**Request Content-Type:** `application/json`  
**Response Content-Type:** `application/json`

#### Path Parameters

| Parameter    | Type   | Required | Description                          |
|--------------|--------|----------|--------------------------------------|
| `customerId` | `UUID` | Yes      | Unique identifier of the customer    |

#### Request Body

| Field       | Type   | Required | Constraints                      |
|-------------|--------|----------|----------------------------------|
| `productId` | `UUID` | Yes      | Not null, UUID of the product    |

#### Example Request (curl)

```bash
curl -X POST http://localhost:8090/api/v1/shop/customers/3fa85f64-5717-4562-b3fc-2c963f66afa6/purchases \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "7cb65f12-1234-4abc-a789-000000000001"
  }'
```

#### Example Response — `201 Created`

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customerId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "productId": "7cb65f12-1234-4abc-a789-000000000001",
  "creationDate": "2026-02-25",
  "status": "ACTIVE"
}
```

#### Response Status Codes

| Status | Description                                        |
|--------|----------------------------------------------------|
| `201`  | Contract created successfully                      |
| `400`  | Validation failed — invalid or missing `productId` |
| `404`  | Customer or product not found                      |
| `500`  | Unexpected internal server error                   |

---

## Contract API

**Tag:** `Contract`  
**Base path:** `/api/v1/shop/contracts`

Provides management and read access to contracts associated with a specific customer.

---

### GET `/api/v1/shop/contracts/{customerId}` — Load All Contracts for a Customer

Returns all contracts associated with the specified customer UUID.

**HTTP Method:** `GET`  
**Response Content-Type:** `application/json`

#### Path Parameters

| Parameter    | Type   | Required | Description                       |
|--------------|--------|----------|-----------------------------------|
| `customerId` | `UUID` | Yes      | Unique identifier of the customer |

#### Example Request (curl)

```bash
curl -X GET http://localhost:8090/api/v1/shop/contracts/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

#### Example Response — `200 OK`

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "customerId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "productId": "7cb65f12-1234-4abc-a789-000000000001",
    "creationDate": "2026-02-25",
    "status": "ACTIVE"
  }
]
```

#### Response Status Codes

| Status | Description                      |
|--------|----------------------------------|
| `200`  | Contracts returned successfully  |
| `404`  | Customer not found               |
| `500`  | Unexpected internal server error |

---

### PUT `/api/v1/contracts/{contractId}/{customerId}/activate` — Activate a Contract

Changes the status of a specific contract to `ACTIVE`.

**HTTP Method:** `PUT`

#### Path Parameters

| Parameter    | Type   | Required | Description                        |
|--------------|--------|----------|------------------------------------|
| `customerId` | `UUID` | Yes      | Unique identifier of the customer  |
| `contractId` | `UUID` | Yes      | Unique identifier of the contract  |

#### Example Request (curl)

```bash
curl -X PUT http://localhost:8090/api/v1/contracts/a1b2c3d4-e5f6-7890-abcd-ef1234567890/3fa85f64-5717-4562-b3fc-2c963f66afa6/activate
```

#### Response Status Codes

| Status | Description                                                     |
|--------|-----------------------------------------------------------------|
| `204`  | Contract activated successfully                                 |
| `404`  | Contract or customer not found                                  |
| `422`  | Brand mismatch: contract does not belong to the specified customer |
| `500`  | Unexpected internal server error                                |

---

## Product API

**Tag:** `Product`  
**Base path:** `/api/v1/shop/products`

Provides read access to the product catalog, filtered by brand.

---

### GET `/api/v1/shop/products` — Load All Products for a Brand

Returns all products available for the specified brand.

**HTTP Method:** `GET`  
**Response Content-Type:** `application/json`

#### Query Parameters

| Parameter | Type               | Required | Description                                      |
|-----------|--------------------|----------|--------------------------------------------------|
| `brand`   | `string (Brand enum)` | Yes   | One of: `GMX`, `WEB_DE`, `MAIL_COM`             |

#### Example Response — `200 OK`

```json
[
  {
    "id": "7cb65f12-1234-4abc-a789-000000000001",
    "name": "GMX ProMail",
    "brand": "GMX",
    "setupFee": 0.00,
    "monthlyFee": 4.99,
    "storageSize": 65536
  }
]
```

#### Response Status Codes

| Status | Description                                     |
|--------|-------------------------------------------------|
| `200`  | Products returned successfully                  |
| `400`  | Invalid or missing `brand` query parameter      |
| `404`  | Brand not found                                 |
| `500`  | Unexpected internal server error                |

---

## Billing API

**Tag:** `Billing`  
**Base path:** `/api/v1/billing`

Handles invoice generation for customers.

---

### POST `/api/v1/billing/invoices` — Generate Invoice for a Customer

Generates an invoice for the specified customer based on their current active contracts.

**HTTP Method:** `POST`  
**Request Content-Type:** `application/json`  
**Response Content-Type:** `application/json`

#### Request Body

| Field        | Type   | Required | Description                       |
|--------------|--------|----------|-----------------------------------|
| `customerId` | `UUID` | Yes      | Unique identifier of the customer |

#### Example Request (curl)

```bash
curl -X POST http://localhost:8090/api/v1/billing/invoices \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
  }'
```

#### Example Response — `200 OK`

```json
{
  "brand": "GMX",
  "invoiceDate": "2026-02-26",
  "customerId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "address": {
    "street": "Musterstraße",
    "number": "12A",
    "postcode": "68161",
    "city": "Mannheim",
    "country": "Germany"
  },
  "invoiceAddress": null,
  "items": [
    {
      "productId": "7cb65f12-1234-4abc-a789-000000000001",
      "productName": "GMX ProMail",
      "contractId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "contractCreationDate": "2026-02-25",
      "setupFee": 0.00,
      "monthlyFee": 4.99
    }
  ],
  "totalSetupFee": 0.00,
  "totalMonthlyFee": 4.99,
  "discount": 0.00,
  "totalAmount": 4.99
}
```

#### Response Status Codes

| Status | Description                      |
|--------|----------------------------------|
| `200`  | Invoice generated successfully   |
| `404`  | Customer not found               |
| `500`  | Unexpected internal server error |

---

## Common Response Schemas

### `CustomerResponseDTO`

```json
{
  "id": "UUID",
  "firstName": "string",
  "lastName": "string",
  "birthDate": "YYYY-MM-DD",
  "address": "AddressResponseDTO",
  "invoiceAddress": "AddressResponseDTO | null",
  "communicationDetails": "CommunicationDetailsResponseDTO",
  "brand": "GMX | WEB_DE | MAIL_COM",
  "status": "ACTIVE | INACTIVE"
}
```

### `ContractResponseDTO`

```json
{
  "id": "UUID",
  "customerId": "UUID",
  "productId": "UUID",
  "creationDate": "YYYY-MM-DD",
  "status": "ACTIVE | INACTIVE"
}
```

### `ProductResponseDTO`

```json
{
  "id": "UUID",
  "name": "string",
  "brand": "GMX | WEB_DE | MAIL_COM",
  "setupFee": "decimal",
  "monthlyFee": "decimal",
  "storageSize": "long (KB)"
}
```

---

## Error Handling

All error responses share a unified structure defined by `ErrorResponse`:

```json
{
  "correlationId": "string (UUID)",
  "errorCode": "string",
  "message": "string",
  "timestamp": "YYYY-MM-DDTHH:MM:SS"
}
```

| Field           | Description                                                  |
|-----------------|--------------------------------------------------------------|
| `correlationId` | Unique identifier for the request — use for log traceability |
| `errorCode`     | Machine-readable error classification code                   |
| `message`       | Human-readable description of what went wrong                |
| `timestamp`     | UTC timestamp of when the error occurred                     |

### Common HTTP Status Codes

| Status | Meaning                                                         |
|--------|-----------------------------------------------------------------|
| `200`  | OK — request completed successfully                             |
| `201`  | Created — resource was created successfully                     |
| `204`  | No Content — operation completed, no body returned              |
| `400`  | Bad Request — validation failed or malformed input              |
| `404`  | Not Found — the requested resource does not exist               |
| `500`  | Internal Server Error — unexpected server-side failure          |

---

## Monitoring & Observability

This project includes a comprehensive observability stack for monitoring metrics, logs, and system health. All components are pre-configured to work together out of the box.

### Monitoring Components

### 1. Grafana
**URL:** [http://localhost:3000](http://localhost:3000)

The primary visualization platform used to create dashboards and analyze metrics collected from Prometheus and logs aggregated by Loki.

- **Health Endpoint:** [http://localhost:3000/api/health](http://localhost:3000/api/health) — Internal endpoint for debugging service availability and status.

### 2. Prometheus
**URL:** [http://localhost:9090](http://localhost:9090)

The core time-series database and monitoring system that pulls (scrapes) metrics from the application and various infrastructure exporters.

- **Config Check:** [http://localhost:9090/config](http://localhost:9090/config) — Verify the current Prometheus configuration.
- **Graph UI:** [http://localhost:9090/graph](http://localhost:9090/graph) — Built-in interface for quick metric testing and visualization using PromQL.

### 3. Loki
A horizontally scalable, highly available log aggregation system. It allows for efficient log querying and analysis across all containers.

- **Health Check:** [http://localhost:3100/ready](http://localhost:3100/ready) — Returns the readiness status of the Loki instance.
- **Internal Metrics:** [http://localhost:3100/metrics](http://localhost:3100/metrics) — Exposes Loki's own operational metrics for self-monitoring.

### 4. Alloy (Grafana Alloy)
**URL:** [http://localhost:12345](http://localhost:12345)

The distribution of the OpenTelemetry Collector used for collecting, transforming, and forwarding telemetry data (logs and metrics) to the backend storage (Loki/Prometheus).

- **Internal Metrics:** [http://localhost:12345/metrics](http://localhost:12345/metrics) — Exposes Alloy's own metrics for performance monitoring of the collector.

### 5. Prometheus Exporters
Exporters translate internal system states into Prometheus-compatible metrics.

- **PostgreSQL Exporter (Shop):** [http://localhost:9187/metrics](http://localhost:9187/metrics) — Provides detailed database performance and health metrics for the shop database.
- **PostgreSQL Exporter (Keycloak):** [http://localhost:9188/metrics](http://localhost:9188/metrics) — Provides metrics for the Keycloak identity provider database.
- **Redis Exporter:** [http://localhost:9121/metrics](http://localhost:9121/metrics) — Exposes performance, memory usage, and health metrics for the Redis cache instance.

### 6. Spring Boot Application (Observability Chain)
The application exposes management endpoints via Spring Boot Actuator for deep introspection and monitoring.

- **Health Check:** [http://localhost:8090/actuator/health](http://localhost:8090/actuator/health) — Aggregated health status of the application and its critical dependencies.
- **Prometheus Metrics:** [http://localhost:8090/actuator/prometheus](http://localhost:8090/actuator/prometheus) — Native application metrics formatted specifically for Prometheus scraping.

---

### Sentry Error Tracking

**Web UI URL:** [http://localhost:9001](http://localhost:9001)


Sentry provides real-time error tracking and APM monitoring for the application.

#### Architecture & Infrastructure Services
- **Sentry Server:** Self-hosted Sentry instance running `sentry:9.1.2` (container name `sentry`).
- **Sentry Database (`sentry-db`):** Dedicated PostgreSQL instance for Sentry data (`postgres:15-alpine` on host port `5436`).
- **Sentry Redis (`sentry-redis`):** Dedicated Redis cache instance for Sentry queue & caching (`redis:7.0-alpine` on host port `6380`).

---

### ELK Stack (Structured Log Pipeline)


The ELK stack provides a centralized, structured logging pipeline for all infrastructure services.
All logs are written to the shared host directory `./logs/` and ingested by Logstash.

#### 7. Elasticsearch
**URL:** [http://localhost:9200](http://localhost:9200)

The search and analytics engine that stores and indexes all structured logs.

- **Cluster Health:** [http://localhost:9200/_cluster/health](http://localhost:9200/_cluster/health)
- **Index Template:** Verify the registered template:
  ```bash
  curl http://localhost:9200/_index_template/app-logs-template
  ```
- **View Indices:**
  ```bash
  curl http://localhost:9200/_cat/indices?v
  ```

#### 8. Logstash
**Monitoring API:** [http://localhost:9600](http://localhost:9600)

The data processing pipeline that reads log files from `./logs/`, parses them per service, and ships structured documents to Elasticsearch.

| Source File | Service Type | Parser |
|---|---|---|
| `logs/application.log` | `app` | Dissect + Grok (multiline) |
| `logs/shop_db.log` | `shop_db` | Grok PostgreSQL format (multiline) |
| `logs/keycloak-db.log` | `keycloak-db` | Grok PostgreSQL format (multiline) |
| `logs/keycloak.log` | `keycloak` | Grok Quarkus format (multiline) |
| `logs/redis.log` | `redis` | Grok Redis format |

Sensitive values (`TrustedClientToken`, `Sec-MS-GEC`) are automatically **redacted** before indexing.

#### 9. Kibana
**URL:** [http://localhost:5601](http://localhost:5601)

The web UI for exploring, filtering, and visualizing all structured log data stored in Elasticsearch.

---

### Log Persistence

All service logs are written to the `./logs/` directory on the host machine via Docker Bind Mounts.
This directory **survives** `docker compose down -v` because it is not a Docker named volume.

| Container | Log File | Mount |
|---|---|---|
| `app` | `logs/application.log` | `./logs:/app/logs` |
| `shop_db` | `logs/shop_db.log` | `./logs:/var/log/postgresql` |
| `keycloak-db` | `logs/keycloak-db.log` | `./logs:/var/log/postgresql` |
| `keycloak` | `logs/keycloak.log` | `./logs:/opt/keycloak/data/log` |
| `redis` | `logs/redis.log` | `./logs:/var/log/redis` |

The entire `logs/` directory is excluded from Git via `.gitignore`.

---

### How to Visualize

#### Grafana (Metrics & Loki Logs)
1. Access **Grafana** at [http://localhost:3000](http://localhost:3000).
2. Login with default credentials (admin/admin).
3. Navigate to **Explore** in the sidebar.
4. Switch between **Prometheus** (for graphing metrics using PromQL) and **Loki** (for searching logs using LogQL).

#### Kibana (Structured ELK Logs)
1. Access **Kibana** at [http://localhost:5601](http://localhost:5601).
2. Go to **Management** → **Stack Management** → **Data Views** → **Create data view**.
3. Set the Index pattern to `*-logs-*` (or service-specific patterns like `app-logs-*`, `shop_db-logs-*`, `keycloak-logs-*`, etc.) and the timestamp field to `@timestamp`.
4. Navigate to the **Discover** tab to search and filter logs using fields such as `service`, `loglevel`, `correlation_id`, `thread`, `pid`.

---

### ELK Operations

#### Starting the ELK Stack

The ELK services start automatically as part of `docker compose up`. However, `elasticsearch-setup` runs once after Elasticsearch is ready and registers the index template automatically.

```bash
docker compose up -d
```

Verify the template was registered:
```bash
curl -s http://localhost:9200/_index_template/app-logs-template | python3 -m json.tool
```

#### Stopping the ELK Stack

Stop all services without deleting volumes (logs and Elasticsearch data are preserved):
```bash
docker compose down
```

Stop and **wipe all Docker volumes** (Elasticsearch index data is lost, but `./logs/` files on disk are preserved):
```bash
docker compose down -v
```

#### Elasticsearch — Diagnostic Commands

Check cluster health:
```bash
curl -s http://localhost:9200/_cluster/health?pretty
```

List all active log indices:
```bash
curl -s http://localhost:9200/_cat/indices/*-logs-*?v&s=index
```

View the last 5 documents from today's application index:
```bash
curl -s -X GET "http://localhost:9200/app-logs-$(date +%Y.%m.%d)/_search?pretty" \
  -H "Content-Type: application/json" \
  -d '{"sort":[{"@timestamp":{"order":"desc"}}],"size":5}'
```

Delete all log indices (use with caution):
```bash
curl -X DELETE "http://localhost:9200/*-logs-*"
```

#### Logstash — Diagnostics

Check Logstash node stats and pipeline health:
```bash
curl -s http://localhost:9600/_node/stats/pipelines?pretty
```

View Logstash container logs:
```bash
docker logs logstash --tail=100 -f
```

#### Kibana — Useful KQL Filters

Once the Data View `*-logs-*` (or service-specific data views) is created in Kibana **Discover**, the following KQL queries are useful:

| Intent | KQL Query |
|---|---|
| All errors across all services | `loglevel: "ERROR"` |
| Only application errors | `service: "app" and loglevel: "ERROR"` |
| Specific correlation ID tracing | `correlation_id: "<your-uuid>"` |
| Keycloak authentication events | `service: "keycloak" and log_message: *auth*` |
| PostgreSQL slow/error queries | `service: "shop_db" and loglevel: "ERROR"` |
| Redis warnings | `service: "redis" and loglevel: "WARN"` |
| API batch generation failures | `log_message: "Batch generation failed"` |

---

## Testing

### Unit Tests (Java/JUnit)
To run unit tests, do:
```bash
mvn clean test
```

To run a specific test class:
```bash
mvn test -Dtest=BillingServiceTest
```

### Code Coverage (JaCoCo)
To generate the coverage report, run tests with the `dev` profile:
```bash
mvn test -P dev
```
Then open the HTML report:
```bash
open target/site/jacoco/index.html
```

### Full Validation
Runs tests and all quality checks (Spotless, PMD, SpotBugs):
```bash
mvn verify -P dev
```

### End-to-End (E2E) Tests (Python)
To run the full test cycle (build, start server, run tests, stop) use:
```bash
chmod +x scripts/run_e2e.sh
./scripts/run_e2e.sh
```

 ---

For a local CI pipeline, do:
```bash
gitlab-ci-local
```

---

## Dockerization

The project uses **PostgreSQL 16** as the database.

### Exposed Ports
- **Application**: `8090` (maps to internal `8090`) can be changed in the .env file.
- **PostgreSQL**: `5430` (maps to internal `5432`) can be changed in the .env file.

### Local SSL Certificates (HTTPS Setup)

Since Keycloak is configured to run exclusively over HTTPS (`https://keycloak:8443`), you must generate SSL certificates and a Java Truststore locally before starting the services.

#### 1. Generate SSL Certificates
Create the certificates directory and generate the self-signed key and certificate. You can use either `mkcert` (recommended) or `openssl`:

**Option A: Using mkcert (Recommended for local development)**
```bash
mkdir -p certs
mkcert -key-file certs/keycloak-key.pem -cert-file certs/keycloak-cert.pem keycloak localhost 127.0.0.1 ::1
```

**Option B: Using OpenSSL (Fallback)**
```bash
mkdir -p certs
openssl req -x509 -newkey rsa:4096 -nodes -sha256 \
  -keyout certs/keycloak-key.pem \
  -out certs/keycloak-cert.pem \
  -subj "/CN=keycloak" \
  -days 365 \
  -addext "subjectAltName=DNS:keycloak,DNS:localhost,IP:127.0.0.1"
```

#### 2. Create Java Truststore (JKS)
The Spring Boot container requires a truststore containing the Keycloak public certificate to securely connect to the Identity Provider:
```bash
keytool -importcert -noprompt \
  -keystore certs/truststore.jks \
  -storepass changeit \
  -alias keycloak \
  -file certs/keycloak-cert.pem
```

### Running the entire project

To run it locally, you need to execute the following commands (after generating the certificates):

```bash
docker compose down -v
docker compose up -d
bash scripts/2_keycloak_api_setup.sh
```

### Stopping the project
```bash
docker compose down
```

### Wiping Database Data (useful for clean Flyway migrations)
```bash
docker compose down -v
```

### Viewing application logs
```bash
docker compose logs -f app
```

### Monitoring Container Resources
```bash
docker stats
```

### Access the application container shell:
```bash
docker compose exec app bash
```

### Access the PostgreSQL container shell:
```bash
docker compose exec shop_db psql -U dev_user -d shop_db
```

### Restarting only the application (after code changes - rebuilds the app image)
```bash
docker compose up -d --build app
```

---

## CI/CD Pipeline & Deployment

The project supports both **GitLab CI/CD** (configured via `.gitlab-ci.yml`) and **GitHub Actions** (configured via `.github/workflows/ci.yml`) for automated building, testing, and deployment. All deployment logic is externalized into standalone Bash scripts located in the `scripts/` directory.

### GitLab CI/CD Pipeline Stages

The GitLab CI/CD pipeline consists of the following stages executed sequentially:

| Stage | Job | Trigger | Description |
|---|---|---|---|
| `test` | `unit_test_job` | Automatic | Runs `mvn clean install` with a Redis service container |
| `e2e-test` | `e2e_test_job` | Automatic | Executes E2E test suite via `1_docker-e2e-setup.sh` and `3_e2e-test-runner.sh` |
| `package` | `package_job` | Automatic | Builds Docker image, transfers to remote server via `4_docker-package.sh` |
| `deploy` | `deploy_dev` | Manual | Deploys to **dev** namespace via Helm using `5_deploy-dev.sh` |
| `deploy` | `deploy_qa` | Manual | Deploys to **qa** namespace via Helm using `6_deploy-qa.sh` |
| `deploy-live` | `deploy_live` | Manual (tag only) | Deploys to **live** namespace via Helm using `7_deploy-live.sh` |

The `deploy_live` job only triggers on semantic version tags matching `^v\d+\.\d+\.\d+$`.

### GitHub Actions Workflows

The GitHub Actions workflow executes on every push and pull request to any branch. It runs the following jobs in parallel:

* **Build & Unit Tests** (`Java CI — Unit Tests`):
  * Sets up JDK 25 (Temurin).
  * Generates self-signed SSL certificates and Java truststore for Keycloak.
  * Launches infrastructure services (PostgreSQL, Keycloak, Redis) via Docker Compose.
  * Runs Maven test suite (`mvn clean install`).
* **E2E Tests** (`E2E Tests (Docker Compose Stack)`):
  * Sets up Python 3.12 and JDK 25.
  * Orchestrates infrastructure, builds the Spring Boot app, and deploys using `1_docker-e2e-setup.sh`.
  * Runs Python API E2E tests using `3_e2e-test-runner.sh`.
  * Captures and uploads container logs (Keycloak, Postgres, Redis) as artifacts with fixed file permissions to prevent archival failures.

### Deployment Scripts

All scripts source `utils.sh` and `env_loader.sh` to load environment variables from `.env` (with CI/CD variable priority).

| Script | Purpose |
|---|---|
| `scripts/utils.sh` | Shared utilities: logging (`log_info`, `log_error`), dependency checks, `PROJECT_ROOT` resolution |
| `scripts/env_loader.sh` | Loads `.env` variables in safe mode (existing CI/CD variables take precedence), exports `SSH_CMD` and `SCP_CMD` |
| `scripts/4_docker-package.sh` | Builds Docker image, saves as tar archive, transfers to remote server via `rsync`, imports into Docker/K3s |
| `scripts/5_deploy-dev.sh` | Copies Helm chart to remote server, runs `helm upgrade --install` for `mam-dev` namespace with `values-dev.yaml` |
| `scripts/6_deploy-qa.sh` | Copies Helm chart to remote server, runs `helm upgrade --install` for `mam-qa` namespace with `values-qa.yaml` |
| `scripts/7_deploy-live.sh` | Copies Helm chart to remote server, runs `helm upgrade --install` for `mam-live` namespace with `values-live.yaml` |

### Environment Configuration

Deployment scripts rely on the following variables from `.env` (or GitLab CI/CD variables):

| Variable | Description |
|---|---|
| `IMAGE_NAME` | Base name of the Docker image |
| `IMAGE_TAG` | Image tag (defaults to `CI_COMMIT_SHORT_SHA` in CI, or `git rev-parse --short HEAD` locally) |
| `SSH_USER` | Remote server SSH login user |
| `SSH_HOST` | Remote server hostname or IP address |

The `env_loader.sh` constructs `SSH_CMD` and `SCP_CMD` from these variables automatically.

### Helm Chart Structure

The Helm chart is located at `helm/mail-and-media-shop/` and contains environment-specific value overrides:

```
helm/mail-and-media-shop/
├── Chart.yaml
├── values.yaml            # Default values
├── values-dev.yaml        # Dev environment overrides
├── values-qa.yaml         # QA environment overrides
├── values-live.yaml       # Live/Production environment overrides
├── templates/
└── charts/
```

### Running Deployment Locally

To run any deployment script outside of CI/CD (e.g., from a local machine):

```bash
chmod +x scripts/4_docker-package.sh
./scripts/4_docker-package.sh
```

The scripts will load variables from `.env` automatically. Ensure `SSH_USER` and `SSH_HOST` are set.

---

## Security & Authentication (Spring Security + Keycloak)

The application implements a robust security layer using **Spring Security** as an OAuth2 Resource Server and **Keycloak** as the Identity Provider (IdP).

### 1. Secured Endpoints
By default, all API endpoints require authentication (a valid Bearer JWT token) except the following public endpoints:
- `POST /api/v1/shop/customers` (Customer Registration)
- `GET /api/v1/shop/products` (Product Catalog)
- OpenAPI/Swagger UI endpoints (`/swagger-ui/**`, `/v3/api-docs/**`)
- Spring Boot Actuator endpoints (`/actuator/**`)

### 2. JWT Token Validation
The application performs strict token validation using `NimbusJwtDecoder`:
- **Audience Validation**: Ensures the token's `aud` claim contains the designated `clientId` (e.g., `mail-and-media-shop-app`) or `account`.
- **Issuer Validation**: Ensures the token was strictly issued by the trusted Keycloak realm (e.g., `/realms/mail-and-media-shop-realm`).

### 3. Role-Based Access Control (RBAC)
Role extraction is handled by a custom `KeycloakRoleConverter`. It maps Keycloak's JSON structure by extracting roles from the `realm_access.roles` claim and converting them into Spring Security authorities with a `ROLE_` prefix (e.g., `ROLE_USER`, `ROLE_ADMIN`).

---

## Redis Caching
The application uses **Redis** with the **Jedis** client to optimize read-heavy operations using the **Cache-Aside** pattern.
### Caching Strategy
- **Read**: Before hitting the database, the system checks the Redis cache. If data is found (Cache Hit), it's returned immediately. Otherwise (Cache Miss), it loads data from the database and populates the cache for future requests.
- **Write/Update**: When a record is modified, removed, or deactivated, the corresponding cache entry is evicted (`@CacheEvict`) to ensure data consistency.
### Configured Caches
- `customers`: Stores individual customer profiles (keyed by UUID).
- `products`: Stores product lists filtered by Brand (keyed by Brand enum).
### Configuration (TTL)
TTL is externalized and can be configured in `application.yml` or `.env`:
```properties
# Default: 10 minutes (600,000 ms)
spring.cache.redis.time-to-live=600000
```

---

## Maintain Code Cleanliness

### 6. SonarQube – Static Analysis and Quality Gate

Provides comprehensive reports on bugs, vulnerabilities, code smells, and test coverage.

```bash
mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Pdev -B \
  -Dsonar.projectKey=mail-and-media-shop-v2 \
  -Dsonar.projectName='mail-and-media-shop-v2' \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=[YOUR_SONAR_TOKEN] \
  -Dspotbugs.skip=true \
  -Dpmd.skip=true \
  -Ddependency-check.skip=true
```

---

### 1. Spotless – Automatic Code Formatting

Formats all `.java` files in `src`, fixes indentation, line breaks, annotations, and Builder pattern.

```bash
mvn spotless:apply -P dev
```

Checks formatting without modifying files; returns an error if violations are found.

```bash
mvn spotless:check -P dev
```

---

### 2. PMD – Anti-pattern and Code Duplication Detection

Analyzes code according to `pmd-rules.xml`, fails the build if violations are found.

```bash
mvn pmd:check -P dev
```

Generates a Copy/Paste Detection (CPD) report for duplicated code.

```bash
mvn pmd:cpd -P dev
```

---

### 3. SpotBugs + FindSecBugs – Bytecode Analysis and Security

Analyzes `.class` files for `NullPointerException`, race conditions, resource leaks; FindSecBugs adds security checks (SQLi, XSS).

```bash
mvn spotbugs:check -P dev
```

Opens the HTML report showing defect and risk categories.

```bash
open target/spotbugs.html
```

---

### 4. OWASP Dependency-Check / Snyk – Dependency Security

Scans Maven dependencies for known CVEs, generates an HTML report; fails the build on critical vulnerabilities (CVSS ≥ 7).

```bash
mvn org.owasp:dependency-check-maven:check -P dev
```

Scans all projects for vulnerabilities and provides update recommendations.

```bash
snyk test --all-projects
```

---

### 5. Git Pre-commit Hook – Automatic Checks Before Commit

Runs Spotless, PMD, and SpotBugs using the `dev` profile; blocks commit if violations are found.

```bash
.git/hooks/pre-commit
```

Simulates hook execution for testing purposes.

```bash
./.git/hooks/pre-commit
```

---

## Dependency Management & Build

### 1. View Dependency Tree
Use this to identify version conflicts or redundant libraries.
```bash
mvn dependency:tree
```

### 2. Fast Build (Skip Tests)
Use this when you only need common artifacts without running the full test suite.
```bash
mvn clean install -DskipTests
```

### 3. Check Active Profiles
Verify if the `dev` profile is active.
```bash
mvn help:active-profiles
```

---

## Troubleshooting

### 1. Check Port Availability
If the application fails to start with "Address already in use", check what process is using the port.
```bash
lsof -i :8090
```

### 2. Clean Docker System
Remove unused images, networks, and containers.
```bash
docker system prune -f
```

---

## GitLab CI/CD Variables Automation

The project includes an automated script [`scripts/upload_gitlab_variables.sh`](file:///home/bnerushev/Schreibtisch/Project/ALL-mail-and-media-shop-v2/API-mail-and-media-shop-v2/scripts/upload_gitlab_variables.sh) to sync environment variables from your local `.env` file directly to GitLab CI/CD Variables via the GitLab REST API (`https://git.mam.dev/api/v4/projects`).

### Prerequisites
1. A valid `.env` file in the project root directory. If `.env` is missing, the script will abort with an error message:
   ```text
   ERROR: .env file not found (.env). Please create a .env file before running this script.
   ```
2. A GitLab Personal Access Token with `api` scope generated at [https://git.mam.dev/-/profile/personal_access_tokens](https://git.mam.dev/-/profile/personal_access_tokens).

### Usage

Run the script by providing your `GITLAB_TOKEN`:

```bash
GITLAB_TOKEN=your_personal_access_token ./scripts/upload_gitlab_variables.sh
```

### Features
* **Dynamic `.env` Parsing**: Automatically reads all non-empty key-value pairs from `.env` (skipping comments and empty lines).
* **Idempotent Execution**: Attempts to create each variable (`POST`). If the variable already exists in GitLab, it updates its value (`PUT`).
* **Environment Overrides**: Supports overriding `GITLAB_URL` (default: `https://git.mam.dev`) and `PROJECT_PATH` (default: `bnerushev%2Fmail-and-media-shop-v2`).

---

## GitHub Actions Secrets Automation

The project includes an automated script [`scripts/upload_github_secrets.sh`](file:///home/bnerushev/Schreibtisch/Project/ALL-mail-and-media-shop-v2/API-mail-and-media-shop-v2/scripts/upload_github_secrets.sh) to upload necessary repository secrets from your local `.env` file directly to GitHub using the GitHub CLI (`gh`).

### Prerequisites
1. A valid `.env` file in the project root directory.
2. GitHub CLI (`gh`) installed on your system.
3. Authenticated GitHub session (`gh auth login`).

### Usage

1. Check if `gh` is installed:
   ```bash
   gh --version
   ```
2. Log in to your GitHub account:
   ```bash
   gh auth login
   ```
3. Run the upload script:
   ```bash
   ./scripts/upload_github_secrets.sh
   ```

### Features
* **Filtered Secret Upload**: Uploads only the allowed keys required by the CI/CD workflows, ignoring auxiliary and monitoring variables to save GitHub secrets quota.
* **Non-empty validation**: Automatically skips empty configuration keys.