package eu.nicosworld.rithmo.core.turn.applier;

import eu.nicosworld.rithmo.engine.model.Board;
import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.PlayerAssets;
import eu.nicosworld.rithmo.engine.reintroduction.Reintroduction;

public class ReintroductionApplier {
    public GameState applyReintroduction(GameState state, Reintroduction action) {
        Board board = state.board();
        Board newBoard = board.addPiece(action.piece(), action.position());

        PlayerAssets assets = state.assetsOfCurrentPlayer();
        PlayerAssets updatedAssets = assets.removeFromReserve(action.piece());

        return state.withBoard(newBoard)
                .withAssets(state.currentPlayer().getColor(), updatedAssets);

    }
}
