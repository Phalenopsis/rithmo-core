package eu.nicosworld.rithmo.core.helper.assertions;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceShape;
import eu.nicosworld.rithmo.core.helper.PieceRepresentationHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class StatusAssertionSupport {
    private final GameStatusDTO actual;

    public StatusAssertionSupport(GameStatusDTO actual) {
        this.actual = actual;
    }

    String findPieceRepresentationById(String id) {
        return actual.board().pieces()
                .stream()
                .flatMap(piece -> {
                    List<PieceDTO> all = new ArrayList<>();
                    all.add(piece);
                    if (piece.shape() == PieceShape.PYRAMID) {
                        all.addAll(piece.components());
                    }
                    return all.stream();
                })
                .filter(piece -> id.equals(piece.id()))
                .map(PieceRepresentationHelper::toRepresentation)
                .findFirst()
                .orElse(null);
    }

    public String formatPossibleDecisionsForError() {
        return actual.possibleDecisions()
                .stream()
                .filter(d -> !d.skip())
                .map(d -> {
                    if (d.capturedIdList() == null) {
                        return "";
                    }
                    return d.capturedIdList()
                            .stream()
                            .map(this::findPieceRepresentationById)
                            .collect(Collectors.joining(", "));
                })
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" | "));
    }

    public String normalize(String value) {
        return value.replace(" ", "");
    }
}