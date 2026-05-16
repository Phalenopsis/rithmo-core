# 📖 UI Integration Guide (v0.2.0)

## Overview
The `GameStatusDTO` is the single source of truth for the UI.

It contains:
- the current board state
- the active player
- the current phase
- all playable decisions
- grouped UI options
- player reserve/captured assets

The frontend should never compute game rules itself.
It should only display the available options returned by the backend.

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
````

---

# Board Representation

## BoardDTO

Contains:

* board dimensions
* all visible pieces currently on the board

```java
BoardDTO {
    int width
    int height
    List<PieceDTO> pieces
}
```

---

## PieceDTO

A `PieceDTO` may represent:

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

* `position == null` means the piece is currently in reserve.
* Pyramids contain their components inside `components`.
* Pyramid components may be targeted individually by captures.

---

# Assets / Reserve

## PlayerAssetsDTO

Each player owns:

* captured pieces
* reserve pieces

```java
PlayerAssetsDTO {
    List<PieceDTO> reserve
    List<PieceDTO> captured
}
```

## UI Usage

### Reserve

Reserve pieces can generate:

* `ReintroductionOptionDTO`

The UI may display reserve pieces in:

* a side panel
* a captured tray
* a "ready for reintroduction" area

### Captured

Captured pieces are informational only.
They are not necessarily reintroducible.

---

# Turn Flow

The UI should react according to the current `PhaseDTO`.

## Available Phases

```java
PRE_CAPTURE
MOVE
POST_CAPTURE
```

---

# Decisions vs Options

## DecisionDTO

A `DecisionDTO` is the actual playable action.

```java
DecisionDTO {
    UUID id
    String actorId
    Set<String> capturedIdList
    Position landing
    boolean skip
}
```

The UI always sends:

```java
play(gameId, decisionId)
```

---

## PlayerOptionDTO

Options are UI-oriented grouped representations.

They help the frontend render:

* move highlights
* capture groups
* reintroduction choices

The UI should render from `possibleOptions`,
but execute using `possibleDecisions`.

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

* Highlight destination square
* Optional movement animation depending on `typeDTO`

---

## 2. PreCaptureOptionDTO

Represents captures occurring BEFORE movement.

```java
PreCaptureOptionDTO {
    PieceDTO target
    CaptureTypeDTO type
    List<PieceDTO> ally
    Position landing
}
```

## Important

Multiple `PreCaptureOptionDTO` may belong to the same decision.

The frontend should group them by:

* attacker
* landing
* captured targets

### Recommended UX

1. User selects attacker
2. UI highlights possible captures
3. UI highlights landing squares
4. UI sends selected `DecisionDTO.id`

---

## 3. PostCaptureOptionDTO

Represents captures occurring AFTER movement.

```java
PostCaptureOptionDTO {
    PieceDTO target
    CaptureTypeDTO type
    List<PieceDTO> ally
}
```

### UI Suggestion

* Show mandatory or optional captures
* Allow selection among available capture groups

---

## 4. ReintroductionOptionDTO

Represents a reserve piece being reintroduced onto the board.

```java
ReintroductionOptionDTO {
    PieceDTO pieceDTO
    Position landing
}
```

## Important

The `pieceDTO.position` is `null`
because the piece comes from reserve.

### UI Suggestion

* Display reserve pieces separately
* Highlight valid reintroduction cells
* Allow drag-and-drop from reserve to board

---

# Option Grouping Strategy

`possibleOptions` is grouped by actor piece.

```java
Map<PieceDTO, Set<PlayerOptionDTO>>
```

This allows the UI to:

* click/select a piece
* display only its associated actions

## Important

For reintroductions:

* the actor piece is a reserve piece (`position == null`)

---

# Recommended UI Flow

## Move Phase

1. User selects a board piece OR reserve piece
2. UI displays associated options
3. User selects a destination
4. UI sends `DecisionDTO.id`

---

## Pre-Capture Phase

1. User selects capture group
2. UI optionally selects landing square
3. UI sends `DecisionDTO.id`

---

## Post-Capture Phase

1. User selects a capture option
2. UI sends `DecisionDTO.id`

---

# Error Handling

The UI should be prepared to catch:

## VictoryException

Display:

* winner screen
* end game modal

---

## PatException

Display:

* stalemate screen
* draw screen

---

## IllegalArgumentException

Usually means:

* expired decision id
* stale frontend state
* invalid replay attempt

The UI should refresh the latest `GameStatusDTO`.

---

# UI Recommendations

## Strongly Recommended

* Render from `possibleOptions`
* Execute from `possibleDecisions`
* Never cache decisions across turns
* Refresh UI after every `play()` call

---

## Recommended Visual Distinction

### Board Pieces

Normal pieces currently on the board.

### Reserve Pieces

Pieces with:

```java
position == null
```

### Captured Pieces

Display separately from reserve if desired.

---

# Reintroduction Notes

Reintroduction is fully backend-driven.

The frontend should:

* never compute legal reintroduction cells
* never infer reserve legality
* only render provided options

A reserve piece may:

* generate reintroduction moves
* immediately become the actor of a post-capture phase

This means:

* a reintroduced piece can instantly capture after placement


