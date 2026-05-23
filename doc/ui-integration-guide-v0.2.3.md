# 📖 UI Integration Guide (v0.2.3)

## Overview

The `GameStatusDTO` is the single source of truth for the UI.

It represents a fully computed snapshot of the current game state, produced by the backend after engine resolution and application-layer projection.

It contains:

* the current board state
* the active player
* the current phase
* all UI-renderable options
* all executable decisions (stable identifiers)
* player assets (reserve / captured pieces)

The frontend must never compute game rules or derive legal moves.

It only renders the provided data and sends back selected decision identifiers.

---

# GameStatusDTO Structure

```java
public record GameStatusDTO(
        UUID gameId,
        BoardDTO board,
        PlayerColorDTO currentPlayer,
        PhaseDTO phase,
        Map<PieceDTO, Set<PlayerOptionDTO>> possibleOptions,
        Set<DecisionDTO> possibleDecisions,
        Map<PlayerColorDTO, PlayerAssetsDTO> assets
)
```

---

# Core Architecture Principle

The system is split into three distinct layers:

### 1. Engine Layer

* Generates `TurnOption`
* Applies `TurnAction`
* Advances `TurnState`

### 2. Application Layer

* Converts `TurnOption` → UI projection
* Builds `DecisionDTO`
* Binds execution via `DecisionRegistry`

### 3. UI Layer

* Displays `PlayerOptionDTO`
* Sends back `DecisionDTO.id`
* Never interprets game rules

---

# Board Representation

## BoardDTO

Contains:

* board dimensions
* all pieces currently on the board

```java
BoardDTO {
    int width
    int height
    List<PieceDTO> pieces
}
```

---

## PieceDTO

A `PieceDTO` represents:

* a normal piece
* a pyramid
* a pyramid component

```java
PieceDTO {
    String id
    Position position
    PieceShape shape
    int value
    PlayerColorDTO owner
    List<PieceDTO> components
}
```

### Important Notes

* `position == null` indicates a reserve piece
* pyramids contain their components in `components`
* components can be individually targeted by captures

---

# Assets / Reserve System

## PlayerAssetsDTO

Each player owns:

* reserve pieces
* captured pieces

```java
PlayerAssetsDTO {
    List<PieceDTO> reserve
    List<PieceDTO> captured
}
```

---

## UI Usage

### Reserve Pieces

Reserve pieces can generate:

* `ReintroductionOptionDTO`

They are displayed in a dedicated UI area.

### Captured Pieces

Captured pieces are informational and may or may not be reintroducible depending on game rules.

---

# Turn Flow

The UI must react exclusively based on `PhaseDTO`.

## Available Phases

```java
PRE_CAPTURE
MOVE
POST_CAPTURE
```

---

# Options vs Decisions vs Actions

## PlayerOptionDTO (UI Layer)

A `PlayerOptionDTO` represents a grouped UI-visible choice.

It is:

* purely descriptive
* non-executable
* derived from `TurnOption`

Used for:

* rendering highlights
* grouping possible actions
* UI interaction feedback

---

## DecisionDTO (Execution Identity)

A `DecisionDTO` represents a **stable executable user decision**.

```java
DecisionDTO {
    UUID id
    String actorId
    Set<String> capturedIdList
    Position landing
    boolean skip
}
```

### Important

Although `DecisionDTO` contains gameplay metadata, it is NOT interpreted by the UI for logic purposes.

The only valid execution key is:

```
DecisionDTO.id
```

### Execution Rule

The UI must always execute:

```
play(gameId, decisionId)
```

---

## Execution Model

A decision is not executed directly.

Instead:

```
DecisionDTO.id → backend lookup → TurnAction → TurnProcessor
```

This mapping is handled internally by the `DecisionRegistry`.

---

## PlayerOptionDTO vs DecisionDTO

| Concept         | Purpose               |
| --------------- | --------------------- |
| PlayerOptionDTO | UI rendering          |
| DecisionDTO     | Execution identity    |
| TurnAction      | Engine execution unit |

---

# Option Types

## 1. MoveOptionDTO

Represents a standard movement.

```java
MoveOptionDTO {
    Position to
    MoveTypeDTO typeDTO
}
```

### UI Suggestion

* highlight destination
* animate movement if needed

---

## 2. PreCaptureOptionDTO

Represents capture possibilities BEFORE movement.

```java
PreCaptureOptionDTO {
    PieceDTO target
    CaptureTypeDTO type
    List<PieceDTO> ally
    Position landing
}
```

### Important Behavior

Multiple `PreCaptureOptionDTO` may share the same decision.

The UI should group them by:

* actor piece
* landing position
* capture set

### UX Flow

1. user selects capture set
2. UI highlights landing options
3. UI sends `DecisionDTO.id`

---

## 3. PostCaptureOptionDTO

Represents capture possibilities AFTER movement.

```java
PostCaptureOptionDTO {
    PieceDTO target
    CaptureTypeDTO type
    List<PieceDTO> ally
}
```

### UI Behavior

* optional or mandatory capture groups
* selection triggers decision execution

---

## 4. ReintroductionOptionDTO

Represents a reserve piece re-entering the board.

```java
ReintroductionOptionDTO {
    PieceDTO pieceDTO
    Position landing
}
```

### Important

* reserve pieces have `position == null`
* reintroduction is fully backend-controlled

### UI Behavior

* display reserve area
* highlight valid placement cells
* allow drag or click selection

---

## 5. SkipOptionDTO

Represents an explicit skip of a capture phase.

Skip is only allowed for:

* PRE_CAPTURE
* POST_CAPTURE

Skip is NOT allowed for MOVE phase.

---

# Option Grouping Strategy

`possibleOptions` groups options by actor piece:

```java
Map<PieceDTO, Set<PlayerOptionDTO>>
```

This allows:

* selecting a piece
* displaying only relevant actions

For reserve pieces:

* actor is a `PieceDTO` with `position == null`

---

# Turn Flow (UI Perspective)

## Move Phase

1. select piece
2. display MoveOptionDTO
3. select destination
4. send DecisionDTO.id

---

## Pre-Capture Phase

1. select capture option
2. optionally select landing
3. send DecisionDTO.id

---

## Post-Capture Phase

1. select capture option
2. send DecisionDTO.id

---

# Execution Lifecycle

1. Engine generates `TurnOption`
2. Application layer builds:

    * PlayerOptionDTO (UI view)
    * DecisionDTO (execution identity)
    * TurnAction (engine command)
3. DecisionRegistry binds:

    * DecisionDTO → TurnAction
4. GameStatusDTO is returned to UI
5. UI sends selected `DecisionDTO.id`
6. Backend resolves and executes `TurnAction`

---

# Error Handling

## VictoryException

* game ends
* display winner screen

## PatException

* stalemate
* display draw screen

## IllegalArgumentException

Usually:

* expired decision
* stale UI state
* invalid replay

UI should refresh `GameStatusDTO`.

---

# UI Recommendations

## Must

* render from `possibleOptions`
* execute via `possibleDecisions`
* never cache decisions across turns
* refresh state after every `play()`

---

## Visual Guidelines

### Board Pieces

Active pieces on board

### Reserve Pieces

`position == null`

### Captured Pieces

Informational display only

---

# Reintroduction Notes

Reintroduction is fully backend-driven.

The UI must never:

* compute valid cells
* infer legality
* simulate placement rules

A reintroduced piece may:

* immediately participate in post-capture phase
* trigger new capture sequences depending on game rules

