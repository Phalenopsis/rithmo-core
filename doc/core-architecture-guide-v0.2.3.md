# 🧠 Rithmo Core Architecture Guide (v0.2.3)

## Overview

The Rithmo core is a deterministic turn-based engine split into three strict layers:

* **Engine Layer** → computes game rules
* **Application Layer** → transforms engine output into UI + execution mapping
* **UI Layer** → renders state and sends user decisions

The system is designed to be:

* UI-agnostic (Web, JavaFX, AI clients)
* deterministic
* fully server-authoritative

---

# 🧱 High-Level Architecture

```
        ┌──────────────────────┐
        │        UI Layer      │
        │  (Web / JavaFX / AI) │
        └─────────┬────────────┘
                  │
        GameStatusDTO (snapshot)
                  │
                  ▼
        ┌──────────────────────┐
        │ Application Layer    │
        │                      │
        │ GameStatusAssembler  │
        │ DecisionRegistry     │
        │ TurnProcessorFactory │
        └─────────┬────────────┘
                  │
        TurnOption / TurnAction mapping
                  │
                  ▼
        ┌──────────────────────┐
        │     Engine Layer     │
        │                      │
        │ TurnProcessor        │
        │ PhaseResolver        │
        │ ActionApplier        │
        │ VictoryEngine        │
        └──────────────────────┘
```

---

# ⚙️ Engine Layer (Rules & Simulation)

## Responsibility

The engine layer is the **single source of truth for game rules**.

It:

* generates legal options (`TurnOption`)
* applies actions (`TurnAction`)
* advances game state (`TurnState`)
* validates victory / stalemate conditions

---

## Core Components

### TurnProcessor

A deterministic state machine:

* consumes `TurnState`
* optionally consumes `TurnAction`
* produces next `TurnState`

It manages:

* phase transitions
* automatic computation phases
* application phases requiring player input

---

### PhaseResolver

Generates all legal `TurnOption`s for a given phase:

* MOVE
* PRE_CAPTURE
* POST_CAPTURE
* REINTRODUCTION

It does NOT:

* create UI objects
* assign IDs
* persist anything

---

### ActionApplier

Transforms a `TurnAction` into:

* updated `GameState`
* side effects (captures, movement, etc.)

---

### VictoryEngine

Checks if:

* a player has won
* stalemate is reached

---

## Key Principle

> The engine does NOT know anything about UI, decisions, or persistence.

---

# 🧠 Application Layer (Projection & Binding)

## Responsibility

This layer transforms raw engine output into:

* UI data (`PlayerOptionDTO`)
* execution identity (`DecisionDTO`)
* executable mapping (`TurnAction` binding)

It is the **only layer allowed to interpret engine options for UI purposes**.

---

## GameStatusAssembler

Transforms:

```
Game → TurnOption → UI Projection
```

Produces:

* grouped options
* decision bindings
* UI-ready DTOs

---

## DecisionRegistry

Central component responsible for:

### Responsibilities

* assign stable `DecisionDTO.id`
* deduplicate equivalent decisions
* persist mapping:

```
DecisionDTO.id → TurnAction
```

* store executable actions for later retrieval

---

## Key Principle

> UI never receives TurnAction. Only DecisionDTO.id.

---

## Turn Projection Model

The assembler produces:

### PlayerOptionDTO

UI representation of choices

### DecisionDTO

Execution identity

### ExecutableDecision

Internal binding:

```
DecisionDTO ↔ TurnAction
```

---

## Important Design Rule

> Options and decisions are NOT structurally linked by position or index.

All mapping is explicit via registry.

---

# 🖥 UI Layer (Rendering Only)

## Responsibility

The UI:

* renders `GameStatusDTO`
* displays options
* sends selected decision IDs

It must NOT:

* compute rules
* interpret game logic
* derive valid moves
* reconstruct state transitions

---

## Input Contract

The only valid user action is:

```
play(gameId, decisionId)
```

---

## UI State Model

The UI receives:

* `BoardDTO`
* `PlayerOptionDTO`
* `DecisionDTO`
* `PhaseDTO`

and renders them without modification.

---

# 🔁 Execution Flow

## 1. Game Start

```
GameFacade.startGame()
```

→ initializes state
→ enters START phase
→ triggers first automatic transitions

---

## 2. Option Generation

```
TurnProcessor → PhaseResolver
```

→ produces `TurnOption`

---

## 3. Projection

```
GameStatusAssembler
```

Transforms:

* TurnOption → PlayerOptionDTO
* TurnOption → TurnAction
* TurnAction → DecisionDTO
* DecisionDTO → registry mapping

---

## 4. UI Display

```
GameStatusDTO sent to frontend
```

---

## 5. Player Interaction

```
UI selects DecisionDTO.id
```

---

## 6. Execution

```
GameFacade.play(gameId, decisionId)
```

→ lookup in OptionRepository
→ retrieve TurnAction
→ execute via TurnProcessor
→ update GameState

---

## 7. Loop

Cycle repeats until:

* victory
* stalemate
* game end phase

---

# 🧩 Data Model Separation

## Engine Types

* GameState
* TurnState
* TurnOption
* TurnAction

👉 pure logic, no persistence, no UI

---

## Application Types

* DecisionDTO
* PlayerOptionDTO
* ExecutableDecision
* TurnProjection

👉 translation layer

---

## UI Types

* GameStatusDTO
* BoardDTO
* PieceDTO

👉 rendering only

---

# 🔐 Key Design Guarantees

## 1. Determinism

Same input state → same output options

---

## 2. UI Independence

UI can be:

* Web
* JavaFX
* AI agent
* CLI

without engine change

---

## 3. No Index Coupling

No reliance on:

* list order
* array position
* implicit mapping

Everything is explicit via IDs

---

## 4. Execution Safety

Only valid:

```
DecisionDTO.id → TurnAction
```

No direct action exposure to UI

---

# 🔮 Future Extension (Graph / Advanced Model)

This architecture is intentionally compatible with:

### Possible evolutions:

* decision graph (DAG of options)
* multi-step composite actions
* replay system
* AI simulation trees

Because:

✔ execution is ID-based
✔ UI is projection-only
✔ engine is stateless per transition
✔ registry isolates binding logic

---

# 🧠 Mental Model Summary

Think of it as:

```
ENGINE → "What can happen"
APPLICATION → "How UI sees it"
REGISTRY → "What actually executes"
UI → "User choice"
```
