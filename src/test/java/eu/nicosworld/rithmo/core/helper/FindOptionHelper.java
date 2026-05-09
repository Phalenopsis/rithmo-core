package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.BoardDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.*;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.*;

public class FindOptionHelper {

    public static UUID findAnyNonSkipOption(GameStatusDTO statusDTO) {
        return statusDTO.possibleDecisions().entrySet()
                .stream()
                .filter(entry -> !entry.getKey().skip())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public static UUID findSkipOption(GameStatusDTO statusDTO) {
        return statusDTO.possibleDecisions().entrySet()
                .stream()
                .filter(entry -> entry.getKey().skip())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public static UUID findDecisionWithCaptures(GameStatusDTO statusDTO, int nbCaptures) {
        return statusDTO.possibleDecisions()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().capturedIdList().size() == nbCaptures)
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow();
    }


    public static UUID findMoveIdByDestination(GameStatusDTO status, Position targetPosition) {
        return status.possibleDecisions()
                .entrySet()
                .stream()
                .filter(entry -> targetPosition.equals(entry.getKey().landing()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow();
    }

    public static List<PreCaptureOptionDTO> findPreCaptureOptions(GameStatusDTO statusDTO) {
        return statusDTO.possibleOptions()
                .values()
                .stream()
                .flatMap(Set::stream)
                .filter(PreCaptureOptionDTO.class::isInstance)
                .map(PreCaptureOptionDTO.class::cast)
                .toList();
    }

//    public static <T extends PlayerOptionDTO> List<T> findPreCaptureOptions(GameStatusDTO status, Class<T> clazz) {
//
//    }

    public static DecisionDTO reconstructPreCaptureDecision(
            PieceDTO pieceDTO,
            Set<PreCaptureOptionDTO> optionList,
            Position expectedLanding) {
        String actorId = pieceDTO.id();
        List<String> capturedIdList = new ArrayList<>();
        for(PreCaptureOptionDTO option : optionList) {
            capturedIdList.add(option.target().id());
        }

        boolean exists = optionList.stream()
                .anyMatch(o -> o.landing().equals(expectedLanding));

        if (!exists) {
            throw new RuntimeException("expected position is not in option");
        }

        return DecisionDTO.preCaptureDecision(actorId, capturedIdList, expectedLanding);
    }

    public static PieceDTO find(BoardDTO boardDTO, Position position) {
        return boardDTO.pieces()
                .stream()
                .filter(p -> p.position().equals(position))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No PieceDTO found"));
    }

    public static PieceDTO findComponent(BoardDTO boardDTO, Position position, int value) {
        return find(boardDTO, position).components()
                .stream()
                .filter(c -> c.value() == value)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No Component found"));
    }
}
