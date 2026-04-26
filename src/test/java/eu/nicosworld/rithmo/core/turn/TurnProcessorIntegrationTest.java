package eu.nicosworld.rithmo.core.turn;

import eu.nicosworld.rithmo.core.Exception.PatException;
import eu.nicosworld.rithmo.core.Exception.VictoryException;
import eu.nicosworld.rithmo.core.turn.action.PreCaptureAction;
import eu.nicosworld.rithmo.core.turn.action.SkipPreCaptureAction;
import eu.nicosworld.rithmo.core.turn.applier.ActionApplier;
import eu.nicosworld.rithmo.core.turn.applier.CaptureApplier;
import eu.nicosworld.rithmo.core.turn.applier.MoveApplier;
import eu.nicosworld.rithmo.core.turn.option.PreCaptureOption;
import eu.nicosworld.rithmo.core.turn.option.TurnOption;
import eu.nicosworld.rithmo.core.turn.resolver.*;
import eu.nicosworld.rithmo.core.turn.testutils.TurnAssertion;
import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.CaptureEngine;
import eu.nicosworld.rithmo.engine.capture.CaptureRule;
import eu.nicosworld.rithmo.engine.capture.CaptureType;
import eu.nicosworld.rithmo.engine.capture.capturerule.AssaultRule;
import eu.nicosworld.rithmo.engine.capture.capturerule.EncounterRule;
import eu.nicosworld.rithmo.engine.model.*;
import eu.nicosworld.rithmo.engine.move.*;
import eu.nicosworld.rithmo.engine.setup.BoardBuilder;
import eu.nicosworld.rithmo.engine.testutils.RithmoDebug;
import eu.nicosworld.rithmo.engine.victory.BodyVictoryRule;
import eu.nicosworld.rithmo.engine.victory.VictoryEngine;
import eu.nicosworld.rithmo.engine.victory.VictoryRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TurnProcessorIntegrationTest {

    private TurnProcessor processor;

    private GameState state;
    private BoardBuilder builder;

    @BeforeEach
    void setup() {
        RegularMoveGenerator regularMoveGenerator = new RegularMoveGenerator();
        FreePathMovementValidator freePathMovementValidator = new FreePathMovementValidator();

        EncounterRule encounterRule = new EncounterRule(regularMoveGenerator, freePathMovementValidator);

        setupProcessor(List.of(encounterRule));

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

        state = GameState.initial(board, Player.BLACK);

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

        state = GameState.initial(board, Player.BLACK);

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

        state = GameState.initial(board, Player.BLACK);

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

        state = GameState.initial(board, Player.BLACK);

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

        state = GameState.initial(board, Player.BLACK);

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

        state = GameState.initial(board, Player.BLACK);

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

        state = GameState.initial(board, Player.BLACK);

        TurnState startTurn = TurnState.of(
                state,
                TurnPhase.START
        );

        RithmoDebug.printBoardAfterAct(state.board());

        TurnState turn1 = processor.process(startTurn, null);

        showOptions(turn1);

        Position attackerPos = new Position(0,0);
        Position targetPos = new Position(1,1);

        PreCaptureChoice choice = findPreCaptureChoice(turn1.options(), attackerPos, targetPos);
        PreCaptureAction chosenAction = PreCaptureAction.from(choice, targetPos);

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

            setupProcessor(List.of(encounterRule, assaultRule), List.of(bodyVictoryRule));

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

            state = GameState.initial(board, Player.BLACK);

            TurnState startTurn = TurnState.of(
                    state,
                    TurnPhase.START
            );

            RithmoDebug.printBoardAfterAct(state.board());

            TurnState turn1 = processor.process(startTurn, null);

            showOptions(turn1);

            Position attackerPos = new Position(0,0);
            Position targetPos = new Position(1,1);

            PreCaptureChoice choice = findPreCaptureChoice(turn1.options(), attackerPos, targetPos);
            PreCaptureAction chosenAction = PreCaptureAction.from(choice, targetPos);

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

            state = GameState.initial(board, Player.BLACK);

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
                    .hasCaptureOptions(CaptureType.ASSAULT);

            Position attackerPos = new Position(0,0);
            Position targetPos = new Position(3,3);

            PreCaptureChoice choice = findPreCaptureChoice(turn1.options(), attackerPos, targetPos);
            PreCaptureAction action = PreCaptureAction.from(choice, targetPos);

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

            setupProcessor(List.of(encounterRule, assaultRule), List.of(bodyVictoryRule));

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

            state = GameState.initial(board, Player.BLACK);

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
                    .hasCaptureOptions(CaptureType.ASSAULT);

            Position attackerPos = new Position(0,0);
            Position targetPos = new Position(3,3);

            PreCaptureChoice choice = findPreCaptureChoice(turn1.options(), attackerPos, targetPos);
            PreCaptureAction chosenAction = PreCaptureAction.from(choice, targetPos);


            assertThatThrownBy(() -> processor.process(turn1, chosenAction))
                    .isInstanceOf(VictoryException.class)
                    .hasMessage("BLACK is winner");
        }


    }

    private void setupProcessor(List<CaptureRule> captureRules) {
        BodyVictoryRule bodyVictoryRule = new BodyVictoryRule(1);

        setupProcessor(captureRules, List.of(bodyVictoryRule));
    }

    private void setupProcessor(List<CaptureRule> captureRules, List<VictoryRule> victoryRules) {
        CaptureApplier captureApplier = new CaptureApplier();
        MoveApplier moveApplier = new MoveApplier();
        ActionApplier actionApplier = new ActionApplier(captureApplier, moveApplier);

        CaptureEngine captureEngine = new CaptureEngine(captureRules);
        CaptureResolver captureResolver = new CaptureResolver(captureEngine);

        MovementEngine movementEngine = new MovementEngine();
        MoveResolver moveResolver = new MoveResolver(movementEngine);

        PhaseResolver phaseResolver = new PhaseResolver(captureResolver, moveResolver);

        VictoryEngine victoryEngine = new VictoryEngine(victoryRules);

        processor = new TurnProcessor(actionApplier,
                phaseResolver,
                victoryEngine);
    }

    void showOptions(TurnState turnState) {
        System.out.println(turnState.options());
    }

    public PreCaptureChoice findPreCaptureChoice(List<TurnOption> options, Position attackerPos, List<Position> targetPositions) {
        return options.stream()
                .filter(PreCaptureOption.class::isInstance)
                .map(o -> ((PreCaptureOption) o).choice())
                .map(PreCaptureChoice.class::cast)
                .filter(c -> {
                    // On extrait toutes les positions cibles de ce choix
                    List<Position> targetsInChoice = c.actions().stream()
                            .map(CaptureAction::targetPosition)
                            .toList();

                    // On vérifie que l'attaquant est le bon ET que TOUTES les cibles attendues sont là
                    boolean sameAttacker = c.actions().stream()
                            .anyMatch(a -> a.attackerPosition().equals(attackerPos));

                    return sameAttacker && targetsInChoice.containsAll(targetPositions)
                            && targetsInChoice.size() == targetPositions.size();
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        String.format("Aucun choix trouvé pour l'attaquant en %s vers les cibles %s",
                                attackerPos, targetPositions)));
    }

    public PreCaptureChoice findPreCaptureChoice(List<TurnOption> options, Position attackerPos, Position... targetPositions) {
        return findPreCaptureChoice(options, attackerPos, List.of(targetPositions));
    }
}