package eu.nicosworld.rithmo.core.turn.applier;

import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.*;
import java.util.List;

/**
 * Applies capture actions to the game state.
 *
 * <p>This applier is responsible for:
 *
 * <ul>
 *   <li>removing captured pieces or pyramid components from the board
 *   <li>updating pyramids after partial captures
 *   <li>transferring captured pieces into the current player's assets
 * </ul>
 *
 * <p>Whole captures of non-pyramid pieces are stored for potential redeployment, while partial
 * captures are added as captured components.
 */
public class CaptureApplier {

  /**
   * Applies a single capture action to the game state.
   *
   * @param state current game state
   * @param action capture action to apply
   * @return updated game state after the capture
   */
  public GameState applyCapture(GameState state, CaptureAction action) {
    Board board = state.board();
    Piece capturedPiece = action.capturedPiece();

    if (action.isWholeCapture()) {
      board = board.removePiece(action.targetPosition());
    } else if (action.target().parentPiece() instanceof Pyramid) {
      Pyramid pyramid = (Pyramid) board.getPieceAt(action.targetPosition());
      Pyramid updated = pyramid.removeComponent(capturedPiece);
      board = board.removePiece(action.targetPosition());
      if (!updated.getComponents().isEmpty()) {
        board = board.addPiece(updated, action.targetPosition());
      }
    }

    PlayerAssets assets = state.assetsOfCurrentPlayer();
    PlayerAssets updatedAssets =
        (action.isWholeCapture() && !capturedPiece.isPyramid())
            ? assets.captureAndStore(capturedPiece)
            : assets.addCaptured(capturedPiece);

    return state.withBoard(board).withAssets(state.currentPlayer().getColor(), updatedAssets);
  }

  /**
   * Applies multiple capture actions sequentially.
   *
   * <p>Each capture is applied on top of the previously updated game state.
   *
   * @param state initial game state
   * @param actions capture actions to apply
   * @return updated game state after all captures
   */
  public GameState applyCaptures(GameState state, List<CaptureAction> actions) {
    GameState result = state;
    for (CaptureAction action : actions) {
      result = applyCapture(result, action);
    }
    return result;
  }
}
