package eu.nicosworld.rithmo.core.turn.applier;

import eu.nicosworld.rithmo.core.turn.BaseTest;
import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.CaptureType;
import eu.nicosworld.rithmo.engine.model.*;
import eu.nicosworld.rithmo.engine.setup.BoardBuilder;
import eu.nicosworld.rithmo.engine.testutils.GameStateAssertion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CaptureApplierTest extends BaseTest {

    @Test
    void applyCapture_OneCapture() {
        // Arrange
        Position attackerPos = new Position(1, 1);
        Position targetPos = new Position(2,2);

        BoardBuilder builder = new BoardBuilder(4,4);

        Board board = builder.blackCircle(4)
                .at(1,1)
                .whiteCircle(4).at(2,2)
                .build();

        GameState state = GameState.initial(board, Player.BLACK);

        Piece attacker = board.getPieceAt(attackerPos);
        Piece captured = board.getPieceAt(targetPos);

        CaptureAction captureAction = new CaptureAction(attacker,
                attackerPos,
                captured,
                targetPos,
                captured,
                true,
                CaptureType.ENCOUNTER
                );
        printBoardAfterArrange(board);
        // Act
        CaptureApplier applier = new CaptureApplier();
        GameState newState = applier.applyCapture(state, captureAction);

        Board newBoard = newState.board();
        printBoardAfterAct(newBoard);

        GameStateAssertion.assertThis(newState)
                .isEmpty(targetPos)
                .player(Player.BLACK)
                .hasInReserve(captured)
                .hasOnBoard(attacker)
                .at(attackerPos);
    }

    @Test
    void applyCapture_TwoCaptures() {
        // Arrange
        Position attackerPos = new Position(1, 1);
        Position targetPos = new Position(2,2);
        Position targetPos2 = new Position(0,0);

        BoardBuilder builder = new BoardBuilder(4,4);

        Board board = builder.blackCircle(4)
                .at(1,1)
                .whiteCircle(4).at(2,2)
                .whiteSquare(4).at(0,0)
                .build();

        GameState state = GameState.initial(board, Player.BLACK);

        Piece attacker = board.getPieceAt(attackerPos);
        Piece captured = board.getPieceAt(targetPos);
        Piece captured2 = board.getPieceAt(targetPos2);

        CaptureAction captureAction = new CaptureAction(attacker,
                attackerPos,
                captured,
                targetPos,
                captured,
                true,
                CaptureType.ENCOUNTER
        );
        CaptureAction captureAction2 = new CaptureAction(attacker,
                attackerPos,
                captured2,
                targetPos2,
                captured2,
                true,
                CaptureType.ENCOUNTER
        );

        List<CaptureAction> captureActions = List.of(captureAction, captureAction2);
        printBoardAfterArrange(board);
        // Act
        CaptureApplier applier = new CaptureApplier();
        GameState newState = applier.applyCaptures(state, captureActions);
        Board newBoard = newState.board();

        printBoardAfterAct(newBoard);

        GameStateAssertion.assertThis(newState)
                .isEmpty(targetPos)
                .isEmpty(targetPos2)
                .player(Player.BLACK)
                .hasInReserve(captured)
                .hasInReserve(captured2)
                .hasOnBoard(attacker)
                .at(attackerPos);
    }
}