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

### JPA vs JDBC Selection

Infrastructure layer MAY use either JPA or JDBC for repository implementations.

**Selection criteria**
- CRUD, state transitions, aggregate persistence → JPA preferred
- List queries, search, reports, cross-domain queries  
  (large JOINs, aggregations, DTO-oriented results) → JDBC preferred
- Performance-critical or SQL-driven operations → JDBC preferred
- N+1 or mapping cost concerns → Consider switching to JDBC

**Important**
- Domain / Application / API layers remain technology-agnostic
- Explicit DTO mapping is recommended for JDBC-based list/report queries
- Layer dependency rules must be strictly followed

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

## 8. Transitional Policy: JPA Annotations in Domain Entities

### Current State (Migration Debt)

Some existing domain entities currently contain JPA annotations (`@Entity`, `@Table`, `@Column`, etc.).
This is **acknowledged migration debt** that will be addressed incrementally.

### Mandatory Rules for New Code

**MUST**
- New domain entities MUST be persistence-agnostic (no Spring/JPA/JDBC annotations)
- Persistence entities MUST be defined in the infrastructure layer
- Mapping between domain models and persistence models MUST be done in infrastructure layer
- Domain repository interfaces MUST remain framework-agnostic (already achieved)

**MUST NOT**
- Add new JPA annotations to existing domain entities
- Create new domain entities with JPA annotations
- Reference database concepts (tables, columns, schemas) in domain layer
- Depend on Spring Data JPA or JPA in domain/application layers

### Do / Don't

| Action | Status | Rationale |
|--------|--------|-----------|
 Create new domain entity with `@Entity` | ❌ **FORBIDDEN** | Domain must not know persistence |
| Create persistence entity in infrastructure | ✅ **REQUIRED** | Persistence is infrastructure concern |
| Add JPA annotations to existing domain entity | ❌ **FORBIDDEN** | Prevents migration progress |
| Map domain ↔ persistence in infrastructure | ✅ **REQUIRED** | Separation of concerns |
| Use domain repository interface in domain/application | ✅ **REQUIRED** | Framework-agnostic contract |

### Incremental Migration Guidelines

When modifying code that touches existing domain entities with JPA annotations:

1. **If adding new fields**: Add to domain entity without JPA annotations. Create/update corresponding ersistence entity in infrastructure layer.
2. **If refactoring entity**: Consider splitting domain model from persistence model as part of the refactor.
3. **If creating new domain entity**: MUST create it without JPA annotations. Create separate persistence entity in infrastructure.
4. **Migration priority**: Migrate entities when they are being actively modified (opportunistic refactoring).
+
### Rationale

Domain entities should represent business concepts, not database schemas.
Separating domain models from persistence models:
- Enables testing domain logic without database
- Allows multiple persistence strategies (JPA, JDBC, NoSQL)
- Prevents infrastructure changes from affecting domain logic
- Maintains clear architectural boundaries

---

 ## 9. Guiding Principle
 
 > AI is a coding assistant, not an architect.
 > Architectural authority always belongs to humans and documented design.
 