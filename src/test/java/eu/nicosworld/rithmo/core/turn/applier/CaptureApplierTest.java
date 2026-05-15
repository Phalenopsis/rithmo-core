package eu.nicosworld.rithmo.core.turn.applier;

import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.model.InvolvedPiece;
import eu.nicosworld.rithmo.engine.model.*;
import eu.nicosworld.rithmo.engine.setup.BoardBuilder;
import eu.nicosworld.rithmo.engine.testutils.GameStateAssertion;
import eu.nicosworld.rithmo.engine.testutils.RithmoDebug;
import org.junit.jupiter.api.Test;

import java.util.List;

class CaptureApplierTest {

    @Test
    void applyCapture_OneCapture() {
        // Arrange
        Position attackerPos = new Position(1, 1);
        Position targetPos = new Position(2, 2);

        BoardBuilder builder = new BoardBuilder(4, 4);
        Board board = builder.blackCircle(4).at(1, 1)
                .whiteCircle(4).at(2, 2)
                .build();

        GameState state = GameState.initial(board, Player.BLACK);

        Piece attackerPiece = board.getPieceAt(attackerPos);
        Piece targetPiece = board.getPieceAt(targetPos);

        // Utilisation des factories InvolvedPiece et CaptureAction
        InvolvedPiece actor = InvolvedPiece.whole(attackerPiece, attackerPos);
        InvolvedPiece target = InvolvedPiece.whole(targetPiece, targetPos);

        CaptureAction captureAction = CaptureAction.encounter(actor, target);

        RithmoDebug.printBoardAfterArrange(board);

        // Act
        CaptureApplier applier = new CaptureApplier();
        GameState newState = applier.applyCapture(state, captureAction);

        Board newBoard = newState.board();
        RithmoDebug.printBoardAfterAct(newBoard);

        // Assert
        GameStateAssertion.assertThis(newState)
                .isEmpty(targetPos)
                .player(Player.BLACK)
                .hasCapturedEquivalentInReserve(targetPiece)
                .hasNotInReserve(targetPiece)
                .hasOnBoard(attackerPiece)
                .at(attackerPos);
    }

    @Test
    void applyCapture_TwoCaptures() {
        // Arrange
        Position attackerPos = new Position(1, 1);
        Position targetPos1 = new Position(2, 2);
        Position targetPos2 = new Position(0, 0);

        BoardBuilder builder = new BoardBuilder(4, 4);
        Board board = builder.blackCircle(4).at(1, 1)
                .whiteCircle(4).at(2, 2)
                .whiteSquare(16).at(0, 0)
                .build();

        GameState state = GameState.initial(board, Player.BLACK);

        Piece attackerPiece = board.getPieceAt(attackerPos);
        Piece targetPiece1 = board.getPieceAt(targetPos1);
        Piece targetPiece2 = board.getPieceAt(targetPos2);

        // Préparation des actions via les factories
        InvolvedPiece actor = InvolvedPiece.whole(attackerPiece, attackerPos);

        CaptureAction action1 = CaptureAction.encounter(actor, InvolvedPiece.whole(targetPiece1, targetPos1));
        CaptureAction action2 = CaptureAction.encounter(actor, InvolvedPiece.whole(targetPiece2, targetPos2));

        List<CaptureAction> captureActions = List.of(action1, action2);

        RithmoDebug.printBoardAfterArrange(board);

        // Act
        CaptureApplier applier = new CaptureApplier();
        GameState newState = applier.applyCaptures(state, captureActions);

        RithmoDebug.printBoardAfterAct(newState.board());

        // Assert
        GameStateAssertion.assertThis(newState)
                .isEmpty(targetPos1)
                .isEmpty(targetPos2)
                .player(Player.BLACK)
                .hasCapturedEquivalentInReserve(targetPiece1)
                .hasNotInReserve(targetPiece1)
                .hasCapturedEquivalentInReserve(targetPiece2)
                .hasNotInReserve(targetPiece2)
                .hasOnBoard(attackerPiece)
                .at(attackerPos);
    }
}