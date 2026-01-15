# Gojo Contract List – Specification

## Purpose

Provide a **read-only contract list** for Mutual Aid (Gojo) with two scopes:

- **Local (Region-based)**: Operational view for regional offices
- **All (Cross-corporation)**: Group-wide analytical / administrative view

This specification defines **behavioral rules only**.
Persistence, DB, and framework details are intentionally excluded.

---

## Scopes

### 1. Local Contracts List

**Intent**  
View contracts within a single region (業務現場向け).

**Scope characteristics**
- Region-scoped
- Corporation is implicitly determined by region
- Operational use

**UI Route**
/gojo/contracts/local

**API**
GET /api/v1/gojo/contracts/local

**Request Parameters**

| Name | Type | Required | Notes |
|----|----|----|----|
| regionId | string | yes | Region identifier |
| page | int | yes | 0-based |
| size | int | yes | Allowed: 20 / 50 / 100 |

**Rules**
- `page` must be `>= 0`
- `size` must be one of `20, 50, 100`
- Region is mandatory
- Read-only

---

### 2. All Contracts List (Cross-Corporation)

**Intent**  
View contracts across corporations for group-level use.

**Scope characteristics**
- Cross-corporation
- Optional corporation filter
- Administrative / analytical use

**UI Route**
/gojo/contracts/all

**API**
GET /api/v1/gojo/contracts/all

**Request Parameters**

| Name | Type | Required | Notes |
|----|----|----|----|
| corporationId | string | no | When omitted, ALL corporations |
| page | int | yes | 0-based |
| size | int | yes | Allowed: 20 / 50 / 100 |

**Rules**
- `page` must be `>= 0`
- `size` must be one of `20, 50, 100`
- `corporationId` is optional
- **No additional guards** (ALL is explicitly allowed)
- Read-only

---

## Common Pagination Rules

- Pagination is **mandatory**
- Allowed page sizes are fixed: `20 / 50 / 100`
- Maximum size is `100`
- Sorting is implementation-defined (out of scope)

---

## Architectural Constraints

The following rules are **mandatory**:

- Domain layer MUST NOT depend on Spring, JPA, JDBC
- Repository interfaces live in domain layer
- Repository implementations live in infrastructure layer
- API / Application layers MUST NOT use Spring Data JPA directly
- nexus-api MUST NOT own DataSource or JPA configuration
- This use case is **read-only**

Refer to:
- `docs/architecture/design-principles.md`
- `docs/architecture/ai-rules.md`

---

## Non-Goals (Out of Scope)

- Contract detail view
- Update / cancel / hold operations
- Authorization / permission rules
- Sorting / filtering beyond corporation scope
- DB schema definitions

---

## Status

- Local list: Implemented
- All list: Implemented
- Ready for delegation to feature developers