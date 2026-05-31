package eu.nicosworld.rithmo.core.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.PreDefinedGame;
import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.helper.FindDecisionHelper;
import eu.nicosworld.rithmo.core.helper.PreDefinedTestGame;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import eu.nicosworld.rithmo.engine.model.Position;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameFacadeE2ETest {

    private GameFacade gameFacade;
    private InMemoryGameRepository gameRepository;
    private InMemoryOptionRepository optionRepository;

    @BeforeEach
    void setUp() {
        gameRepository = new InMemoryGameRepository();
        optionRepository = new InMemoryOptionRepository();
        gameFacade = new GameFacade(gameRepository, optionRepository);
    }

    @Test
    @DisplayName("Flux complet : Démarrage et exécution d'une action (gère Moves et PreCaptures)")
    void fullGameFlowTest_withRepositoryVerification() throws Exception {
        // 1. ARRANGEMENT
        Game initialGame = PreDefinedGame.predefinedVerySimpleGame();
        UUID gameId = initialGame.getId();

        // 2. ACTION : Démarrage du jeu
        GameStatusDTO statusAfterStart = gameFacade.startGame(initialGame);

        // Vérification initiale : on a des options et le jeu est sauvé
        assertThat(gameRepository.findById(gameId)).isPresent();
        assertThat(statusAfterStart.possibleOptions()).isNotEmpty();

        // 3. ACTION : Sélection et exécution de la première option jouable
        // On simule l'UI qui doit extraire un ID valide pour le moteur
        UUID actionIdToPlay = FindDecisionHelper.findAnyNonSkipDecision(statusAfterStart);

        GameStatusDTO statusAfterPlay = gameFacade.play(gameId, actionIdToPlay);

        // 4. ASSERTIONS FINALES
        // Le jeu en base doit avoir évolué (TurnState différent)
        Game savedGame = gameRepository.findById(gameId).orElseThrow();
        assertThat(savedGame.getCurrentState())
                .as("L'état du tour doit avoir été mis à jour en base")
                .isNotEqualTo(initialGame.getCurrentState());

        // L'OptionRepository doit avoir été nettoyé
        assertThat(optionRepository.findById(actionIdToPlay))
                .as("L'action jouée ne doit plus exister dans le repository")
                .isEmpty();

        // Le nouveau DTO doit être cohérent
        assertThat(statusAfterPlay.gameId()).isEqualTo(gameId);
        assertThat(statusAfterPlay.possibleOptions())
                .as("De nouvelles options doivent être générées pour le nouveau tour/phase")
                .isNotNull();
    }

    @Test
    @DisplayName("Pyramid vs Pyramid : Power and Encounter")
    void testPyramidVsPyramid() throws VictoryException, PatException {
        Game initialGame = PreDefinedTestGame.pyramidVsPyramid();
        GameStatusDTO statusAfterStart = gameFacade.startGame(initialGame);

        StatusDTOAssertion.from(statusAfterStart)
                .status()
                    .isInPreCapturePhase()
                .decisions()
                    .canCaptureInOneDecision("BT16(0,0)", "BC4(0,0)");

        UUID id = FindDecisionHelper.findCaptureDecisionId(
                statusAfterStart,
                "WC16(0,3)",
                "BT16(0,0)", "BC4(0,0)");
        GameStatusDTO statusAfterCapture = gameFacade.play(initialGame.getId(), id);

        StatusDTOAssertion.from(statusAfterCapture)
                .status()
                    .isInMovePhase()
                .board()
                    .hasPyramidValue(PlayerColorDTO.BLACK, 71)
                    .hasPyramidComposedBy(PlayerColorDTO.BLACK, "BS36", "BS25", "BT9", "BC1")
                .decisions()
                    .hasOnlyMoveDecisions()
                    .hasStrictMoveDecisionTo("(3,3)", "(1,2)", "(0,1)", "(2,3)")
                    .hasOnlyDecisionsFor("WP190(0,3)");
    }

    @Test
    @DisplayName("Pyramid vs incomplete Pyramid : Power and Encounter")
    void testPyramidVsIncompleteBlackPyramid() throws VictoryException, PatException {
        Game initialGame = PreDefinedTestGame.pyramidVsIncompleteBlackPyramid();
        GameStatusDTO statusAfterStart = gameFacade.startGame(initialGame);

        StatusDTOAssertion.from(statusAfterStart)
                .status()
                    .isInPreCapturePhase()
                .decisions()
                    .canCaptureInOneDecision("BT16(0,0)", "BC4(0,0)");

        UUID id = FindDecisionHelper.findCaptureDecisionId(
                statusAfterStart,
                "WC16(0,3)",
                "BT16(0,0)", "BC4(0,0)");

        GameStatusDTO statusAfterCapture = gameFacade.play(initialGame.getId(), id);

        StatusDTOAssertion.from(statusAfterCapture)
                .status()
                    .hasActivePlayer(PlayerColorDTO.WHITE)
                    .isInMovePhase()
                .board()
                    .hasPyramidComposedBy(PlayerColorDTO.BLACK, "BS36")
                    .hasPyramidValue(PlayerColorDTO.BLACK, 36)
                .assets()
                    .capturedContains("BT16", "BC4")
                    .reserveDoesNotContain("BT16", "BC4")
                .options()
                    .hasNoReintroductionOptions()
                .decisions()
                    .hasOnlyMoveDecisions()
                    .hasOnlyDecisionsFor("WP190(0,3)")
                    .hasStrictMoveDecisionTo("(3,3)", "(1,2)", "(0,1)", "(2,3)");
    }

    @Test
    @DisplayName("Test for capture after reintroduction")
    void testForCaptureAfterReintroduction() throws VictoryException, PatException {
        Game initialGame = PreDefinedTestGame.gameTestForCaptureAfterReintroduction();
        UUID gameId = initialGame.getId();
        GameStatusDTO statusAfterStart = gameFacade.startGame(initialGame);

        UUID captureId = FindDecisionHelper.findDecisionWithCaptures(statusAfterStart,1);
        GameStatusDTO statusDTO1 = gameFacade.play(gameId, captureId);

        UUID moveTo23Id = FindDecisionHelper.findMoveDecisionId(statusDTO1, new Position(2,3));
        GameStatusDTO statusDTO2 = gameFacade.play(gameId, moveTo23Id);

        UUID moveTo22Id = FindDecisionHelper.findMoveDecisionId(statusDTO2, new Position(2,2));
        GameStatusDTO statusDTO3 = gameFacade.play(gameId, moveTo22Id);

        UUID reintroductionId = FindDecisionHelper.findReintroductionIdByDestination(
                statusDTO3,
                "BT4",
                new Position(0, 2)
        );
        GameStatusDTO statusDTO4 = gameFacade.play(gameId, reintroductionId);

        StatusDTOAssertion.from(statusDTO4)
                .status()
                    .isInPostCapturePhase()
                    .hasActivePlayer(PlayerColorDTO.BLACK)
                .assets()
                    .reserveDoesNotContain("BT4")
                    .capturedContains("WT4")
                .decisions()
                    .canCaptureInOneDecision("WC4(2,2)");

        UUID captureAfterReintroductionId = FindDecisionHelper.findDecisionWithCaptures(statusDTO4, 1);
        assertThatThrownBy(() -> gameFacade.play(gameId, captureAfterReintroductionId))
                .isInstanceOf(PatException.class)
                .hasMessage("WHITE is pat");

    }
}