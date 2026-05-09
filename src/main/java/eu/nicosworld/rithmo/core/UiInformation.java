package eu.nicosworld.rithmo.core;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PlayerOptionDTO;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Représente,
 * <ul>
 *     <li>les options que l'UI peut montrer au joueur, avec toutes les infos nécessaires</li>
 *     <li>une map qui contient en clé les actions simplifiées possibles et l'UUID de l'action sauvée en base associée</li>
 * </ul>
 *
 * @param playerOptionPerPiece
 * @param possibleDecisions
 */
public record UiInformation(Map<PieceDTO, Set<PlayerOptionDTO>> playerOptionPerPiece,
                            Map<DecisionDTO, UUID> possibleDecisions) {
}
