# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
