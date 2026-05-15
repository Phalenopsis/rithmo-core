# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0-SNAPSHOT] - In work

### Added
- **BoardDTO**: Add width and height.
- **PreDefinedGame**: add a method to generate a game for a 8*4 board.

### Changed
- **Engine Integration (v0.5.0)**: Adapted core logic to the Engine 0.5.0 redesign, including:
  - Migration to the refactored `capture.model` using record-based `CaptureAction` and `InvolvedPiece`
  - Support for pyramid-aware capture resolution (component vs whole-piece targeting)
  - Alignment with new duplicate-safe capture generation logic (value-based de-duplication in capture rules)
  - Compatibility with updated `PlayerAssets` rules (pyramids cannot be stored in reserve)
  - Preparation for future reintroduction mechanics introduced at engine level
- **Capture Integration**: Updated the core logic to support the refactored `capture.model` from the Engine.
- **Model Migration**: Migrated internal calls from the old `CaptureAction` class to the new record-based structure.
- **Pyramid Handling**: Integrated `InvolvedPiece` within the capture flow, enabling the Core to distinguish between whole pyramid captures and component-specific captures.
- **Dependencies**: Refreshed the Engine dependency to v0.5.0 and updated all related import paths.
- **Piece Model**: Use DTO color in `PieceDTO` rather than engine `Color`.

### Refactor
- **CaptureResolver**: Centralized subset resolution logic and added landing validation for capture options.
- **Core Capture Flow**: Simplified pre/post capture resolution by extracting shared subset computation.
- **Code Cleanup**: Removed unused imports and translated remaining French comments to English.

### Test
- **PreDefinedGameTest**: add some predefined test cases.
- **FindOptionHelper**: add a helper to easily find options in tests.
- **Existing E2E Tests**: refactor to use FindOptionHelper.
- **Capture Component Tests**: add tests for component-level pyramid captures.
- **Existing Tests**: adapt with reserve-piece ownership normalization

### Fixed
- **CaptureApplier**: fixed a bug where pyramids were incorrectly reset when partially captured.
- **ActionApplier**: fixed issue allowing proper capture of pyramid components.
- **Compatibility**: Fixed several breaking changes in the game loop and action appliers caused by the Engine's DTO restructuring.
- **Test Suite**: Updated all core tests to ensure parity with the new capture data structures.
- **Core**: Corrected `PreCaptureOptionDTO` mapping logic to prevent merging different attackers targeting the same position.
- **CaptureResolver**: fixed a bug where captureResolver did not group subsets when a pyramid component was acting as the actor.

### Documentation
- **UiInformation**: translate documentation to English.
- **CaptureResolver**: add Javadoc.
- **PreCaptureOption**: add Javadoc.
- **CaptureApplier**: add Javadoc.


## [0.1.0] - 2026-05-01

### Added
- **Core Engine**: Implementation of the base Rithmomachia game state machine.
- **Sealed Turn System**: Introduced a robust `TurnAction` and `TurnOption` hierarchy using Java 21 sealed interfaces for compile-time safety.
- **Phase Resolution**: Created a `PhaseResolver` to manage transitions between `PRE_CAPTURE`, `MOVE`, and `POST_CAPTURE` phases.
- **Rule Engines**:
    - Movement engine with `RegularMoveGenerator`.
    - Capture engine supporting `Encounter`, `Ambush`, `Assault`, and `Power` rules.
    - Victory engine with `Body` and `Goods` victory conditions.
- **Persistence SPI**: Defined `GameRepository` and `OptionRepository` interfaces for decoupled storage management.
- **Game Facade**: Developed a high-level `GameFacade` to orchestrate the core logic and expose UI-friendly DTOs.
- **DTO Mapping**: Added specialized DTOs for player options, including landing choice grouping for multi-path captures.

### Changed
- **Naming Alignment**: Refactored internal terminology to use `actorPosition` (Core) and `attackerPosition` (Engine) consistently.
- **Architecture**: Moved from a monolithic processor to a decoupled `ActionApplier` / `PhaseResolver` pattern.
- **API Refinement**: Optimized `TurnProcessor.process()` to handle phase-only transitions (automatic starts).

### Fixed
- **Switch Exhaustivity**: Fixed potential runtime errors by ensuring all sealed implementation cases are covered in the `ActionApplier`.
- **Immutability**: Enforced collection immutability in `TurnAction` and `TurnOption` records using `List.copyOf`.

### Removed
- **Obsolete Entry Point**: Removed the `Main` class as the project is now strictly a library/core module.

---

## [0.0.1] - 2026-04-20

### Added
- Doc
- Architecture skeleton
- Appliers (Capture and Move)

### Changed

### Tests
- Tests for Appliers
