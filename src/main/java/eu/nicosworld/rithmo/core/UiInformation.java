package eu.nicosworld.rithmo.core;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PlayerOptionDTO;
import java.util.Map;
import java.util.Set;

/**
 * Represents:
 * <ul>
 *     <li>the options that the UI can display to the player, including all required information</li>
 *     <li>a collection of possible simplified decisions associated with persisted game actions</li>
 * </ul>
 *
 * @param playerOptionPerPiece
 *         maps each piece to the set of available player options
 *
 * @param possibleDecisions
 *         the set of simplified decisions available to the player
 */
public record UiInformation(
        Map<PieceDTO, Set<PlayerOptionDTO>> playerOptionPerPiece,
        Set<DecisionDTO> possibleDecisions
) {
}
