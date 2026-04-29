package eu.nicosworld.rithmo.core.turn;

import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.turn.action.PreCaptureAction;
import eu.nicosworld.rithmo.core.turn.action.SkipPreCaptureAction;
import eu.nicosworld.rithmo.core.turn.option.PreCaptureOption;
import eu.nicosworld.rithmo.core.turn.resolver.*;
import eu.nicosworld.rithmo.core.turn.testutils.TurnAssertion;
import eu.nicosworld.rithmo.engine.capture.CaptureType;
import eu.nicosworld.rithmo.engine.capture.capturerule.AssaultRule;
import eu.nicosworld.rithmo.engine.capture.capturerule.EncounterRule;
import eu.nicosworld.rithmo.engine.model.*;
import eu.nicosworld.rithmo.engine.move.*;
import eu.nicosworld.rithmo.engine.setup.BoardBuilder;
import eu.nicosworld.rithmo.engine.testutils.RithmoDebug;
import eu.nicosworld.rithmo.engine.victory.BodyVictoryRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static eu.nicosworld.rithmo.core.turn.testutils.TurnHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TurnProcessorIntegrationBeforeMoveTest {

    private TurnProcessor processor;
    private BoardBuilder builder;

    @BeforeEach
    void setup() {
        RegularMoveGenerator regularMoveGenerator = new RegularMoveGenerator();
        FreePathMovementValidator freePathMovementValidator = new FreePathMovementValidator();

        EncounterRule encounterRule = new EncounterRule(regularMoveGenerator, freePathMovementValidator);

        processor = setupProcessor(List.of(encounterRule));

        // =========================
        // SIMPLE BOARD SETUP
        // =========================
        builder = new BoardBuilder(4, 4);
    }

    @Test
    void shouldPassFromStartToPreCaptureApplication() throws VictoryException, PatException {
        Board board = builder
                .blackCircle(5).at(1,1)
                .whiteCircle(5).at(2, 2)
                .build();

        GameState state = GameState.initial(board, Player.BLACK);

        TurnState startTurn = TurnState.of(
                state,
                TurnPhase.START
        );

        RithmoDebug.printBoardAfterAct(state.board());

        TurnState turn1 = processor.process(startTurn, null);

        assertThat(turn1).isNotSameAs(startTurn);

        TurnAssertion.assertThis(turn1)
                .isInPreCaptureApplicationPhase()
                .hasCurrentBlackPlayer()
                .hasOptionsCount(2)
                .hasSkipPreCaptureOption()
                .hasPreCaptureOptionsCount(1)
                .hasCaptureLandingOption(new Position(2, 2))
                .hasPreCaptureLandingOption(new Position(2, 2));

    }

    @Test
    void shouldPassFromStartToMove_4MovesPossibles() throws VictoryException, PatException {
        Board board = builder
                .blackTriangle(5).at(0,0)
                .whiteCircle(5).at(2, 2)
                .build();

        GameState state = GameState.initial(board, Player.BLACK);

        TurnState startTurn = TurnState.of(
                state,
                TurnPhase.START
        );

        RithmoDebug.printBoardAfterAct(state.board());

        TurnState turn1 = processor.process(startTurn, null);

        assertThat(turn1).isNotSameAs(startTurn);

        TurnAssertion.assertThis(turn1)
                .isInMoveApplicationPhase()
                .hasCurrentBlackPlayer()
                .hasMoveOption()
                .hasRegularMoveOption(2)
                .hasIrregularMoveOption(2)
                .hasRegularMoveTo(0,2)
                .hasRegularMoveTo(2,0)
                .hasIrregularMoveTo(1,2)
                .hasIrregularMoveTo(2,1);
    }

    @Test
    void shouldPassFromStartToMove_Only1RegularMovesPossibles() throws VictoryException, PatException {
        Board board = builder
                .blackCircle(5).at(0,0)
                .whiteCircle(5).at(2, 2)
                .build();

        GameState state = GameState.initial(board, Player.BLACK);

        TurnState startTurn = TurnState.of(
                state,
                TurnPhase.START
        );

        RithmoDebug.printBoardAfterAct(state.board());

        TurnState turn1 = processor.process(startTurn, null);

        assertThat(turn1).isNotSameAs(startTurn);

        TurnAssertion.assertThis(turn1)
                .isInMoveApplicationPhase()
                .hasCurrentBlackPlayer()
                .hasMoveOption()
                .hasRegularMoveOption(1)
                .hasNoIrregularMoveOption()
                .hasRegularMoveTo(1,1);
    }

    @Test
    void shouldPassFromStartToPreCaptureApplication_BecausePlayerSkipPreCapture() throws VictoryException, PatException {
        Board board = builder
                .blackCircle(5).at(1,1)
                .whiteCircle(5).at(2, 2)
                .build();

        GameState state = GameState.initial(board, Player.BLACK);

        TurnState startTurn = TurnState.of(
                state,
                TurnPhase.START
        );

        RithmoDebug.printBoardAfterAct(state.board());

        TurnState turn1 = processor.process(startTurn, null);

        TurnState turn2 = processor.process(turn1, new SkipPreCaptureAction());

        assertThat(turn1).isNotSameAs(startTurn);
        assertThat(turn2).isNotSameAs(turn1);

        TurnAssertion.assertThis(turn2)
                .isInMoveApplicationPhase()
                .hasCurrentBlackPlayer()
                .hasOptionsCount(3)
                .hasMoveOption()
                .hasRegularMoveOption(3)
                .hasNoIrregularMoveOption()
                .hasRegularMoveTo(0,0)
                .hasRegularMoveTo(0,2)
                .hasRegularMoveTo(2,0);

    }

    @Test
    void shouldThrowPatException_BecauseNoPossibleMove() throws VictoryException {
        Board board = builder
                .blackCircle(5).at(0,0)
                .whiteCircle(5).at(1, 1)
                .build();

        GameState state = GameState.initial(board, Player.BLACK);

        TurnState startTurn = TurnState.of(
                state,
                TurnPhase.MOVE_COMPUTATION
        );

        RithmoDebug.printBoardAfterAct(state.board());

        assertThatThrownBy(() -> processor.process(startTurn, null))
                .isInstanceOf(PatException.class)
                .hasMessage("BLACK is pat");
    }

    @Test
    void shouldThrowPatException_BecausePlayerSkipCapture() throws VictoryException, PatException {
        Board board = builder
                .blackCircle(5).at(0,0)
                .whiteCircle(5).at(1, 1)
                .build();

        GameState state = GameState.initial(board, Player.BLACK);

        TurnState startTurn = TurnState.of(
                state,
                TurnPhase.START
        );

        RithmoDebug.printBoardAfterAct(state.board());

        TurnState turn1 = processor.process(startTurn, null);

        assertThatThrownBy(() -> processor.process(turn1, new SkipPreCaptureAction()))
                .isInstanceOf(PatException.class)
                .hasMessage("BLACK is pat");
    }

    @Test
    void shouldThrowVictoryException_BecausePlayerChooseCapture() throws VictoryException, PatException {
        Board board = builder
                .blackCircle(5).at(0,0)
                .whiteCircle(5).at(1, 1)
                .build();

        GameState state = GameState.initial(board, Player.BLACK);

        TurnState startTurn = TurnState.of(
                state,
                TurnPhase.START
        );

        RithmoDebug.printBoardAfterAct(state.board());

        TurnState turn1 = processor.process(startTurn, null);

        showOptions(turn1);

        Position attackerPos = new Position(0,0);
        Position targetPos = new Position(1,1);

        PreCaptureOption choice = findPreCaptureOption(turn1.options(), attackerPos, targetPos, targetPos);
        PreCaptureAction chosenAction = PreCaptureAction.from(choice);

        assertThatThrownBy(() -> processor.process(turn1, chosenAction))
                .isInstanceOf(VictoryException.class)
                .hasMessage("BLACK is winner");

    }

    @Nested
    class WithAssaultRuleAnd2CapturesVictory {

        @BeforeEach
        void setup() {
            RegularMoveGenerator regularMoveGenerator = new RegularMoveGenerator();
            FreePathMovementValidator freePathMovementValidator = new FreePathMovementValidator();

            AssaultRule assaultRule = new AssaultRule(regularMoveGenerator, freePathMovementValidator);
            EncounterRule encounterRule = new EncounterRule(regularMoveGenerator, freePathMovementValidator);

            BodyVictoryRule bodyVictoryRule = new BodyVictoryRule(2);

            processor = setupProcessor(List.of(encounterRule, assaultRule), List.of(bodyVictoryRule));

            // =========================
            // SIMPLE BOARD SETUP
            // =========================
            builder = new BoardBuilder(4, 4);
        }

        @Test
        void shouldPassToMovePhaseAfterPreCaptureAction() throws VictoryException, PatException {
            Board board = builder
                    .blackCircle(5).at(0,0)
                    .whiteCircle(5).at(1, 1)
                    .whiteTriangle(6).at(3,3)
                    .build();

            GameState state = GameState.initial(board, Player.BLACK);

            TurnState startTurn = TurnState.of(
                    state,
                    TurnPhase.START
            );

            RithmoDebug.printBoardAfterAct(state.board());

            TurnState turn1 = processor.process(startTurn, null);

            showOptions(turn1);

            Position attackerPos = new Position(0,0);
            Position targetPos = new Position(1,1);

            PreCaptureOption choice = findPreCaptureOption(turn1.options(), attackerPos, targetPos, targetPos);
            PreCaptureAction chosenAction = PreCaptureAction.from(choice);

            TurnState turn2 = processor.process(turn1, chosenAction);

            assertThat(turn1).isNotSameAs(startTurn);
            assertThat(turn2).isNotSameAs(turn1);

            RithmoDebug.printBoardAfterAct(turn2.state().board());

            TurnAssertion.assertThis(turn2)
                    .isInMoveApplicationPhase()
                    .hasCurrentBlackPlayer()
                    .hasMoveOption()
                    .hasRegularMoveOption(4)
                    .hasNoIrregularMoveOption()
                    .hasRegularMoveTo(0,2)
                    .hasRegularMoveTo(2,0)
                    .hasRegularMoveTo(2,2)
                    .hasRegularMoveTo(0,0);
        }

        @Test
        void shouldThrowPatException_BecausePlayerCaptures() throws VictoryException, PatException {
            Board board = builder
                    .blackTriangle(4).at(0,0)
                    .whiteCircle(8).at(3, 3)
                    .whiteSquare(2).at(3,2)
                    .whiteTriangle(25).at(1,3)
                    .build();

            GameState state = GameState.initial(board, Player.BLACK);

            TurnState startTurn = TurnState.of(
                    state,
                    TurnPhase.START
            );

            RithmoDebug.printBoardAfterAct(state.board());

            TurnState turn1 = processor.process(startTurn, null);

            showOptions(turn1);

            TurnAssertion.assertThis(turn1)
                    .hasSkipPreCaptureOption()
                    .hasOptionsCount(2)
                    .hasPreCaptureOptionsCount(1)
                    .hasPreCaptureOptions(CaptureType.ASSAULT);

            Position attackerPos = new Position(0,0);
            Position targetPos = new Position(3,3);

            PreCaptureOption choice = findPreCaptureOption(turn1.options(), attackerPos, targetPos, targetPos);
            PreCaptureAction action = PreCaptureAction.from(choice);

            assertThatThrownBy(() -> processor.process(turn1, action))
                    .isInstanceOf(PatException.class)
                    .hasMessage("BLACK is pat");
        }

    }

    @Nested
    class WithAssaultRuleAnd1CapturesVictory {

        @BeforeEach
        void setup() {
            RegularMoveGenerator regularMoveGenerator = new RegularMoveGenerator();
            FreePathMovementValidator freePathMovementValidator = new FreePathMovementValidator();

            AssaultRule assaultRule = new AssaultRule(regularMoveGenerator, freePathMovementValidator);
            EncounterRule encounterRule = new EncounterRule(regularMoveGenerator, freePathMovementValidator);

            BodyVictoryRule bodyVictoryRule = new BodyVictoryRule(1);

            processor = setupProcessor(List.of(encounterRule, assaultRule), List.of(bodyVictoryRule));

            // =========================
            // SIMPLE BOARD SETUP
            // =========================
            builder = new BoardBuilder(4, 4);
        }

        @Test
        void shouldThrowVictoryException_BecausePlayerCaptures() throws VictoryException, PatException {
            Board board = builder
                    .blackTriangle(4).at(0,0)
                    .whiteCircle(8).at(3, 3)
                    .whiteSquare(2).at(3,2)
                    .whiteTriangle(25).at(1,3)
                    .build();

            GameState state = GameState.initial(board, Player.BLACK);

            TurnState startTurn = TurnState.of(
                    state,
                    TurnPhase.START
            );

            RithmoDebug.printBoardAfterAct(state.board());

            TurnState turn1 = processor.process(startTurn, null);

            TurnAssertion.assertThis(turn1)
                    .hasSkipPreCaptureOption()
                    .hasOptionsCount(2)
                    .hasPreCaptureOptionsCount(1)
                    .hasPreCaptureOptions(CaptureType.ASSAULT);

            Position attackerPos = new Position(0,0);
            Position targetPos = new Position(3,3);

            PreCaptureOption choice = findPreCaptureOption(turn1.options(), attackerPos, targetPos, targetPos);
            PreCaptureAction chosenAction = PreCaptureAction.from(choice);


            assertThatThrownBy(() -> processor.process(turn1, chosenAction))
                    .isInstanceOf(VictoryException.class)
                    .hasMessage("BLACK is winner");
        }
    }

    @Nested
    class WithAssaultRuleAnd3CapturesVictory {

        @BeforeEach
        void setup() {
            RegularMoveGenerator regularMoveGenerator = new RegularMoveGenerator();
            FreePathMovementValidator freePathMovementValidator = new FreePathMovementValidator();

            AssaultRule assaultRule = new AssaultRule(regularMoveGenerator, freePathMovementValidator);
            EncounterRule encounterRule = new EncounterRule(regularMoveGenerator, freePathMovementValidator);

            BodyVictoryRule bodyVictoryRule = new BodyVictoryRule(3);

            processor = setupProcessor(List.of(encounterRule, assaultRule), List.of(bodyVictoryRule));

            // =========================
            // SIMPLE BOARD SETUP
            // =========================
            builder = new BoardBuilder(4, 4);
        }

        @Test
        void shouldPassToMovePhaseAfterPreCaptureAction_With2Captures() throws VictoryException, PatException {
            Board board = builder
                    .blackCircle(5).at(1,1)
                    .whiteCircle(5).at(2, 2)
                    .whiteSquare(5).at(0,2)
                    .whiteTriangle(6).at(3,3)
                    .build();

            GameState state = GameState.initial(board, Player.BLACK);

            TurnState startTurn = TurnState.of(
                    state,
                    TurnPhase.START
            );

            RithmoDebug.printBoardAfterAct(state.board());

            TurnState turn1 = processor.process(startTurn, null);

            showOptions(turn1);

            Position attackerPos = new Position(1,1);
            Position targetPos1 = new Position(2,2);
            Position targetPos2 = new Position(0,2);

            PreCaptureOption choice = findPreCaptureOption(turn1.options(), attackerPos, targetPos2, targetPos1, targetPos2);
            PreCaptureAction chosenAction = PreCaptureAction.from(choice);

            TurnState turn2 = processor.process(turn1, chosenAction);

            assertThat(turn1).isNotSameAs(startTurn);
            assertThat(turn2).isNotSameAs(turn1);

            RithmoDebug.printBoardAfterAct(turn2.state().board());

            TurnAssertion.assertThis(turn2)
                    .isInMoveApplicationPhase()
                    .hasCurrentBlackPlayer()
                    .hasMoveOption()
                    .hasRegularMoveOption(2)
                    .hasNoIrregularMoveOption()
                    .hasRegularMoveTo(1,1)
                    .hasRegularMoveTo(1,3)
                    .checkState()
                        .player(Player.BLACK)
                        .hasInReserve(turn1.state().board().getPieceAt(targetPos1))
                        .hasInReserve(turn1.state().board().getPieceAt(targetPos2));
        }
    }
}