# Rithmo Core

## Overview

`rithmo-core` is the orchestration layer of the Rithmomachia project.

It manages the full lifecycle of a game, including:

- turn sequencing
- player decisions
- interaction between engine and game state
- multi-phase turn flow (pre-capture → move → post-capture)

It is **UI-agnostic**, **framework-independent**, and strictly separated from game rules.

---

# 🧠 Architecture Overview

This project is part of a modular system:

- `rithmo-engine` → pure game rules (stateless computations)
- `rithmo-core` → game orchestration (this module)
- `rithmo-spring-app` → REST API (optional)
- `rithmo-javafx-app` → desktop UI (optional)

```

UI → App → Core → Engine

```

---

# 🧩 Responsibilities

## rithmo-core handles

- Game session lifecycle
- Turn state machine
- Player action processing
- Orchestration of engine + appliers
- Game flow transitions

## rithmo-core does NOT handle

- Game rules (engine responsibility)
- Move/capture computation logic
- UI rendering
- Persistence or networking

---

# 🔁 Core Flow Model

A turn is a **state machine**:

1. Pre-capture phase
2. Move phase
3. Post-capture phase
4. Victory check
5. Next player

All transitions are controlled by the `TurnProcessor`.

---

# ⚙️ Key Components

## GameService

Main entry point of the module.

Responsible for:

- starting games
- processing player actions
- exposing current game state

---

## TurnProcessor

Central orchestrator of a turn.

It coordinates:

- Engine queries (moves, captures)
- Appliers (state mutations)
- VictoryEngine evaluation
- TurnState transitions

---

## TurnState

Immutable snapshot of a turn containing:

- current GameState
- current player
- current phase
- available actions (moves / captures)
- selected move (if any)

👉 It does NOT contain business logic.

---

## Appliers

- MoveApplier → applies moves to GameState
- CaptureApplier → applies captures to GameState

👉 They are the ONLY components allowed to mutate GameState.

---

## Engine (external dependency)

- MovementEngine → computes legal moves
- CaptureEngine → computes possible captures
- VictoryEngine → evaluates victory conditions

👉 Engine is **pure and stateless**.

---

# 🧠 Architecture Rules (CRITICAL)

These rules prevent architectural drift.

---

## 1. Engine = PURE QUERY ONLY

✔ Allowed:
- compute moves
- compute captures
- evaluate victory conditions

❌ Forbidden:
- modifying GameState
- storing state
- knowing turn phases or players

👉 Engine answers: *“what is possible?”*

---

## 2. Core = ORCHESTRATION ONLY

✔ Allowed:
- manage TurnState
- call engine
- call appliers
- control turn flow

❌ Forbidden:
- implementing game rules
- duplicating engine logic

👉 Core answers: *“what happens next?”*

---

## 3. Appliers = STATE MUTATION ONLY

✔ Allowed:
- apply move to GameState
- apply capture to GameState

❌ Forbidden:
- computing moves/captures
- decision logic

👉 Appliers answer: *“how does state change?”*

---

## 4. TurnState = FLOW SNAPSHOT ONLY

✔ Allowed:
- GameState
- Player
- Phase
- available options
- selected move

❌ Forbidden:
- business logic
- computed flags
- persistent selection state like `selectedPreCaptures`

👉 TurnState is NOT a decision model.

---

## 5. Actions = USER INTENT ONLY

✔ Allowed:
- represent player choices
- minimal data (Move, positions, etc.)

❌ Forbidden:
- computed engine results
- validation logic

👉 Actions represent: *“what the player wants”*

---

## 6. Resolver = OPTIONS ONLY

✔ Allowed:
- transform GameState → options (CaptureChoice, Move list)

❌ Forbidden:
- applying changes
- modifying state

---

## 🚫 Anti-Patterns (DO NOT REINTRODUCE)

- `selected` inside domain models
- engine logic inside core
- move/capture computation in appliers
- duplicated rules across layers

---

# 🧭 Mental Model

```

Engine   → what is possible
Core     → what happens next
Applier  → how state changes
Action   → player decision

```

---

# 🚀 Future Extensions

- AI player integration
- replay system
- persistence adapters
- multiplayer synchronization

---

# 📄 License

TBD

