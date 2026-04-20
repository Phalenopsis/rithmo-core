package eu.nicosworld.rithmo.core.turn.applier;

import eu.nicosworld.rithmo.engine.model.Board;
import eu.nicosworld.rithmo.engine.model.Position;

public class CaptureApplier {
    public Board applyCapture(Board board, Position position) {
        return board.removePiece(position);
    }
}
