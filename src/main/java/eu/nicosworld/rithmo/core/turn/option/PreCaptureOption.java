package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;

/**
 * Represents a pre-move capture option available during a turn.
 * <p>
 * A pre-capture option contains:
 * <ul>
 *     <li>the list of capture actions performed as part of the option</li>
 *     <li>the possible landing positions where captured pieces may be redeployed</li>
 * </ul>
 *
 * @param captures
 *         capture actions included in this option
 *
 * @param possibleLandings
 *         valid landing positions for captured-piece redeployment
 */
public record PreCaptureOption(
        List<CaptureAction> captures,
        List<Position> possibleLandings
) implements TurnOption {}
