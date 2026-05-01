# Rithmo Core

## Overview

`rithmo-core` is the orchestration layer of the Rithmomachia project.

It manages the full lifecycle of a game, including:
- Turn sequencing & Multi-phase flow (Pre-capture → Move → Post-capture)
- Player decision mapping (from UUIDs to technical Actions)
- Interaction between the Engine and Game State
- Immutable state management

It is **UI-agnostic**, **framework-independent**, and strictly follows a "Clean Architecture" pattern.

---

# 🧠 Architecture Overview

```text
UI (DTOs) ↔ GameFacade ↔ Core (Domain) ↔ Engine (Rules)
```

- `rithmo-engine` → Pure game rules (stateless computations).
- `rithmo-core` → Game orchestration (this module).
- `rithmo-persistence` → (SPI) Interfaces for saving games and pending actions.

---

# ⚙️ Key Components

## GameFacade (formerly GameService)
Main entry point. It acts as a **Boundary** between the UI and the Domain.
- Converts internal `TurnOption` into `PlayerOptionDTO`.
- Manages the interaction with `Repositories`.
- Handles the dynamic configuration of the `TurnProcessor` (variants/rules).

## TurnProcessor
Central orchestrator. It is a **pure function** that transforms a `TurnState` into the next one based on a `TurnAction`.
- It ensures the player cannot skip mandatory phases.
- It automatically transitions through "hidden" phases (like START).

## TurnState & TurnPhase
Immutable snapshots.
- **TurnPhase**: Defines where we are (`PRE_CAPTURE`, `MOVE`, `POST_CAPTURE`, `VICTORY`).
- **TurnState**: Holds the `GameState`, the current `Player`, and the list of **available choices**.

## ActionApplier
The bridge to mutations. It uses specialized appliers (`MoveApplier`, `CaptureApplier`) to transform the board based on the selected `TurnAction`.

---

# 🧩 Type Safety: Sealed Hierarchies

The project leverages Java 21 **Sealed Interfaces** to ensure total safety during turn processing:

- **TurnOption**: Legal choices presented to the UI (e.g., `MoveOption`, `SkipPreCaptureOption`).
- **TurnAction**: Explicit decisions returned to the Core (e.g., `MoveAction`, `PreCaptureAction`).

*Benefit: The compiler guarantees that every possible player action is handled in the `ActionApplier` switch.*

---

# 🧠 Architecture Rules (CRITICAL)

### 1. Engine = PURE QUERY ONLY
- Engine answers: *“What is legal?”* (Moves, Captures, Victory).
- **Forbidden**: Modifying state or knowing about turn phases.

### 2. Core = ORCHESTRATION ONLY
- Core answers: *“What happens next?”*
- **Forbidden**: Re-implementing movement or capture logic.

### 3. Persistence = SPI BASED
- The Core defines `GameRepository` and `OptionRepository`.
- **Implementation** is left to the infrastructure layer (SQL, Redis, In-Memory).

---

# 🚀 Getting Started

To use the core, instantiate the `GameFacade` with your repository implementations:

```java
GameFacade rithmo = new GameFacade(gameRepo, optionRepo);
GameStatusDTO status = rithmo.startGame(options, board);
```

---

# 📄 License
TBD
