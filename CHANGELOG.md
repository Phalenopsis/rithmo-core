# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.0-SNAPSHOT] - in progress

### Breaking Changes

* `CaptureOptionDTO` is now the shared capture option abstraction.
* The former concrete post-capture `CaptureOptionDTO` has been renamed to `PostCaptureOptionDTO`.
* `CaptureOptionDTO` is now implemented by:

  * `PreCaptureOptionDTO`
  * `PostCaptureOptionDTO`

This change clarifies the capture DTO hierarchy and enables unified handling of pre- and post-capture options.

#### Migration

If your code previously referenced:

```java
CaptureOptionDTO
````

for concrete post-capture options, replace it with:

```java
PostCaptureOptionDTO
```

If your code only relies on shared capture metadata (`target`, `type`, `ally`, `justification`), prefer depending on:

```java
CaptureOptionDTO
```


### Added

* **PreDefinedGame**: added a predefined test game with 4 rules
* **Build**: added a filtered test JAR to expose reusable test helpers to downstream test suites
* **FourEightGame Tests**: added tests covering available player options
* **Assertion DSL**:
  * introduced nested specialized assertion scopes for decision, option, asset, board and global status validation
  * introduced unified capture option assertion helpers
* **Assertion Messaging**: added centralized status assertion failure message helpers
* **Assertion Support**: introduced shared internal status assertion support utilities for normalization, piece resolution and decision formatting
* **Code Formatting**: integrated Spotless with Google Java Format and local formatting toggle support (`spotless:off/on`)
* **Git Hooks**: introduced repository-managed pre-commit hooks for automatic formatting and debug statement protection
* **Capture Justification DTOs**:
  * introduced UI-facing capture justification projection model
  * added dedicated DTOs for Encounter, Ambush, Assault and Power capture explanations
  * added framework-agnostic operator and relation DTO enums for client consumption


### Changed
* **Capture DTO Model**:
  * introduced shared `CaptureOptionDTO` abstraction
  * renamed concrete post-capture DTO to `PostCaptureOptionDTO`
  * aligned capture DTO documentation with the new hierarchy
* **Capture Options**:
  * added capture justification metadata to all capture option DTOs
  * exposed rule-specific capture explanations through a shared `CaptureJustificationDTO` hierarchy
  * decoupled client-facing justification contracts from Engine justification implementations
* **E2E Tests**: completed migration of end-to-end scenarios to the fluent nested assertion DSL
* **OptionAssertions**:
  * merged duplicated pre/post capture assertion helpers into unified capture assertions
  * simplified option matching by leveraging shared capture DTO polymorphism
  * removed duplicated capture assertion hierarchy from the DSL
  * added justification assertions through fluent `.because(...)` helpers
* **StatusAssertionMessages**: harmonized error message naming conventions into semantic families (missing, unexpected, incorrect, notFound) and adjusted method visibilities
* **StatusDTOAssertion**:
  * refactored into pre-instantiated nested specialized scopes (`status`, `decisions`, `options`, `assets`, `board`)
  * migrated remaining assertion helpers into specialized scope implementations
  * simplified into a pure DSL entry point by removing legacy flat assertion methods
* **FindDecisionHelper**: extracted `findNonSkipDecision` stream utility, reused it in `findDecisionsFor`, and simplified `findReintroductionIdByDestination` by removing legacy `possibleOptions()` manipulation
* **PieceRepresentationHelper**: refactored piece resolution to explicitly distinguish between board pieces (full representation) and reserve pieces (short representation)
* **Tests (Appliers)**: updated `CaptureAction` factory usage to align with the new justification engine feature
* **CaptureResolver Tests**: refreshed legacy comments for consistency
* **Build Tooling**: standardized project-wide formatting enforcement through Spotless and pre-commit integration

### Tests

* Added tests for multiple captures within the same decision
* Simplified capture option assertions through unified capture DSL helpers
* Added fluent justification assertions for capture option validation
* Validated capture justification propagation from Engine actions to exposed DTO projections
* Updated E2E scenarios to verify mathematical capture explanations through the assertion DSL
* Validated complete assertion DSL migration across end-to-end gameplay coverage
* Validated formatter exclusion support for semantically aligned assertion blocks

### Documentation

* **Capture DTO API**: documented the new capture option abstraction hierarchy and migration path
* **Architecture Tooling**: added project dependency graph generation for structural analysis and refactoring guidance
* **Developer Experience**: documented Spotless usage, formatting conventions and local Git hook setup
* **Capture Justifications**: documented the client-facing justification projection model and capture explanation hierarchy

## [0.2.3] - 2026-05-23

### Changed

* **FindOptionHelper** renamed to **FindDecisionHelper** and extended with additional helper methods
* **StatusDTOAssertion**: added new assertion methods for improved game state validation
* **PreDefinedGame**: renamed `FourEightBoardGame` to `fourEightBoardGame`

---

### Fixed

* **GameFacade**: fixed a real-world bug related to pyramid component naming collision (Pyramid components vs pyramid components)

---

### Refactor

* **GameFacade**:
  * reduced responsibilities by delegating turn processing, decision handling, and UI assembly to dedicated components (`TurnProcessorFactory`, `GameStatusAssembler`, `DecisionRegistry`)

* **TurnProcessorFactory**:
  * extracted processor creation and engine wiring logic
  * centralized capture rule registration and victory rule resolution

* **GameStatusAssembler**:
  * refactored projection pipeline to remove index-based coupling between actions and decisions
  * replaced `PresentationResult` with `TurnProjection`
  * introduced `ExecutableDecision` to explicitly bind `DecisionDTO` and `TurnAction`

* **Presentation Layer**:
  * replaced `PresentationResult` with `TurnProjection`
  * removed parallel list synchronization between actions and decisions
  * introduced explicit binding model via `ExecutableDecision`

* **Decision System**:
  * introduced `DecisionRegistry` to centralize:
    - decision identity generation
    - deduplication of UI decisions
    - persistence of executable actions (`PendingAction`)
  * removed index-based correlation between UI decisions and engine actions
  * strengthened separation between UI projection and engine execution

* **Application Architecture**:
  * clarified separation between:
    - engine execution (`TurnProcessor`, `PhaseResolver`, `ActionApplier`)
    - application projection layer (`GameStatusAssembler`, `DecisionRegistry`)
  * reinforced unidirectional flow:
    `Game → TurnState → Engine Execution → UI Projection → Decision Execution`

* **Core Flow**:
  * simplified execution pipeline to:
    `load → execute → persist → project`

---

### Notes

* Versions 0.2.1 and 0.2.2 were deployed to diagnose a core/engine versioning mismatch issue
---

## [0.2.0-SNAPSHOT] - 2026/05/16

### Added
- **BoardDTO**: Add width and height.
- **PreDefinedGame**: add a method to generate a game for a 8*4 board.
- **Reintroduction Mechanic**:
  - Added support for piece reintroduction from reserve after captures.
  - Added `ReintroductionResolver` to compute valid reintroduction options.
  - Added `ReintroductionOption`, `ReintroductionAction`, and `ReintroductionOptionDTO`.
  - Added `ReintroductionApplier` integration into the turn pipeline.
  - Added support for post-reintroduction capture resolution.
  - Added reserve and captured-assets exposure in `GameStatusDTO`.

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
- **Reserve Rules**:
  - Renamed `hasInReserve` to `hasCapturedEquivalentInReserve`.
  - Added `doesNotHaveCapturedEquivalentInReserve`.
  - Refined reserve matching rules to compare captured piece type, value, and owner equivalence.

### Refactor
- **CaptureResolver**: Centralized subset resolution logic and added landing validation for capture options.
- **Core Capture Flow**: Simplified pre/post capture resolution by extracting shared subset computation.
- **Code Cleanup**: Removed unused imports and translated remaining French comments to English.
- **Turn Options**: Standardized option/action conversion flow using explicit factories where intent mapping is involved.

### Test
- **PreDefinedGameTest**: add some predefined test cases.
- **FindOptionHelper**: add a helper to easily find options in tests.
- **Existing E2E Tests**: refactor to use FindOptionHelper.
- **Capture Component Tests**: add tests for component-level pyramid captures.
- **Existing Tests**: adapt with reserve-piece ownership normalization.
- **Reintroduction Tests**:
  - Added E2E scenarios for reserve reintroduction flow.
  - Added assertions for reserve consistency and reintroduction availability.
  - Added tests for capture-after-reintroduction sequences.
  - Added helpers to locate reintroduction decisions in integration tests.

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
- **Documentation**: added v0.2.0 TurnProcessor UML and UI Integration Guide.

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
