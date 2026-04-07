# Maintenance Log System

## Vision

This project is a backend REST API for managing maintenance operations in industrial environments. The system enables technicians, managers, and administrators to track maintenance activities, manage assets, and monitor employee performance through a role-based access control system.

---

## Links

**Portfolio website:**  
https://jespertandersen.github.io/Portfolio/

**Project overview video (max 5 min):**  
[Your Video URL]

**Deployed application:**  
https://maintenancelog.heltsort.dk/

https://maintenancelog.heltsort.dk/routes

**Source code repository:**  
https://github.com/JesperTAndersen/MaintenanceLog

---

# Architecture

## System Overview

This project implements a layered backend architecture with clear separation of concerns:

- **Controller layer** - REST endpoints handling HTTP requests/responses
- **Service layer** - Business logic and domain rules
- **Persistence layer** - Data access via DAO pattern with interface segregation
- **Security layer** - JWT authentication and role-based authorization

The application follows SOLID principles with emphasis on Interface Segregation Principle (ISP), using atomic interfaces (`ICreateDAO`, `IReadDAO`, `IUpdateDAO`) that compose into larger interfaces as needed.

### Technologies

- **Java 17** - Primary language
- **Javalin 7.x** - Lightweight web framework
- **JPA / Hibernate** - ORM for database access
- **PostgreSQL** - Relational database
- **Maven** - Build and dependency management
- **JWT** - Token-based authentication
- **BCrypt** - Password hashing
- **JUnit 6** - Unit testing
- **RestAssured** - API integration testing
- **Testcontainers** - Database testing with Docker

---

## Architecture Diagram
```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP/JSON
       ▼
┌─────────────────────────────────────────┐
│          Security Layer                 │
│  authenticate() → authorize()           │
│  (JWT validation & role checking)       │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│        Controller Layer                 │
│  AssetController                        │
│  EmployeeController                     │
│  MaintenanceLogController               │
│  SecurityController                     │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│         Service Layer                   │
│  AssetService                           │
│  EmployeeService                        │
│  MaintenanceLogService                  │
│  SecurityService                        │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│       Persistence Layer (DAOs)          │
│  AssetDAO                               │
│  EmployeeDAO                            │
│  MaintenanceLogDAO                      │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│      PostgreSQL Database                │
└─────────────────────────────────────────┘
```

---

## Key Design Decisions

### Interface Segregation Principle (ISP)

The DAO layer implements atomic interfaces that can be composed:
```java
ICreateDAO<T>  // create()
IReadDAO<T>    // get(), getAll()
IUpdateDAO<T>  // update()
ICrudDAO<T>    // Composes all three

IEmployeeDAO = ICrudDAO<Employee> + ISecurityDAO + IEmployeeQueries
```

Services depend only on the operations they require, improving testability and reducing coupling.

### Authentication & Authorization

**Authentication** occurs in the Javalin `beforeMatched` lifecycle hook:
- Validates JWT token signature and expiration
- Stores authenticated user in request context

**Authorization** occurs in a second Javalin `beforeMatched` lifecycle hook (executed after authentication):
- Reads `ctx.routeRoles()` for the matched endpoint
- Verifies user role matches endpoint requirements
- Implements role hierarchy (ADMIN > MANAGER > TECHNICIAN > AUTHENTICATED)

This separation is necessary because `ctx.routeRoles()` is only available after route matching.

### DTO Conversion Layer

The system maintains two separate DTO types:
- **Domain DTOs** (`EmployeeDTO`, `AssetDTO`, etc.) used in controllers and services
- **Library DTO** (`dk.bugelhartmann.UserDTO`) used internally for JWT operations

A conversion method isolates the JWT library from the domain model.

### Role Hierarchy

Each employee has one role in the database. Permissions are expanded at runtime:
```java
ROLE_HIERARCHY = {
    "ADMIN" → ["ADMIN", "MANAGER", "TECHNICIAN", "AUTHENTICATED"],
    "MANAGER" → ["MANAGER", "TECHNICIAN", "AUTHENTICATED"],
    "TECHNICIAN" → ["TECHNICIAN", "AUTHENTICATED"]
}
```

This approach maintains a simple data model while supporting hierarchical permissions.

### Soft Delete Pattern

Employees and assets use an `active` flag rather than physical deletion:
- Preserves referential integrity for historical maintenance logs
- Prevents orphaned records
- Inactive employees cannot authenticate but their work history remains accessible

### Mapper Pattern

Static mapper classes (`AssetMapper`, `EmployeeMapper`, `MaintenanceLogMapper`) handle entity ↔ DTO conversion:
- DTOs are pure records with no constructor logic
- Bidirectional mapping implemented only where required
- Calculated fields (e.g., `lastLogDate`) passed as parameters

---

# Data Model

## ERD
```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│    Employee     │         │ MaintenanceLog   │         │      Asset      │
├─────────────────┤         ├──────────────────┤         ├─────────────────┤
│ employee_id (PK)│◄───────┤performed_by_id(FK)│         │  name           │
│ first_name      │         │ asset_id (FK)    │───────►│   asset_id (PK)  │
│ last_name       │         │ log_id (PK)      │         │  description    │
│ email (UNIQUE)  │         │ performed_date   │         │  active         │
│ phone           │         │ status           │         └─────────────────┘
│ role            │         │ task_type        │
│ password        │         │ comment          │
│ active          │         └──────────────────┘
└─────────────────┘

Relationships:
- Employee ──< MaintenanceLog (one-to-many)
- Asset ──< MaintenanceLog (one-to-many)
```

---

## Entities

### Employee

Represents system users (authenticated users, technicians, managers, administrators).

**Fields:**
- `employee_id` - Primary key
- `first_name`, `last_name` - Employee name
- `email` - Unique login identifier
- `phone` - Contact information
- `role` - Enum: AUTHENTICATED, TECHNICIAN, MANAGER, ADMIN
- `password` - BCrypt hashed
- `active` - Soft delete flag

**Constraints:**
- Email must be unique
- Inactive employees cannot authenticate
- Only MANAGER And ADMIN can create employees
- Passwords must be hashed before storage

---

### Asset

Represents physical equipment or machinery requiring maintenance.

**Fields:**
- `asset_id` - Primary key
- `name` - Asset identifier
- `description` - Asset details
- `active` - Soft delete flag

**Constraints:**
- Only MANAGER and ADMIN can create/modify assets
- Only ADMIN can deactivate assets
- Historical logs remain accessible for inactive assets

---

### MaintenanceLog

Represents a maintenance activity performed on an asset.

**Fields:**
- `log_id` - Primary key
- `asset_id` - Foreign key to Asset
- `performed_by_employee_id` - Foreign key to Employee
- `performed_date` - Timestamp
- `status` - Enum: DONE, PENDING, IN_PROGRESS
- `task_type` - Enum: MAINTENANCE, PRODUCTION, ERROR, INSPECTION
- `comment` - Optional notes

**Constraints:**
- Immutable after creation
- TECHNICIAN+ can create logs
- Referenced asset and employee must be active
- Performed date cannot be in the future

---

# API Documentation

**Base URL:** `/api/v1`

**Authentication:** Most endpoints require `Authorization: Bearer <token>` header

## Authentication Endpoints

| HTTP Method | Endpoint | Required Role | Notes | Success | Common Errors |
|-------------|----------|---------------|-------|---------|--------------|
| POST | /auth/login | None | User authentication | 200 | 401, 403 |
| POST | /auth/register | MANAGER | Create new employee | 201 | 400, 409 |

**Example: Login (request)**
```json
{
    "email": "john.doe@example.com",
    "password": "securePassword123"
}
```

**Example: Login (response)**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "phone": "12345678",
        "role": "TECHNICIAN",
        "active": true
    }
}
```

**Example: Register (request)**
```json
{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "phone": "87654321",
    "role": "TECHNICIAN",
    "password": "securePassword456"
}
```

**Example: Register (response)**
```json
{
    "id": 2,
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "phone": "87654321",
    "role": "TECHNICIAN",
    "active": true
}
```

**Validation Rules:**
- Login requires valid `email` and `password`
- Register requires: `firstName`, `lastName`, `email`, `phone`, `role`, `password`
- `email` must match `^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$`
- `role` must be `TECHNICIAN`, `MANAGER`, or `ADMIN`
- Password is hashed with BCrypt and never returned
- Inactive employees cannot log in (401 error)

---

## Employee Endpoints

| HTTP Method | Endpoint | Required Role | Notes | Success | Common Errors |
|-------------|----------|---------------|-------|---------|--------------|
| GET | /employees | AUTHENTICATED | List employees | 200 | |
| GET | /employees/{id} | AUTHENTICATED | Get employee by ID | 200 | 404 |
| PUT | /employees/{id} | MANAGER | Update employee | 200 | 400, 404, 409 |
| DELETE | /employees/{id} | ADMIN | Deactivate employee (soft delete) | 204 | 404 |
| PATCH | /employees/{id} | ADMIN | Reactivate employee | 204 | 404 |

**Example: Get all employees (response)**
```json
[
    {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "phone": "12345678",
        "role": "TECHNICIAN",
        "active": true
    },
    {
        "id": 2,
        "firstName": "Jane",
        "lastName": "Smith",
        "email": "jane.smith@example.com",
        "phone": "87654321",
        "role": "MANAGER",
        "active": true
    }
]
```

**Example: Update employee (request)**
```json
{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "11111111",
    "role": "MANAGER",
    "active": true
}
```

**Example: Update employee (response)**
```json
{
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "11111111",
    "role": "MANAGER",
    "active": true
}
```

**Query Parameters:**
- `active` (optional): `true` = active employees only, `false` = inactive only, omitted = all employees

**Validation Rules:**
- Update requires all fields: `firstName`, `lastName`, `email`, `phone`, `role`, `active`
- `email` must match `^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$`
- `role` must be `AUTHENTICATED` `TECHNICIAN`, `MANAGER`, or `ADMIN`
- Email must be unique (409 if duplicate)
- Password cannot be updated via this endpoint

---

## Asset Endpoints

| HTTP Method | Endpoint | Required Role | Notes | Success | Common Errors |
|-------------|----------|---------------|-------|---------|--------------|
| POST | /assets | MANAGER       | Create asset | 201 | 400 |
| GET | /assets | AUTHENTICATED | List assets | 200 | |
| GET | /assets/{id} | AUTHENTICATED | Get asset by ID | 200 | 404 |
| PATCH | /assets/{id} | ADMIN         | Activate asset | 204 | 404 |
| DELETE | /assets/{id} | ADMIN         | Deactivate asset | 204 | 404 |

**Example: Create asset (request)**
```json
{
    "name": "Hydraulic Press #3",
    "description": "Main production line hydraulic press",
    "active": true
}
```

**Example: Asset (response)**
```json
{
    "id": 1,
    "name": "Hydraulic Press #3",
    "description": "Main production line hydraulic press",
    "active": true,
    "lastLogDate": null
}
```

**Query Parameters:**
- `active` (optional): `true` = active assets only, `false` = inactive only, omitted = all assets

**Validation Rules:**
- `name` and `description` are required
- `active` defaults to `true` if not provided

---

## Maintenance Log Endpoints (Asset-Scoped)

| HTTP Method | Endpoint | Required Role | Notes | Success | Common Errors |
|-------------|----------|---------------|-------|---------|--------------|
| POST | /assets/{id}/logs | TECHNICIAN | Create log for asset | 201 | 400, 404 |
| GET | /assets/{id}/logs | AUTHENTICATED | Get logs for asset | 200 | 400, 404 |

**Example: Create log (request)**
```json
{
    "performedDate": "2026-03-23T14:30:00",
    "status": "DONE",
    "taskType": "MAINTENANCE",
    "comment": "Replaced hydraulic fluid",
    "performedByEmployeeId": 1
}
```

**Example: Log (response)**
```json
{
    "id": 1,
    "performedDate": "2026-03-23T14:30:00",
    "status": "DONE",
    "taskType": "MAINTENANCE",
    "comment": "Replaced hydraulic fluid",
    "assetId": 1,
    "performedByEmployeeId": 1
}
```

**Query Parameters (GET /assets/{id}/logs):**
- `taskType` (optional): `PRODUCTION`, `MAINTENANCE`, `ERROR`, or `INSPECTION`
- `status` (optional): `PENDING`, `IN_PROGRESS`, or `DONE`

**Validation Rules:**
- All fields required: `performedDate`, `status`, `taskType`, `comment`, `performedByEmployeeId`
- `performedDate` cannot be in the future
- Referenced asset and employee must exist and be active
- `status` must be valid enum value
- `taskType` must be valid enum value

---

## Maintenance Log Endpoints (Standalone)

| HTTP Method | Endpoint | Required Role | Notes | Success | Common Errors |
|-------------|----------|---------------|-------|---------|--------------|
| GET | /logs | AUTHENTICATED | List all logs | 200 | 400 |
| GET | /logs/{id} | AUTHENTICATED | Get log by ID | 200 | 404 |
| GET | /logs/employee/{employeeId} | MANAGER | Logs by employee | 200 | 404 |

**Example: Get all logs (response)**
```json
[
    {
        "id": 1,
        "performedDate": "2026-03-23T14:30:00",
        "status": "DONE",
        "taskType": "MAINTENANCE",
        "comment": "Replaced hydraulic fluid",
        "assetId": 1,
        "performedByEmployeeId": 1
    },
    {
        "id": 2,
        "performedDate": "2026-03-22T10:15:00",
        "status": "PENDING",
        "taskType": "INSPECTION",
        "comment": "Scheduled inspection",
        "assetId": 2,
        "performedByEmployeeId": 2
    }
]
```

**Query Parameters (GET /logs):**
- `status` (optional): Filter by `PENDING`, `IN_PROGRESS`, or `DONE`

**Notes:**
- Logs are immutable (no update/delete operations)
- Logs can only be created via `/assets/{id}/logs`


---

## Role Hierarchy

The system implements a role hierarchy where higher roles inherit permissions from lower roles:

**Permission Inheritance:**
- **ADMIN** can access all MANAGER, TECHNICIAN, and AUTHENTICATED endpoints
- **MANAGER** can access all TECHNICIAN and AUTHENTICATED endpoints
- **TECHNICIAN** can access all AUTHENTICATED endpoints
- **AUTHENTICATED** represents any logged-in user

**Special Cases:**
- Endpoints with no role requirement are public (e.g., `POST /auth/login`)
- Role validation occurs after JWT token authentication but **before the endpoint handler executes**


---

# User Stories

### **US-1: Employee Authentication**

**As an** employee  
**I want to** log in with my email and password  
**So that** I can access the maintenance system securely

**Given** I have valid credentials  
**When** I submit my email and password  
**Then** I receive a JWT token  
**And** I receive my employee profile information  
**And** the token can be used to access protected endpoints

**Definition of Done:**
-  POST `/api/v1/auth/login` endpoint functional
-  Email and password validated
- Password verified using BCrypt
-  JWT token generated and returned
- Employee details returned (excluding password)
-  Invalid credentials return 401 error
-  Inactive employees cannot log in (403 error)

---

### **US-2: Register New Employee**

**As a** manager  
**I want to** register new employees with assigned roles  
**So that** they can access the system with appropriate permissions

**Given** I am logged in as a manager or admin  
**When** I submit employee details with a role  
**Then** the employee account is created with a hashed password  
**And** the employee appears in the active employees list  
**And** the employee can log in immediately

**Definition of Done:**
-  POST `/api/v1/auth/register` endpoint functional
-  Requires MANAGER role or higher
-  Email uniqueness validated (409 if duplicate)
-  Invalid email format rejected (400 error)
-  Password hashed with BCrypt before storage
-  All required fields validated (firstName, lastName, email, phone, role, password)
-  Password never returned in responses
- Role can be set to any valid role ENUM

---

### **US-3: View Employee Directory**

**As an** authenticated employee  
**I want to** view all employees in the system  
**So that** I can find contact information and verify team members

**Given** I am logged in  
**When** I request the employee list  
**Then** I see all employees with their details
**And** passwords are not exposed

**Definition of Done:**
-  GET `/api/v1/employees` endpoint functional
-  Requires AUTHENTICATED role (any logged-in user)
-  Response excludes password field
-  All employee details returned (id, name, email, phone, role, active)

---

### **US-4: Update Employee Information**

**As a** manager  
**I want to** update employee details  
**So that** employee information stays current and roles can be adjusted

**Given** I am logged in as a manager or admin  
**When** I update an employee's information  
**Then** the changes are saved  
**And** email uniqueness is enforced  
**And** the employee's role can be changed

**Definition of Done:**
-  PUT `/api/v1/employees/{id}` endpoint functional
-  Requires MANAGER role or higher
-  All fields can be updated except password
-  Email uniqueness validated (409 if duplicate)
-  Email format validated
-  Employee not found returns 404 error
-  Role can be updated to any valid EmployeeRole

---

### **US-5: Deactivate Employee**

**As an** admin  
**I want to** deactivate employee accounts  
**So that** former employees cannot access the system but their work history is preserved

**Given** I am logged in as an admin  
**When** I deactivate an employee  
**Then** the employee's active status is set to false  
**And** the employee cannot log in  
**And** their historical maintenance logs remain accessible

**Definition of Done:**
-  DELETE `/api/v1/employees/{id}` endpoint functional
-  Requires ADMIN role
-  Employee `active` field set to false (soft delete)
-  Operation is idempotent
-  Employee not found returns 404 error
-  Login attempts by inactive employees return 403 error

---

### **US-6: Reactivate Employee**

**As an** admin  
**I want to** reactivate previously deactivated employees  
**So that** rehired employees can regain system access

**Given** I am logged in as an admin  
**When** I reactivate an employee  
**Then** the employee's active status is set to true  
**And** the employee can log in  
**And** the employee appears in the active employees list

**Definition of Done:**
-  PATCH `/api/v1/employees/{id}` endpoint functional
-  Requires ADMIN role
-  Employee `active` field set to true
-  Operation is idempotent
- Employee not found returns 404 error

---

### **US-7: Create Asset**

**As a** manager  
**I want to** create new assets in the system  
**So that** maintenance activities can be tracked against them

**Given** I am logged in as a manager or admin  
**When** I create a new asset with name and description  
**Then** the asset is created and appears in the active assets list  
**And** the asset is ready to have maintenance logs attached

**Definition of Done:**
- POST `/api/v1/assets` endpoint functional
-  Requires MANAGER role or higher
-  All required fields validated (name, description)
-  Asset defaults to active if not specified
-  Validation failures return 400 error
---

### **US-8: View Asset List**

**As an** authenticated employee  
**I want to** view all assets and filter by status  
**So that** I can find equipment that needs maintenance

**Given** I am logged in  
**When** I request the asset list  
**Then** I see all assets with their details  
**And** I can filter by active or inactive status  
**And** each asset shows when it was last serviced

**Definition of Done:**
-  GET `/api/v1/assets` endpoint functional
-  Requires AUTHENTICATED role (any logged-in user)
-  Query parameter `?active=true/false` filters results
-  Each asset includes lastLogDate field
-  lastLogDate calculated from most recent maintenance log
-  Empty array returned if no assets match filter

---

### **US-9: View Asset Details**

**As an** authenticated employee  
**I want to** view detailed information about a specific asset  
**So that** I can verify I'm working on the correct equipment

**Given** I am logged in  
**When** I request details for a specific asset  
**Then** all asset information is displayed  
**And** the last maintenance date is shown if available

**Definition of Done:**
-  GET `/api/v1/assets/{id}` endpoint functional
-  Requires AUTHENTICATED role
-  Response includes all asset fields (id, name, description, active, lastLogDate)
-  lastLogDate calculated from most recent log (null if no logs)
-  Asset not found returns 404 error

---

### **US-10: Deactivate Asset**

**As an** admin  
**I want to** deactivate assets that are no longer in use  
**So that** they don't clutter active asset lists but history is preserved

**Given** I am logged in as an admin  
**When** I deactivate an asset  
**Then** the asset's active status is set to false  
**And** the asset no longer appears in default asset lists  
**And** all historical maintenance logs remain accessible

**Definition of Done:**
-  DELETE `/api/v1/assets/{id}` endpoint functional
-  Requires ADMIN role
-  Asset `active` field set to false (soft delete)
-  Operation is idempotent
-  Asset not found returns 404 error
-  Deactivated assets appear in `?active=false` query
-  Asset logs still accessible via `/api/v1/assets/{id}/logs`

---

### **US-11: Activate Asset**

**As an** admin  
**I want to** reactivate previously deactivated assets  
**So that** equipment can be returned to service

**Given** I am logged in as a manager or admin  
**When** I activate an asset  
**Then** the asset's active status is set to true  
**And** the asset appears in the active assets list

**Definition of Done:**
-  PATCH `/api/v1/assets/{id}` endpoint functional
-  Requires MANAGER role or higher
-  Asset `active` field set to true
-  Operation is idempotent
-  Asset not found returns 404 error

---

### **US-12: Create Maintenance Log**

**As a** technician  
**I want to** create a maintenance log entry for an asset  
**So that** I can document work performed and maintain an audit trail

**Given** I am logged in as a technician or higher  
**And** I have selected an asset  
**When** I submit a maintenance log with date, status, task type, and comment  
**Then** the log is created and associated with the asset  
**And** the log records me as the performer  
**And** the asset's last log date is updated

**Definition of Done:**
-  POST `/api/v1/assets/{id}/logs` endpoint functional
-  Requires TECHNICIAN role or higher
-  All required fields validated (performedDate, status, taskType, comment, performedByEmployeeId)
-  Log persisted with correct asset and employee relationships
-  Performed date cannot be in the future
-  Referenced asset and employee must be active
-  Response includes log details
-  Asset not found returns 404 error

---

### **US-13: View Asset Maintenance History**

**As an** authenticated employee  
**I want to** view all maintenance logs for a specific asset  
**So that** I can review its maintenance history

**Given** I am logged in  
**When** I request maintenance logs for an asset  
**Then** all logs are displayed  
**And** I can filter by task type  
**And** I can filter by status

**Definition of Done:**
-  GET `/api/v1/assets/{id}/logs` endpoint functional
-  Requires AUTHENTICATED role
-  Query parameters `?taskType=X` and `?status=Y` work correctly
-  Invalid enum values return 400 error with clear message
-  Response includes all log details
-  Empty array returned if no logs exist
-  Asset not found returns 404 error

---

### **US-14: View All Maintenance Logs**

**As an** authenticated employee  
**I want to** view all maintenance logs across all assets  
**So that** I can monitor overall maintenance activity

**Given** I am logged in  
**When** I request all maintenance logs  
**Then** logs from all assets are displayed  
**And** I can filter by status

**Definition of Done:**
-  GET `/api/v1/logs` endpoint functional
-  Requires AUTHENTICATED role
-  Query parameter `?status=X` filters results
-  Invalid status values return 400 error
-  Returns logs across all assets
-  Empty array returned if no logs exist

---

### **US-15: View Maintenance Log Details**

**As an** authenticated employee  
**I want to** view details of a specific maintenance log  
**So that** I can review the work that was performed

**Given** I am logged in  
**When** I request a specific maintenance log  
**Then** all log details are displayed

**Definition of Done:**
-  GET `/api/v1/logs/{id}` endpoint functional
-  Requires AUTHENTICATED role
-  Response includes all log fields
-  Log not found returns 404 error

---

### **US-16: View Logs by Employee**

**As a** manager  
**I want to** view all maintenance logs performed by a specific employee  
**So that** I can review their work quality and productivity

**Given** I am logged in as a manager or admin  
**When** I request logs for a specific employee  
**Then** all logs they performed are displayed across all assets

**Definition of Done:**
-  GET `/api/v1/logs/employee/{employeeId}` endpoint functional
-  Requires MANAGER role or higher
-  Returns logs across all assets for specified employee
-  Employee not found returns 404 error
-  Empty array returned if employee has no logs

---

### **US-17: Role-Based Access Control**

**As a** system administrator  
**I want to** enforce role-based permissions on all endpoints  
**So that** employees can only perform actions appropriate to their role

**Given** endpoints have defined role requirements  
**When** an employee attempts to access an endpoint  
**Then** their role is checked against the requirement  
**And** access is granted if their role is sufficient  
**And** access is denied with error if insufficient

**Definition of Done:**
-  All endpoints have role requirements defined
-  Role hierarchy implemented (ADMIN > MANAGER > TECHNICIAN > AUTHENTICATED)
-  Authentication validates JWT tokens before authorization
-  Authorization checks role matches endpoint requirement
-  Unauthorized and Unauthenticated requests return 403 error
-  Open endpoints (login) accessible without authentication

---

### **US-18: Secure Password Management**

**As a** system administrator  
**I want to** ensure all passwords are securely hashed  
**So that** employee credentials are protected

**Given** passwords are submitted during registration or updates  
**When** passwords are stored in the database  
**Then** they are hashed using BCrypt  
**And** passwords are never returned in API responses  
**And** passwords are never logged

**Definition of Done:**
-  Passwords hashed with BCrypt
-  Password field excluded from all DTO responses
-  Password verification uses BCrypt comparison
-  Plaintext passwords never stored
-  Password updates not allowed via PUT endpoint

---

### **US-19: Immutable Maintenance Logs**

**As a** system administrator  
**I want to** ensure maintenance logs cannot be modified or deleted  
**So that** audit trails remain trustworthy

**Given** maintenance logs exist in the system  
**When** attempts are made to update or delete logs  
**Then** no update or delete endpoints exist for logs  
**And** logs can only be viewed, never modified

**Definition of Done:**
-  No PUT endpoint for maintenance logs
-  No PATCH endpoint for maintenance logs
-  No DELETE endpoint for maintenance logs
-  Logs can only be created via POST
-  MaintenanceLog entity immutable after creation

---

### **US-20: Soft Delete for Data Preservation**

**As a** system administrator  
**I want to** use soft deletes for employees and assets  
**So that** historical data relationships are preserved

**Given** employees and assets can be deactivated  
**When** an employee or asset is deleted  
**Then** the active flag is set to false  
**And** the record remains in the database  
**And** historical maintenance logs remain accessible  
**And** deactivated records can be reactivated if needed

**Definition of Done:**
-  DELETE operations set `active = false`
-  No physical deletion of employee or asset records
-  Foreign key relationships preserved
-  Historical logs remain queryable
-  PATCH endpoints allow reactivation
-  Deactivated employees cannot log in
---

# Development Notes

## Technical Implementation

### JWT Integration

The system uses an external JWT library (`dk.bugelhartmann.TokenSecurity`) that defines its own DTO structure. A conversion layer maintains separation between the library's `UserDTO` and the domain's `EmployeeDTO`.

### Request Lifecycle Security

Authentication and authorization are split across Javalin lifecycle hooks:
- `beforeMatched` authenticates the JWT token after route matching and stores the authenticated user in the request context
- a second `beforeMatched` handler authorizes the request by checking `ctx.routeRoles()` and enforcing role hierarchy

This ensures unauthorized requests are rejected **before** any endpoint handler runs.

### Role-Based Permissions

Single-role assignment in the database with runtime permission expansion via a hierarchy map maintains data model simplicity while supporting complex access control.

### Testing Strategy

Integration tests use RestAssured with Testcontainers for PostgreSQL. Test setup authenticates as different roles to verify authorization rules. TestPopulator seeds the database with BCrypt-hashed passwords for consistent test credentials.

### Interface Segregation

DAOs implement atomic interfaces (`ICreateDAO`, `IReadDAO`, `IUpdateDAO`) that compose into role-specific interfaces. Services depend only on required operations, improving testability and reducing coupling.