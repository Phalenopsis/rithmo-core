package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.helper.FindDecisionHelper;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import eu.nicosworld.rithmo.core.helper.TestDebugger;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.helper.PreDefinedTestGame;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.engine.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncounterTutorialTest {

    private GameFacade gameFacade;

    @BeforeEach
    void setUp() {
        InMemoryGameRepository gameRepository = new InMemoryGameRepository();
        InMemoryOptionRepository optionRepository = new InMemoryOptionRepository();
        gameFacade = new GameFacade(gameRepository, optionRepository);
    }

    @Test
    @DisplayName("1. Choisir SKIP doit mener à une phase de MOVE")
    void shouldLeadToMovesWhenSkipIsChosen() throws Exception {
        Game game = PreDefinedTestGame.encounterPreCaptureTestCase();
        GameStatusDTO status = gameFacade.startGame(game);

        UUID skipId = FindDecisionHelper.findSkipDecision(status);

        GameStatusDTO nextStatus = gameFacade.play(game.getId(), skipId);

        StatusDTOAssertion.from(nextStatus)
                        .isInMovePhase()
                        .hasOnlyMoveDecisions();
    }

    @Test
    @DisplayName("2. Choisir une capture simple (1 cible) doit mener à une phase de MOVE")
    void shouldLeadToMovesWhenSingleCaptureIsChosen() throws Exception {
        Game game = PreDefinedTestGame.encounterPreCaptureTestCase();
        GameStatusDTO status = gameFacade.startGame(game);

        UUID landingId = FindDecisionHelper.findDecisionWithCaptures(status,1);
        GameStatusDTO nextStatus = gameFacade.play(game.getId(), landingId);

        StatusDTOAssertion.from(nextStatus)
                .isInMovePhase()
                .hasOnlyMoveDecisions();
    }

    @Test
    @DisplayName("3. Choisir la double capture doit lever une VictoryException")
    void shouldThrowVictoryExceptionWhenDoubleCaptureIsChosen() throws Exception {
        Game game = PreDefinedTestGame.encounterPreCaptureTestCase();
        GameStatusDTO status = gameFacade.startGame(game);

        UUID landingId = FindDecisionHelper.findDecisionWithCaptures(status, 2);

        assertThatThrownBy(() -> gameFacade.play(game.getId(), landingId))
                .isInstanceOf(VictoryException.class);
    }

    @Test
    @DisplayName("4. Il doit y avoir 3 options")
    void shouldPropose3Options_2CapturesFrom2DifferentPieceWhoTargetSameTarget() throws Exception {
        Game game = PreDefinedTestGame.encounterPreCaptureTestCase_WhitePlayer();
        GameStatusDTO status = gameFacade.startGame(game);

        StatusDTOAssertion.from(status)
                .hasActivePlayer(PlayerColorDTO.WHITE)
                .isInPreCapturePhase()
                .haveSkipDecision()
                .hasCaptureDecisionCount(2);
    }

    @Test
    @DisplayName("4. on doit avoir une option de pre capture en ambush")
    void shouldProposeAPreCaptureOption_WhiteAttacker2PyramidsAndAnotherTarget() throws Exception {

        Game game = PreDefinedTestGame.encounterPreCaptureTest_WhiteAttacker2PyramidsAndAnotherTarget();
        GameStatusDTO status = gameFacade.startGame(game);

        TestDebugger.render(status);

        StatusDTOAssertion.from(status)
                .isInPreCapturePhase()
                .hasNOptions(4)
                .hasNDecisions(5)
                .hasNOptionsFor("WC5(2,0)", 2)
                .hasNOptionsFor("WC4(2,0)", 1)
                .hasNDecisionsFor("WC5(2,0)", 3)
                .hasNDecisionsFor("WC4(2,0)", 1);

        UUID id = FindDecisionHelper.findCaptureDecisionId(status, "WC5(2,0)", new Position(3,1), "BC5(3,1)", "BC5(1,1)");

        GameStatusDTO statusAfterPreCapture = gameFacade.play(game.getId(), id);
        StatusDTOAssertion.from(statusAfterPreCapture)
                .hasActivePlayer(PlayerColorDTO.WHITE)
                .isInMovePhase()
                .hasPiece("WP9", "(3,1)")
                .hasOnlyMoveDecisions()
                .hasStrictMoveDecisionTo("(2,0)", "(2,2)")
                .hasNoReintroductionOptions()
                .capturedContains("BC5", "BC5")
                .hasInReserve("WC5");
    }
}