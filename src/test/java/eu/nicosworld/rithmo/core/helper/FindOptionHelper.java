package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.BoardDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PreCaptureOptionDTO;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FindOptionHelper {

    public static UUID findAnyNonSkipOption(GameStatusDTO statusDTO) {
        return statusDTO.possibleDecisions()
                .stream()
                .filter(decision -> !decision.skip())
                .map(DecisionDTO::id)
                .findFirst()
                .orElse(null);
    }

    public static UUID findSkipOption(GameStatusDTO statusDTO) {
        return statusDTO.possibleDecisions()
                .stream()
                .filter(DecisionDTO::skip)
                .map(DecisionDTO::id)
                .findFirst()
                .orElse(null);
    }

    public static UUID findDecisionWithCaptures(
            GameStatusDTO statusDTO,
            int nbCaptures
    ) {
        return statusDTO.possibleDecisions()
                .stream()
                .filter(decision ->
                        decision.capturedIdList() != null
                                && decision.capturedIdList().size() == nbCaptures
                )
                .map(DecisionDTO::id)
                .findFirst()
                .orElseThrow();
    }

    public static UUID findMoveIdByDestination(
            GameStatusDTO status,
            Position targetPosition
    ) {
        return status.possibleDecisions()
                .stream()
                .filter(decision ->
                        targetPosition.equals(decision.landing())
                )
                .map(DecisionDTO::id)
                .findFirst()
                .orElseThrow();
    }

    public static List<PreCaptureOptionDTO> findPreCaptureOptions(
            GameStatusDTO statusDTO
    ) {
        return statusDTO.possibleOptions()
                .values()
                .stream()
                .flatMap(Set::stream)
                .filter(PreCaptureOptionDTO.class::isInstance)
                .map(PreCaptureOptionDTO.class::cast)
                .toList();
    }

    public static UUID findPreCaptureDecisionId(
            GameStatusDTO statusDTO,
            PieceDTO pieceDTO,
            Set<PreCaptureOptionDTO> optionList,
            Position expectedLanding
    ) {

        String actorId = pieceDTO.id();

        Set<String> capturedIds = optionList.stream()
                .map(option -> option.target().id())
                .collect(java.util.stream.Collectors.toSet());

        return statusDTO.possibleDecisions()
                .stream()
                .filter(decision -> !decision.skip())
                .filter(decision -> actorId.equals(decision.actorId()))
                .filter(decision -> expectedLanding.equals(decision.landing()))
                .filter(decision ->
                        decision.capturedIdList() != null
                                && decision.capturedIdList().equals(capturedIds)
                )
                .map(DecisionDTO::id)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "No matching decision found"
                        )
                );
    }

    public static PieceDTO find(
            BoardDTO boardDTO,
            Position position
    ) {
        return boardDTO.pieces()
                .stream()
                .filter(p -> p.position().equals(position))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("No PieceDTO found")
                );
    }

    public static PieceDTO findComponent(
            BoardDTO boardDTO,
            Position position,
            int value
    ) {
        return find(boardDTO, position)
                .components()
                .stream()
                .filter(c -> c.value() == value)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("No Component found")
                );
    }
}