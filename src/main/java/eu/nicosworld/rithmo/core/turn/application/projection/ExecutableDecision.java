package eu.nicosworld.rithmo.core.turn.application.projection;

import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.turn.action.TurnAction;

/**
 * Binds a UI decision to a single executable engine action.
 *
 * <p>This record represents the execution bridge between:
 *
 * <ul>
 *   <li>the UI-facing decision ({@link DecisionDTO})
 *   <li>the engine-level action ({@link TurnAction})
 * </ul>
 *
 * <p>The UI only manipulates decision identifiers. The engine is responsible for resolving those
 * identifiers into executable actions.
 *
 * <p>This abstraction removes any need for index-based or positional correlation between UI
 * decisions and engine actions.
 *
 * <p>Note: while currently 1-to-1, this structure allows future extension toward composite or
 * multi-action decisions without breaking the API.
 */
public record ExecutableDecision(DecisionDTO decision, TurnAction action) {}
