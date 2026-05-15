package eu.nicosworld.rithmo.core.turn.applier;

import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.Position;
import eu.nicosworld.rithmo.engine.move.Move;
import eu.nicosworld.rithmo.engine.move.MoveNature;

public record AppliedResult(
        GameState gameState,
        boolean wasMoveIrregular,
        Position landingPosition

) {
    public static AppliedResult of(GameState gameState) {
        return new AppliedResult(gameState, false, null);
    }

    public static AppliedResult of(GameState gameState, Move move) {
        return new AppliedResult(gameState, move.nature().equals(MoveNature.IRREGULAR), move.to());
    }

    public static AppliedResult withCapture(GameState gameState, Position landingPosition) {
        return new AppliedResult(gameState, false, landingPosition);
    }

    public static AppliedResult withReintroduction(GameState gameState, Position landingPosition) {
        return new AppliedResult(gameState, false, landingPosition);
    }
}
