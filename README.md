# Maintenance Log Backend

This project is a backend API for managing maintenance history of assets such as machines, vehicles, or equipment.

The system focuses on traceability and data integrity by storing maintenance activities as immutable logs. Each log represents a concrete maintenance action performed on an asset at a specific point in time.

The application is developed incrementally throughout the semester, where new backend technologies and architectural concepts are gradually introduced and integrated into the same system.

### Core concepts
- Assets that require maintenance
- Maintenance logs representing performed work
- Users with different roles (e.g. technician, manager, admin)
- Historical data that should not be modified or deleted

### Initial scope
The initial version of the system focuses on a simple domain model with assets and maintenance logs, exposing basic CRUD functionality through a REST API.

Authentication, authorization, validation, testing, and deployment concerns will be added progressively as the project evolves.

### Goal
The goal of this project is to build a production-ready backend system that demonstrates clean structure, realistic business rules, and continuous technical progression.
