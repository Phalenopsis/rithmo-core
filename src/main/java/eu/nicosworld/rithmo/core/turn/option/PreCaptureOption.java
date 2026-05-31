package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.PieceAtPosition;
import eu.nicosworld.rithmo.engine.model.Position;
import java.util.List;

/**
 * Represents a pre-move capture possibility available during a turn.
 *
 * <p>A {@code PreCaptureOption} is produced by the engine before the movement phase and describes a
 * set of captures that may be performed by a single acting piece.
 *
 * <p>This option may generate several executable decisions depending on:
 *
 * <ul>
 *   <li>which captures are selected by the player
 *   <li>which landing position is chosen for redeployment
 * </ul>
 *
 * <p>The acting piece context is embedded directly in the option in order to avoid external board
 * lookups during application-layer projection and UI assembly.
 *
 * @param actor The acting piece together with its current board position.
 * @param captures The capture possibilities available to the actor.
 * @param possibleLandings Valid landing positions available after capture resolution.
 */
public record PreCaptureOption(
    PieceAtPosition actor, List<CaptureAction> captures, List<Position> possibleLandings)
    implements TurnOption {}
