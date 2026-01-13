# NEXUS AI Working Rules

This document defines **mandatory rules** for all AI-assisted work in the NEXUS repository.
These rules exist to protect the architecture and design intent of the system.

---

## 1. Source of Truth

The following documents are the **single source of truth** for architecture and design:

- docs/architecture/design-principles.md
- docs/architecture/dependency-rules.md
- docs/architecture/overview.md

If any AI-generated suggestion, existing code, or implementation detail conflicts with these documents,
**the documents ALWAYS take precedence**.

AI tools MUST NOT override or reinterpret these documents.

---

## 2. Architectural Layers

NEXUS follows a strict layered architecture.
Each layer has clear responsibilities and forbidden knowledge.

### 2.1 Domain Layer

**Responsibilities**
- Business concepts and business rules
- Domain models and value objects
- Repository interfaces (business-oriented)

**Must NOT**
- Depend on frameworks (Spring, JPA, JDBC, etc.)
- Reference database concepts or schemas
- Know infrastructure or configuration details

---

### 2.2 Application Layer

**Responsibilities**
- Use cases and orchestration
- Transaction boundaries
- Coordination between domain objects

**Must NOT**
- Access databases directly
- Depend on infrastructure implementations
- Contain SQL, JPA, or DataSource references

---

### 2.3 Infrastructure Layer

**Responsibilities**
- Database access
- External system integration
- Framework and library usage
- Configuration and environment-dependent logic

**Allowed**
- JPA / JDBC / SQL
- DataSource and connection management
- Repository implementations

---

### 2.4 Presentation Layer (nexus-api)

**Responsibilities**
- API endpoints
- Input validation
- Output formatting (DTOs)

**Must NOT**
- Own database connections
- Contain business rules
- Perform persistence logic

---

## 3. Database Rules (Critical)

- Database connection information MUST exist **only** in the infrastructure layer
- nexus-api MUST NOT own or configure DataSource objects
- Domain and Application layers MUST NOT know:
  - DataSource
  - JDBC / JPA
  - Connection URLs or credentials

### Repository Rules

- Repository **interfaces** belong to the domain layer
- Repository **implementations** belong to the infrastructure layer
- JPA repositories are implementation details, not domain contracts

---

## 4. Dependency Rules

- Dependencies MUST point inward
- domain ← application ← infrastructure ← presentation (dependency direction only)
- Higher-level layers MUST NOT depend on lower-level layers

AI MUST NOT introduce new dependency violations.

---

## 5. Forbidden Actions

AI tools MUST NOT:

- Introduce Spring Data JPA into domain or application layers
- Add database logic to nexus-api or presentation
- Bypass repository interfaces
- Restructure architecture without explicit human instruction
- Perform large refactors unrelated to the requested task

---

## 6. AI Behavior Rules

When assisting with this repository, AI tools MUST:

- Prefer minimal, incremental changes
- Explain architectural impact before applying changes
- Ask for clarification if architectural intent is unclear
- Refuse to apply changes that violate these rules

---

## 7. Priority Order

When conflicts arise, the priority order is:

1. Architecture documents (Section 1)
2. This AI rules document
3. Existing code
4. AI-generated suggestions

---

## 8. Guiding Principle

> AI is a coding assistant, not an architect.
> Architectural authority always belongs to humans and documented design.

