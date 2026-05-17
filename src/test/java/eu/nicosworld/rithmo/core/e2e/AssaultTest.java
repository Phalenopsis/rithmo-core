package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.helper.FindDecisionHelper;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.helper.PreDefinedTestGame;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PhaseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssaultTest {

    private GameFacade gameFacade;

    @BeforeEach
    void setUp() {
        InMemoryGameRepository gameRepository = new InMemoryGameRepository();
        InMemoryOptionRepository optionRepository = new InMemoryOptionRepository();
        gameFacade = new GameFacade(gameRepository, optionRepository);
    }

    @Test
    @DisplayName("1. Choisir SKIP doit mener à une phase de MOVE standard")
    void shouldLeadToMovesWhenSkipIsChosen() throws Exception {
        Game game = PreDefinedTestGame.assaultPreCaptureTutorialTestCase();
        GameStatusDTO status = gameFacade.startGame(game);

        UUID skipId = FindDecisionHelper.findSkipDecision(status);
        GameStatusDTO nextStatus = gameFacade.play(game.getId(), skipId);

        assertThat(nextStatus.phase()).isEqualTo(PhaseDTO.MOVE);
    }

    @Test
    @DisplayName("2. Flux complet Assault : PreCapture -> Move -> PostCapture -> Victoire")
    void fullAssaultFlowTest() throws Exception {
        Game game = PreDefinedTestGame.assaultPreCaptureTutorialTestCase();
        UUID gameId = game.getId();

        // --- ÉTAPE 1 : START -> Sélection de la PreCapture ---
        GameStatusDTO statusStart = gameFacade.startGame(game);

        UUID id = FindDecisionHelper.findCaptureDecisionIdWithLanding(
                statusStart,
                "BC4(0,1)",
                "(3,1)",
                "WC8(3,1)");
        GameStatusDTO statusAfterPreCapture = gameFacade.play(gameId, id);
        StatusDTOAssertion.from(statusAfterPreCapture)
                .isInMovePhase();

        // --- ÉTAPE 2 : MOVE -> Vers la position (2,2) ---
        UUID moveId = FindDecisionHelper.findMoveDecisionId(
                statusAfterPreCapture,
                "BC4(3,1)",
                "(2,2)");
        GameStatusDTO statusAfterMove = gameFacade.play(gameId,moveId);
        StatusDTOAssertion.from(statusAfterMove)
                .isInPostCapturePhase()
                .haveSkipDecision()
                .canCaptureInOneDecision("WC4");

        UUID captureId = FindDecisionHelper.findCaptureDecisionId(
                statusAfterMove,
                "BC4(2,2)",
                "WC4(3,3)");
        assertThatThrownBy(() -> gameFacade.play(gameId, captureId))
                .isInstanceOf(VictoryException.class);
    }
}
