# 📘 Turn Flow – Rithmo Core

This document describes how a full turn is processed in the Rithmo Core layer.

It defines the responsibilities of:

* the **engine** (pure computation),
* the **core** (game orchestration + state machine),
* and the **player** (decision maker).

---

# 🧠 Architectural Principles

## Engine (Pure Computation Layer)

The engine is **stateless and side-effect free**.

It:

* computes possible moves
* computes possible captures
* evaluates victory conditions

It does NOT know anything about:

* turn phases
* player decisions
* game progression
* state mutations

👉 The engine answers only:

> “What is possible in this exact GameState?”

---

## Core (Orchestration Layer)

The core is responsible for:

* turn progression (state machine)
* applying player actions
* mutating GameState via appliers
* selecting next phase automatically
* skipping phases when not applicable

👉 The core answers:

> “Given this action (or absence of action), what is the next valid state?”

---

# 🔁 Core Design Principle (Important)

The system is **push-based**, not request-based.

👉 The player never asks for actions.

Instead:

> The TurnProcessor always produces a fully prepared TurnState.

---

# 🧩 TurnState Role

`TurnState` is the **single source of truth**.

It contains:

* current `GameState`
* current player
* current phase
* available actions (if any)
* selected actions (if any)

👉 It is immutable and always reflects the current game situation.

---

# 🎬 1. Game Initialization

The game starts with:

* `GameStateFactory` (engine) → builds initial state
* `BoardBuilder` → builds board
* `GameService.startGame()` → creates first `TurnState`

Initial output:

```text
GameState (initial)
→ TurnState(PRE_CAPTURE or MOVE depending on available captures)
```

---

# 🟡 2. Pre-Capture Phase

## Step 1 — Compute captures

```text
CaptureEngine.findCaptures(GameState)
→ List<CaptureAction>
```

---

## Step 2 — Branching logic (IMPORTANT)

### Case A — Captures exist

```text
TurnState(PRE_CAPTURE, availablePreCaptures)
```

👉 Player chooses whether and how to capture.

---

### Case B — No captures available

👉 PRE_CAPTURE phase is skipped entirely

```text
MovementEngine.generateMoves(GameState)
→ TurnState(MOVE)
```

---

## Step 3 — Player decision (only Case A)

```text
PreCaptureAction
```

---

## Step 4 — Apply pre-captures

```text
PreCaptureApplier → GameState
```

---

## Step 5 — Victory check

```text
VictoryEngine.check(GameState)
```

---

## Step 6 — Transition to movement

If no victory:

```text
MovementEngine.generateMoves(GameState)
→ TurnState(MOVE)
```

---

# 🟠 3. Movement Phase

Movement rules depend on previous actions:

| Situation             | Allowed moves             |
| --------------------- | ------------------------- |
| Pre-capture performed | Regular moves only        |
| No pre-capture        | Regular + Irregular moves |

---

## Step 1 — Compute moves

```text
MovementEngine.generateMoves(GameState)
```

---

## Step 2 — Emit state

```text
TurnState(MOVE, availableMoves)
```

---

## Step 3 — Player decision

```text
MoveAction
```

---

## Step 4 — Apply move

```text
MoveApplier → GameState
```

---

## Step 5 — Victory check

```text
VictoryEngine.check(GameState)
```

---

## Step 6 — Post-capture evaluation trigger

If move was **regular**, post-capture phase may occur.

---

# 🔵 4. Post-Capture Phase

## Condition

Post-capture is only available if:

* the move was **regular**
* AND at least one capture exists

---

## Step 1 — Compute captures

```text
CaptureEngine.findCaptures(GameState)
```

---

## Step 2 — Branching logic

### Case A — Captures exist

```text
TurnState(POST_CAPTURE, availablePostCaptures)
```

👉 Player may optionally capture.

---

### Case B — No captures available

👉 POST_CAPTURE is skipped

```text
switchPlayer()
→ TurnState(PRE_CAPTURE or MOVE depending on initial captures)
```

---

## Step 3 — Player decision (only Case A)

```text
PostCaptureAction
```

---

## Step 4 — Apply post-captures

```text
PostCaptureApplier → GameState
```

---

## Step 5 — Victory check

```text
VictoryEngine.check(GameState)
```

---

## Step 6 — End of turn

If no victory:

```text
switchPlayer()
→ TurnState(PRE_CAPTURE or MOVE)
```

---

# 🔴 5. Victory Phase

After every mutation:

```text
VictoryEngine.check(GameState)
```

### Outcomes:

* ✅ Victory → game ends immediately
* ❌ No victory → continue turn flow

---

# 🔁 6. Turn Transition

At the end of a turn:

```text
switch(currentPlayer)
→ new TurnState(PRE_CAPTURE or MOVE)
```

---

# 🧠 Core Flow Summary

Every interaction follows:

```text
TurnState → TurnAction → GameState → TurnState
```

The system is a:

> deterministic, push-based state machine

---

# ⚙️ Engine vs Core Responsibilities

## Engine

* pure computation
* no state mutation
* no awareness of turns

---

## Core

* orchestration
* phase transitions
* state mutations
* decision handling

---

## Appliers (IMPORTANT)

* MoveApplier → applies movement
* PreCaptureApplier → applies pre-capture
* PostCaptureApplier → applies post-capture

👉 ONLY mutators of GameState

---

# ⚠️ Important Rules

## 1. No player requests

Players never call:

* getMoves()
* getCaptures()

👉 They receive TurnState only

---

## 2. Engine is pure

* no mutation
* no side effects

---

## 3. Core is the only mutator

All changes go through appliers.

---

## 4. TurnState is authoritative

It fully describes:

* current state
* possible actions
* phase
* progression

---

# 🧭 Turn State Machine

```text
          ┌────────────────────┐
          │   PRE_CAPTURE      │
          └────────┬───────────┘
                   │
        (skip if no capture)
                   ▼
          ┌────────────────────┐
          │       MOVE         │
          └────────┬───────────┘
                   │
                   ▼
          ┌────────────────────┐
          │   POST_CAPTURE     │
          └────────┬───────────┘
                   │
        (skip if no capture)
                   ▼
          ┌────────────────────┐
          │  NEXT PLAYER       │
          └────────────────────┘
```

---

# 🧠 Final Insight

This system is:

> a **push-based deterministic state machine with conditional phase skipping**

---

# 🚀 Next Steps

* implement TurnProcessor as strict state machine
* finalize appliers (pre / move / post)
* stabilize TurnState as immutable contract
* keep engine fully isolated and pure

---
