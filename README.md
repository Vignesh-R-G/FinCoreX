FinCoreX
A mini core banking system designed for financial institutions, featuring modules for CASA, Fixed Deposits, Loan Processing, and Repayments. Built with a secure, scalable architecture using Spring Boot, Spring Security, and REST APIs, integrated with PostgreSQL via Hibernate and Spring Data JPA.

Features

CASA Module: Manage Current and Savings Accounts.
Fixed Deposits: Create, track, and process maturity/breakage.
Loan Processing: Handle loan creation, disbursement, and repayments.
Repayments: Automated batch jobs using Spring Batch.
Notifications: Decoupled event-driven alerts via Application Events and Java Mail Sender.
Automated Tasks: Scheduled jobs using Spring Scheduler.
Cross-Cutting Concerns: Implemented with Spring AOP.


Tech Stack

Backend: Spring Boot, Spring Security, Spring Batch, Spring Scheduler
Persistence: Hibernate, Spring Data JPA
Database: PostgreSQL
Other: Java Mail Sender, Application Events, AOP


Architecture

Layered Design: Controller → Service → Repository
Security: Role-based authentication using Spring Security
Batch Processing: Spring Batch for repayments
Event-Driven: Application Events for notifications

