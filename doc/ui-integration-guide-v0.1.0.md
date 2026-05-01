# 📖 UI Integration Guide (v0.1.0)

## Overview
The `GameStatusDTO` is the single source of truth for the UI. It contains the board state and a list of `PlayerOptionDTO`.

## Option Types

### 1. Simple Unitary Options
For most actions, the UI receives a flat DTO with a `UUID`.
- **MoveOptionDTO**: Contains `from`, `to`, and `nature` (how it moves).
- **SkipOptionDTO**: A simple button "End Phase" or "Pass".
- **PostCaptureOptionDTO**: Contains the `attackerPosition` and a list of `targetPositions`.

**Flow:** UI sends the `UUID` → Backend executes the associated action.

### 2. Grouped Pre-Capture Options
This is the most complex part of the Rithmo UI. Because one capture sequence can end on different landing squares, we group them.

**Structure of `PreCaptureOptionDTO`:**
- `attackerPos`: The piece doing the capture.
- `targetPositions`: List of pieces that will be removed.
- `landingChoices`: A list of `LandingChoiceDTO`, each containing:
    - `id`: The **UUID** to send back to the `play()` method.
    - `landing`: The coordinate where the piece will stay.

**UI Implementation Strategy:**
1. User selects the capture (the targets).
2. UI shows a sub-menu or highlights possible landing squares.
3. User clicks a landing square → UI sends the associated `id`.

---

## Error Handling
The UI should be prepared to catch:
- **VictoryException**: Show a victory screen with the winner's name.
- **PatException**: Show a stalemate/draw screen.
- **IllegalArgumentException**: Usually happens if an `optionId` has expired (cleared by a new turn).
