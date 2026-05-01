package eu.nicosworld.rithmo.core.e2e;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

        UUID skipId = findOptionIdByType(status, SkipOptionDTO.class);
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

        PreCaptureOptionDTO assaultOpt = statusStart.possibleOptions().stream()
                .filter(PreCaptureOptionDTO.class::isInstance)
                .map(PreCaptureOptionDTO.class::cast)
                .findFirst().orElseThrow();

        // On joue le premier landing choice (actionId technique)
        UUID landingId = assaultOpt.choices().getFirst().actionId();
        GameStatusDTO statusAfterPreCapture = gameFacade.play(gameId, landingId);

        // Assert : On doit être en phase MOVE pour l'assaut
        assertThat(statusAfterPreCapture.phase()).isEqualTo(PhaseDTO.MOVE);

        // --- ÉTAPE 2 : MOVE -> Vers la position (2,2) ---
        MoveOptionDTO moveOpt = statusAfterPreCapture.possibleOptions().stream()
                .filter(MoveOptionDTO.class::isInstance)
                .map(MoveOptionDTO.class::cast)
                .filter(m -> m.to().equals(new Position(2, 2)))
                .findFirst().orElseThrow();

        GameStatusDTO statusAfterMove = gameFacade.play(gameId, moveOpt.id());

        // Assert : On doit être en phase POST_CAPTURE
        assertThat(statusAfterMove.phase()).isEqualTo(PhaseDTO.POST_CAPTURE);
        // --- ÉTAPE 3 : POST_CAPTURE -> Vérification des options et Victoire ---

// On vérifie qu'on a bien le choix entre capturer et passer (Skip)
        assertThat(statusAfterMove.possibleOptions())
                .as("La phase POST_CAPTURE doit proposer la capture ET le skip")
                .extracting(dto -> dto.getClass().getSimpleName())
                .containsExactlyInAnyOrder("PostCaptureOptionDTO", "SkipOptionDTO");

        PostCaptureOptionDTO postOpt = statusAfterMove.possibleOptions()
                .stream()
                .filter(PostCaptureOptionDTO.class::isInstance)
                .map(PostCaptureOptionDTO.class::cast)
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> gameFacade.play(gameId, postOpt.id()))
                .isInstanceOf(VictoryException.class);
    }

    // Helper pour simplifier la lecture
    private <T extends PlayerOptionDTO> UUID findOptionIdByType(GameStatusDTO status, Class<T> clazz) {
        return status.possibleOptions().stream()
                .filter(clazz::isInstance)
                .map(opt -> {
                    return switch (opt) {
                        case MoveOptionDTO m -> m.id();
                        case SkipOptionDTO s -> s.id();
                        case PostCaptureOptionDTO p -> p.id();
                        default ->
                                throw new UnsupportedOperationException("L'ID doit être extrait manuellement pour ce type");
                    };
                })
                .findFirst().orElseThrow();
    }
}
