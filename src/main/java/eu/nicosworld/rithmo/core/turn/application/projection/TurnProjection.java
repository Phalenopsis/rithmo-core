package eu.nicosworld.rithmo.core.turn.application.projection;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PlayerOptionDTO;
import java.util.List;

/**
 * UI projection of the current game state for a single decision step.
 *
 * <p>A TurnProjection is the result of transforming engine-level {@code TurnOption}s into
 * UI-consumable structures.
 *
 * <p>It aggregates three independent concerns:
 *
 * <ul>
 *   <li><b>context piece</b>: the piece currently associated with the options
 *   <li><b>available options</b>: UI-visible choices derived from engine TurnOptions
 *   <li><b>executable decisions</b>: bindings between a UI decision and engine actions
 * </ul>
 *
 * <p>This structure is intentionally UI-agnostic. It does not assume any frontend rendering model
 * (web, JavaFX, AI agent, etc.).
 *
 * <p>It replaces previous index-based coupling between options and actions by explicit {@link
 * ExecutableDecision} bindings.
 *
 * <p>This guarantees:
 *
 * <ul>
 *   <li>stable decision identity
 *   <li>deterministic action execution
 *   <li>no reliance on list ordering or positional mapping
 * </ul>
 */
public record TurnProjection(
    PieceDTO piece, List<PlayerOptionDTO> options, List<ExecutableDecision> executableDecisions) {}
