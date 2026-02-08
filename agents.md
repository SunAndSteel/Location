# AGENTS.md — Android / Compose / MVVM 

## Purpose

This document defines **strict architectural and contribution rules** for this Android project.

It is written for:
- human developers,
- AI coding agents (Codex, Cursor, GPT),
- future maintainers.

The goal is not speed, but **clarity, consistency, and long-term maintainability**.

If a change breaks architecture or intent, it is wrong — even if it compiles.


---

## Core Principles (Non-Negotiable)

- This is a **state-driven Android application**
- UI is a **pure function of state**
- Business logic lives outside the UI
- Dependency direction is enforced
- Refactors must **preserve behavior** unless explicitly requested

> If the UI “knows too much”, the architecture is already leaking.


---

## Architecture Overview

The project follows a **Clean Architecture inspired layout**, adapted for Android.

Typical layers:

- `ui` (Compose screens & components)
- `presentation` (ViewModels, UiState, UiEvent)
- `domain` (business logic)
- `data` (persistence, network, implementations)
- `di` (dependency injection)

### Dependency Direction (Strict)
UI → Presentation → Domain → Data

- UI depends on Presentation
- Presentation depends on Domain
- Domain depends on nothing Android-specific
- Data depends on Domain abstractions
- DI wires everything together

**No reverse dependency is allowed.**


---

## UI Layer (Jetpack Compose)

**Purpose:** render state and emit user intent.

Allowed:
- Stateless Composables
- `collectAsState()` / `collectAsStateWithLifecycle()`
- Rendering based only on `UiState`
- Emitting `UiEvent` to ViewModel

Forbidden:
- Business logic
- Direct repository / DAO access
- Mutable state outside ViewModel
- Hidden side-effects

Rules:
- Composables must be **pure functions of state**
- No `remember { mutableStateOf(...) }` for business state
- UI reacts, it does not decide

If a Composable needs logic, it probably belongs in the ViewModel.


---

## Presentation Layer (ViewModel)

**Purpose:** orchestrate state and intent.

Allowed:
- `StateFlow<UiState>`
- `UiEvent` handling
- Calling UseCases
- Mapping domain results to UI state

Forbidden:
- Direct DAO or network access
- Android UI references (Context, Views, etc.)
- Hidden mutable state

Rules:
- One ViewModel = one screen or feature
- ViewModel exposes **state, not methods**
- All state changes are explicit and traceable

Think of the ViewModel as a **state machine**, not a controller.


---

## State Model

- Each screen has a clearly defined `UiState`
- State represents **what the user perceives**
- Prefer sealed hierarchies for screen modes

Good:
- Loading / Empty / Content / Error states
- Explicit flags instead of implicit conditions

Bad:
- “We’ll just check if the list is empty”
- Deriving UI meaning from raw data


---

## Domain Layer

**Purpose:** express business intent.

Allowed:
- UseCases
- Domain models
- Repository interfaces
- Pure Kotlin

Forbidden:
- Android imports
- Room / Retrofit / framework code
- UI concepts

Rules:
- Domain must compile without Android
- Names express **what**, not **how**
- One use case = one intention

If it sounds like something a user wants to do, it belongs here.


---

## Data Layer

**Purpose:** implement domain contracts.

Allowed:
- Room DAOs
- Network APIs
- Repository implementations
- Entity ↔ Domain mappers

Forbidden:
- UI state logic
- Business rules that affect decisions

Rules:
- Implements domain interfaces exactly
- No leaking DB or API models to upper layers
- Translate technical failures into domain-meaningful results


---

## Dependency Injection

- DI is assembly only, never logic
- Interfaces live in Domain
- Implementations live in Data
- ViewModels receive UseCases only

Forbidden:
- Creating implementations manually in UI
- DI modules containing business decisions

DI should be boring.


---

## Refactoring Rules (Critical)

Allowed:
- Renaming for clarity
- Extracting components
- Improving readability
- Reducing coupling

Forbidden unless explicitly requested:
- Changing behavior
- Breaking ViewModel public APIs
- Large architectural rewrites
- Mixing layers “temporarily”

When refactoring:
1. Preserve behavior
2. Improve structure
3. Update documentation if intent changes


---

## Naming Conventions

- UseCases: verbs (`CreateLease`, `CloseBail`)
- UiState: nouns (`TenantUiState`)
- Events: actions (`OnSaveClicked`)
- Avoid abbreviations unless obvious

If a name needs a comment, the name is wrong.


---

## Error Handling

- No raw exceptions crossing layers
- Errors must be meaningful to the user
- Prefer sealed result types over generic failures

Users don’t care about stack traces.


---

## AI Agent Rules (Very Important)

When acting as an AI agent:

- Respect existing architecture
- Do not introduce new patterns casually
- Prefer minimal, incremental changes
- Never assume intent — infer it from code
- If a change affects architecture, **ask or stop**

An AI that preserves clarity is more valuable than one that writes more code.


---

## Final Rule

If unsure:
- Preserve structure
- Preserve intent
- Make the smallest possible change

Consistency beats cleverness.
