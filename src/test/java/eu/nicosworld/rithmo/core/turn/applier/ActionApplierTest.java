package eu.nicosworld.rithmo.core.turn.applier;

import eu.nicosworld.rithmo.core.turn.action.*;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.model.InvolvedPiece;
import eu.nicosworld.rithmo.engine.model.*;
import eu.nicosworld.rithmo.engine.move.Move;
import eu.nicosworld.rithmo.engine.move.MoveNature;
import eu.nicosworld.rithmo.engine.setup.BoardBuilder;
import eu.nicosworld.rithmo.engine.testutils.GameStateAssertion;
import eu.nicosworld.rithmo.engine.testutils.RithmoDebug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static eu.nicosworld.rithmo.engine.testutils.RithmoDebug.printBoardAfterAct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ActionApplierTest {

    Board board;
    CaptureAction blackCircleCaptureWhiteAt22;
    CaptureAction blackCircleCaptureWhiteAt00;
    Piece blackCircle;
    Position blackCirclePosition;
    Piece whiteCircle;
    Position whiteCirclePosition;
    Piece whiteTriangle;
    Position whiteTrianglePosition;

    ActionApplier actionApplier;

    void setupApplier() {
        CaptureApplier captureApplier = new CaptureApplier();
        MoveApplier moveApplier = new MoveApplier();

        actionApplier = new ActionApplier(captureApplier, moveApplier);
    }

    @BeforeEach
    void setup() {
        setupApplier();

        blackCirclePosition = new Position(1, 1);
        whiteCirclePosition = new Position(2, 2);
        whiteTrianglePosition = new Position(0, 0);

        board = new BoardBuilder(4,4)
                .blackCircle(4).at(1,1)
                .whiteCircle(4).at(2,2)
                .whiteTriangle(4).at(0,0)
                .build();
        blackCircle = board.getPieceAt(blackCirclePosition);
        whiteCircle = board.getPieceAt(whiteCirclePosition);
        whiteTriangle = board.getPieceAt(whiteTrianglePosition);

        InvolvedPiece actor = InvolvedPiece.whole(blackCircle, blackCirclePosition);
        InvolvedPiece target22 = InvolvedPiece.whole(whiteCircle, whiteCirclePosition);
        InvolvedPiece target00 = InvolvedPiece.whole(whiteTriangle, whiteTrianglePosition);

        blackCircleCaptureWhiteAt22 = CaptureAction.encounter(actor, target22);
        blackCircleCaptureWhiteAt00 = CaptureAction.encounter(actor, target00);
    }

    @Test
    void apply_applyPreCaptureAction_with2CapturesAndLanding() {
        GameState state = GameState.initial(board, Player.BLACK);

        PreCaptureAction action = new PreCaptureAction(
                List.of(blackCircleCaptureWhiteAt22, blackCircleCaptureWhiteAt00),
                whiteTrianglePosition
        );

        AppliedResult result = actionApplier.apply(state, action);
        GameState newGameState = result.gameState();

        // Immutability test
        assertThat(newGameState).isNotSameAs(state);
        assertThat(state.board().getPieceAt(blackCirclePosition)).isNotNull();

        // functional test
        assertThat(result.landingPosition())
                .isEqualTo(whiteTrianglePosition);
        assertFalse(result.wasMoveIrregular());

        GameStateAssertion.assertThis(newGameState)
                .player(Player.BLACK)
                .hasInReserve(whiteCircle)
                .hasInReserve(whiteTriangle)
                .hasOnBoard(blackCircle)
                .at(whiteTrianglePosition)
                .isNotAt(blackCirclePosition)
                .isNotAt(whiteCirclePosition);
    }

    @Test
    void apply_applyPreCaptureAction_with1CaptureAndLanding() {
        GameState state = GameState.initial(board, Player.BLACK);

        PreCaptureAction action = new PreCaptureAction(
                List.of(blackCircleCaptureWhiteAt22),
                whiteCirclePosition
        );

        AppliedResult result = actionApplier.apply(state, action);
        GameState newGameState = result.gameState();

        assertThat(result.landingPosition())
                .isEqualTo(whiteCirclePosition);
        assertFalse(result.wasMoveIrregular());

        GameStateAssertion.assertThis(newGameState)
                .player(Player.BLACK)
                .hasInReserve(whiteCircle)
                .hasNotInReserve(whiteTriangle)
                .hasOnBoard(blackCircle)
                .at(whiteCirclePosition)
                .isNotAt(blackCirclePosition);
    }

    @Test
    void apply_applySkipPreCaptureAction_with2CapturesAndLanding() {
        GameState state = GameState.initial(board, Player.BLACK);

        SkipPreCaptureAction action = new SkipPreCaptureAction();

        AppliedResult result = actionApplier.apply(state, action);
        GameState newGameState = result.gameState();

        assertThat(result.landingPosition())
                .isNull();
        assertFalse(result.wasMoveIrregular());

        GameStateAssertion.assertThis(newGameState)
                .player(Player.BLACK)
                .hasNotInReserve(whiteCircle)
                .hasNotInReserve(whiteTriangle)
                .hasOnBoard(blackCircle)
                .at(blackCirclePosition)
                .player(Player.WHITE)
                .hasOnBoard(whiteTriangle)
                .at(whiteTrianglePosition)
                .hasOnBoard(whiteCircle)
                .at(whiteCirclePosition);
    }

    @Test
    void apply_applyMoveAction_Regular() {
        Position blackCirclePositionAfterMove = new Position(0, 1);

        GameState state = GameState.initial(board, Player.BLACK);

        MoveAction action = new MoveAction(
                new Move(blackCirclePosition,
                        blackCirclePositionAfterMove,
                        MoveNature.REGULAR)
        );

        AppliedResult result = actionApplier.apply(state, action);
        GameState newGameState = result.gameState();

        assertThat(result.landingPosition())
                .isEqualTo(blackCirclePositionAfterMove);
        assertFalse(result.wasMoveIrregular());

        GameStateAssertion.assertThis(newGameState)
                .player(Player.BLACK)
                .hasNotInReserve(whiteCircle)
                .hasNotInReserve(whiteTriangle)
                .hasOnBoard(blackCircle)
                .at(blackCirclePositionAfterMove)
                .isNotAt(blackCirclePosition);
    }

    @Test
    void apply_applyMoveAction_Irregular() {
        Position whiteTrianglePositionAfterMove = new Position(1, 2);

        GameState state = GameState.initial(board, Player.WHITE);

        MoveAction action = new MoveAction(
                new Move(whiteTrianglePosition,
                        whiteTrianglePositionAfterMove,
                        MoveNature.IRREGULAR)
        );
        RithmoDebug.printBoardAfterArrange(state.board());
        AppliedResult result = actionApplier.apply(state, action);
        GameState newGameState = result.gameState();
        printBoardAfterAct(newGameState.board());
        assertThat(result.landingPosition())
                .isEqualTo(whiteTrianglePositionAfterMove);
        assertTrue(result.wasMoveIrregular());

        GameStateAssertion.assertThis(newGameState)
                .player(Player.BLACK)
                .hasNotInReserve(whiteCircle)
                .hasNotInReserve(whiteTriangle)
                .hasOnBoard(blackCircle)
                .at(blackCirclePosition)
                .player(Player.WHITE)
                .hasOnBoard(whiteTriangle)
                .isNotAt(whiteTrianglePosition)
                .at(whiteTrianglePositionAfterMove);
    }

    @Test
    void apply_applyPostCaptureAction_2Captures() {
        GameState state = GameState.initial(board, Player.BLACK);

        PostCaptureAction action = new PostCaptureAction(
                List.of(
                        blackCircleCaptureWhiteAt22,
                        blackCircleCaptureWhiteAt00
                )
        );
        RithmoDebug.printBoardAfterArrange(state.board());
        AppliedResult result = actionApplier.apply(state, action);
        GameState newGameState = result.gameState();
        printBoardAfterAct(newGameState.board());

        // Immutability test
        assertThat(newGameState).isNotSameAs(state);
        assertThat(state.board().getPieceAt(blackCirclePosition)).isNotNull();

        // functional test
        assertThat(result.landingPosition())
                .isNull();
        assertFalse(result.wasMoveIrregular());

        GameStateAssertion.assertThis(newGameState)
                .player(Player.BLACK)
                .hasInReserve(whiteCircle)
                .hasInReserve(whiteTriangle)
                .hasOnBoard(blackCircle)
                .at(blackCirclePosition)
                .isEmpty(whiteTrianglePosition)
                .isEmpty(whiteCirclePosition);
    }

    @Test
    void apply_applyPostCaptureAction_1Captures() {
        GameState state = GameState.initial(board, Player.BLACK);

        PostCaptureAction action = new PostCaptureAction(
                List.of(
                        blackCircleCaptureWhiteAt00
                )
        );
        RithmoDebug.printBoardAfterArrange(state.board());
        AppliedResult result = actionApplier.apply(state, action);
        GameState newGameState = result.gameState();
        printBoardAfterAct(newGameState.board());

        // Immutability test
        assertThat(newGameState).isNotSameAs(state);
        assertThat(state.board().getPieceAt(blackCirclePosition)).isNotNull();

        // functional test
        assertThat(result.landingPosition())
                .isNull();
        assertFalse(result.wasMoveIrregular());

        GameStateAssertion.assertThis(newGameState)
                .player(Player.BLACK)
                .hasInReserve(whiteTriangle)
                .hasOnBoard(blackCircle)
                .at(blackCirclePosition)
                .isEmpty(whiteTrianglePosition)
                .player(Player.WHITE)
                .hasOnBoard(whiteCircle)
                .at(whiteCirclePosition);
    }

    @Test
    void apply_applySkipPostCaptureAction() {
        GameState state = GameState.initial(board, Player.BLACK);

        SkipPostCaptureAction action = new SkipPostCaptureAction();


        RithmoDebug.printBoardAfterArrange(state.board());
        AppliedResult result = actionApplier.apply(state, action);
        GameState newGameState = result.gameState();
        printBoardAfterAct(newGameState.board());

        // Immutability test
        assertThat(newGameState).isSameAs(state);
        assertThat(state.board().getPieceAt(blackCirclePosition)).isNotNull();

        // functional test
        assertThat(result.landingPosition())
                .isNull();
        assertFalse(result.wasMoveIrregular());

        GameStateAssertion.assertThis(newGameState)
                .player(Player.BLACK)
                .hasEmptyReserve()
                .hasOnBoard(blackCircle)
                .at(blackCirclePosition)
                .player(Player.WHITE)
                .hasOnBoard(whiteCircle)
                .at(whiteCirclePosition)
                .hasOnBoard(whiteTriangle)
                .at(whiteTrianglePosition);
    }
}