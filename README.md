# Rithmo Core

## Overview

`rithmo-core` is the orchestration layer of the Rithmomachia project.

It manages the full lifecycle of a game, including:

* Turn sequencing & Multi-phase flow (Pre-capture → Move → Post-capture)
* Player decision mapping (from UUIDs to technical Actions)
* Interaction between the Engine and Game State
* Immutable state management

It is **UI-agnostic**, **framework-independent**, and strictly follows a Clean Architecture pattern.

---

# 🧠 Architecture Overview

```text
UI (DTOs) ↔ GameFacade ↔ Core (Domain) ↔ Engine (Rules)
```

* `rithmo-engine` → Pure game rules (stateless computations)
* `rithmo-core` → Game orchestration (this module)
* `rithmo-persistence` → SPI interfaces for saving games and pending actions

---

# ⚙️ Key Components

## GameFacade

Main entry point. It acts as a **Boundary** between the UI and the Domain.

It:

* Converts internal `TurnOption` into `PlayerOptionDTO`
* Manages repository interactions
* Handles dynamic `TurnProcessor` configuration (variants/rules)

---

## TurnProcessor

Central orchestrator.

A **pure function** transforming a `TurnState` into the next one from a `TurnAction`.

It:

* Prevents invalid phase skipping
* Automatically advances through hidden phases (such as `START`)

---

## TurnState & TurnPhase

Immutable snapshots.

### TurnPhase

Defines where the game currently is:

* `PRE_CAPTURE`
* `MOVE`
* `POST_CAPTURE`
* `VICTORY`

### TurnState

Contains:

* Current `GameState`
* Current player
* Available legal choices

---

## ActionApplier

The mutation bridge.

Delegates board transformations to specialized appliers such as:

* `MoveApplier`
* `CaptureApplier`

---

# 🧩 Type Safety: Sealed Hierarchies

The project leverages Java 21 **sealed interfaces** to guarantee exhaustive handling during turn processing.

## TurnOption

Legal choices exposed to the UI.

Examples:

* `MoveOption`
* `SkipPreCaptureOption`

## TurnAction

Concrete player decisions returned to the Core.

Examples:

* `MoveAction`
* `PreCaptureAction`

**Benefit:** exhaustive pattern matching ensures every legal action is handled by the compiler.

---

# 🧠 Architecture Rules (Critical)

## 1. Engine = Pure Query Only

The Engine answers:

> “What is legal?”

It computes:

* Moves
* Captures
* Victory conditions

It must never:

* Mutate state
* Know about turn phases

---

## 2. Core = Orchestration Only

The Core answers:

> “What happens next?”

It must never:

* Reimplement movement logic
* Reimplement capture rules

---

## 3. Persistence = SPI-Based

The Core defines:

* `GameRepository`
* `OptionRepository`

Implementations belong to infrastructure layers:

* SQL
* Redis
* In-memory
* etc.

---

# 🛠️ Development Environment

## Git Hooks

This project uses a local `pre-commit` hook to enforce code hygiene before every commit.

The hook automatically:

* Runs the Spotless formatter
* Rejects commits containing accidental `TestDebugger` usage
* Ensures formatting consistency across the codebase

Enable hooks after cloning:

```bash
git config core.hooksPath .githooks
```

---

## Code Formatting

This project uses Spotless with Google Java Format.

Formatting is enforced automatically through the pre-commit hook.

### Manual formatting

Format the whole project:

```bash
mvn spotless:apply
```

Validate formatting without modifying files:

```bash
mvn spotless:check
```

---

### Formatter Rules

The formatter automatically handles:

* Import ordering
* Removal of unused imports
* Indentation normalization
* Empty-line cleanup
* Standardized Java formatting

---

### Preserving intentional formatting

Some test assertions use visual indentation to express decision trees.

For these rare cases, formatting can be locally disabled:

```java
// spotless:off
StatusDTOAssertion.from(nextStatus)
        .status()
            .isInPostCapturePhase()
        .decisions()
            .canCaptureInOneDecision("WT12(2,1)")
            .hasCaptureCiblesFor("BC4(1,2)", "WT12(2,1)")
            .cannotCaptureWith("BC8(1,0)", "WT12(2,1)");
// spotless:on
```

Use this sparingly and only when formatting carries semantic readability.

---

# 🚀 Getting Started

Instantiate the `GameFacade` with repository implementations:

```java
GameFacade rithmo = new GameFacade(gameRepo, optionRepo);
GameStatusDTO status = rithmo.startGame(options, board);
```

---

# 📄 License

TBD


