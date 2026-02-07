# AGENTS.md — Architecture & Contribution Rules

## Project Context

Android application written in **Kotlin** using:

* Jetpack Compose (Material 3)
* MVVM (state-driven UI, MVI-inspired)
* Koin for dependency injection
* Room + Flow for persistence
* Single-user, offline-first (MVP scope)

This project is **actively developed** and already structured.
Refactors are allowed **only if they preserve architecture and behavior**.

---

## Core Architectural Principle (NON-NEGOTIABLE)

This is a **state-driven application**.

User experience is defined by:

* ViewModel public APIs
* UiState models
* Explicit user intents (events)

If a screen calls a property or function, **it MUST exist**.
Prefer adapters or incremental changes over breaking changes.

---

## Layer Responsibilities

### UI Layer (Compose)

**Allowed**

* Stateless Composables
* `collectAsState()` from `StateFlow`
* Rendering based on `UiState`
* Emitting `UiEvent` to ViewModel

**Forbidden**

* Accessing repositories or DAOs
* Business logic
* Mutable state outside ViewModel
* Navigation logic inside ViewModel

Composables must be **pure functions of state**.

---

### ViewModel Layer

**Allowed**

* Expose `StateFlow<UiState>`
* Receive `UiEvent`
* Call UseCases
* Handle loading / error / empty states explicitly

**Forbidden**

* Accessing DAOs or Room
* SQL or persistence logic
* Holding Android Context
* Silent side effects

ViewModels orchestrate, they do not decide business rules.

---

### Domain Layer (UseCases)

**Purpose**

* Express business rules
* Validate invariants
* Orchestrate repositories

**Allowed**

* Pure Kotlin
* Calling repositories
* Throwing domain-level errors

**Forbidden**

* Android imports
* Room / SQL
* UI concepts

UseCases are **thin but explicit**.

---

### Data Layer (Repositories + DAOs)

#### Repositories

**Purpose**

* Single source of truth for data writes
* Enforce consistency rules
* Expose `Flow` for reads

**Allowed**

* RoomDatabase transactions
* Combining multiple DAOs
* Mapping entities → domain models

**Forbidden**

* UI logic
* Exposing DAOs outside data layer

Business invariants such as

> “A housing can have only one active lease”
> are enforced **here**, not in UI.

#### DAOs

**Purpose**

* SQL only

**Allowed**

* CRUD
* Queries
* Room annotations

**Forbidden**

* Business logic
* Multi-DAO orchestration

---

## Data Model Rules

* `Housing` can have **0 or 1 active Lease**
* A Lease is **active** when `endDateEpochDay == null`
* Keys (`KeyEntity`) are always attached to a Lease
* Deleting entities is forbidden if it breaks invariants

    * e.g. deleting a Tenant with an active Lease

SQLite cannot enforce all invariants:
**repositories MUST**.

---

## Dependency Injection (Koin)

* `AppModule` is the **single source of truth**
* Do NOT move bindings out of it
* Do NOT inject DAOs into ViewModels
* ViewModels depend only on UseCases

If a class is referenced in `AppModule`, it **must exist**.

---

## Transactions

Any operation that:

* touches multiple tables
* enforces invariants
* represents a single business action

**MUST be atomic**.

Transactions belong in:

* Repository layer
* via `RoomDatabase.withTransaction {}`

Never in:

* ViewModel
* UI
* UseCase

---

## Code Generation (Codex / AI Agents)

AI-generated code is allowed **only if** it respects this file.

### Hard rules for generated code

* Do NOT change existing architecture
* Do NOT rename or move existing classes
* Do NOT introduce new patterns
* Do NOT bypass repositories
* Prefer explicit, boring code over clever abstractions

If something is missing:

* Create it in the correct layer
* Wire it via existing DI structure
* Do not “simplify” by breaking separation

Generated code must:

* compile
* respect layer boundaries
* preserve business rules

---

## Refactoring Policy

Refactors are allowed if:

* behavior is preserved
* architecture boundaries remain intact
* code is simplified, not abstracted away

Allowed:

* Renaming for clarity
* Extracting functions
* Removing dead code
* Improving readability

Forbidden:

* Architectural rewrites
* Pattern swapping (MVVM → MVI, etc.)
* Moving logic across layers

---

## Definition of “Done” (MVP)

A feature is considered done when:

* It compiles
* It respects architecture rules
* State changes are explicit
* No invariant can be violated through UI
* No silent failure exists

---

## Philosophy

This project values:

* Clarity over cleverness
* Explicit state over implicit behavior
* Boring correctness over fragile elegance

If a decision is unclear, choose the option that:

* makes bugs harder
* reasoning easier
* future refactors safer

If unsure where logic belongs, prefer:
Repository → UseCase → ViewModel → UI
Never skip layers.

