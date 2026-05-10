package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.helper.FindOptionHelper;
import eu.nicosworld.rithmo.core.helper.TestDebugger;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.helper.PreDefinedTestGame;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.option.*;
import eu.nicosworld.rithmo.core.game.dto.status.PhaseDTO;
import eu.nicosworld.rithmo.engine.model.Position;
import eu.nicosworld.rithmo.engine.testutils.RithmoDebug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssaultTutorialTest {

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
    @DisplayName("1. Choisir SKIP doit mener à une phase de MOVE standard")
    void shouldLeadToMovesWhenSkipIsChosen() throws Exception {
        Game game = PreDefinedTestGame.assaultPreCaptureTutorialTestCase();
        GameStatusDTO status = gameFacade.startGame(game);


        UUID skipId = status.possibleDecisions().stream()
                .filter(DecisionDTO::skip)
                .findFirst()
                .orElseThrow()
                .id();
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
        TestDebugger.render(statusStart);

        UUID id = statusStart.possibleDecisions()
                .stream()
                .filter(d -> !d.skip())
                .findFirst().orElseThrow().id();


        GameStatusDTO statusAfterPreCapture = gameFacade.play(gameId, id);
        TestDebugger.render(statusAfterPreCapture);

        // Assert : On doit être en phase MOVE pour l'assaut
        assertThat(statusAfterPreCapture.phase()).isEqualTo(PhaseDTO.MOVE);

        for(DecisionDTO d : statusAfterPreCapture.possibleDecisions()) {
            System.out.println(d);
        }

        // --- ÉTAPE 2 : MOVE -> Vers la position (2,2) ---
        UUID moveId = FindOptionHelper.findMoveIdByDestination(statusAfterPreCapture, new Position(2, 2));

        GameStatusDTO statusAfterMove = gameFacade.play(gameId,moveId);
        TestDebugger.render(statusAfterMove);

        // Assert : On doit être en phase POST_CAPTURE
        assertThat(statusAfterMove.phase()).isEqualTo(PhaseDTO.POST_CAPTURE);
        // --- ÉTAPE 3 : POST_CAPTURE -> Vérification des options et Victoire ---

        // On vérifie qu'on a bien le choix entre capturer et passer (Skip)
        assertThat(statusAfterMove.possibleOptions().values().stream().flatMap(Set::stream))
                .as("La phase POST_CAPTURE doit proposer la capture ET le skip")
                .extracting(dto -> dto.getClass().getSimpleName())
                .containsExactlyInAnyOrder("CaptureOptionDTO", "SkipOptionDTO");

        for(DecisionDTO d : statusAfterMove.possibleDecisions()) {
            System.out.println(d);
        }

        DecisionDTO captureDecision = statusAfterMove.possibleDecisions().stream()
                .filter(d -> !d.skip())
                .findFirst()
                .orElseThrow();

        UUID captureId = captureDecision.id();


        assertThatThrownBy(() -> gameFacade.play(gameId, captureId))
                .isInstanceOf(VictoryException.class);
    }
}
