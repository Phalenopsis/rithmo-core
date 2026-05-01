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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncounterTutorialTest {

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
    @DisplayName("1. Choisir SKIP doit mener à une phase de MOVE")
    void shouldLeadToMovesWhenSkipIsChosen() throws Exception {
        Game game = PreDefinedTestGame.encounterPreCaptureTestCase();
        GameStatusDTO status = gameFacade.startGame(game);

        UUID skipId = status.possibleOptions().stream()
                .filter(SkipOptionDTO.class::isInstance)
                .map(SkipOptionDTO.class::cast)
                .findFirst().orElseThrow().id();

        GameStatusDTO nextStatus = gameFacade.play(game.getId(), skipId);

        // Assertion utilisant directement l'enum PhaseDTO
        assertThat(nextStatus.phase()).isEqualTo(PhaseDTO.MOVE);
        assertThat(nextStatus.possibleOptions()).allMatch(opt -> opt instanceof MoveOptionDTO);
    }

    @Test
    @DisplayName("2. Choisir une capture simple (1 cible) doit mener à une phase de MOVE")
    void shouldLeadToMovesWhenSingleCaptureIsChosen() throws Exception {
        Game game = PreDefinedTestGame.encounterPreCaptureTestCase();
        GameStatusDTO status = gameFacade.startGame(game);

        PreCaptureOptionDTO singleCapture = status.possibleOptions().stream()
                .filter(PreCaptureOptionDTO.class::isInstance)
                .map(PreCaptureOptionDTO.class::cast)
                .filter(opt -> opt.targets().size() == 1)
                .findFirst().orElseThrow();

        // On joue le premier choix d'atterrissage du DTO de pré-capture
        UUID landingId = singleCapture.choices().get(0).actionId();
        GameStatusDTO nextStatus = gameFacade.play(game.getId(), landingId);

        assertThat(nextStatus.phase()).isEqualTo(PhaseDTO.MOVE);
        assertThat(nextStatus.possibleOptions()).allMatch(opt -> opt instanceof MoveOptionDTO);
    }

    @Test
    @DisplayName("3. Choisir la double capture doit lever une VictoryException")
    void shouldThrowVictoryExceptionWhenDoubleCaptureIsChosen() throws Exception {
        Game game = PreDefinedTestGame.encounterPreCaptureTestCase();
        GameStatusDTO status = gameFacade.startGame(game);

        PreCaptureOptionDTO doubleCapture = status.possibleOptions().stream()
                .filter(PreCaptureOptionDTO.class::isInstance)
                .map(PreCaptureOptionDTO.class::cast)
                .filter(opt -> opt.targets().size() == 2)
                .findFirst().orElseThrow();

        UUID landingId = doubleCapture.choices().get(0).actionId();

        assertThatThrownBy(() -> gameFacade.play(game.getId(), landingId))
                .isInstanceOf(VictoryException.class);
    }

    @Test
    @DisplayName("4. Il doit y avoir 3 options")
    void shouldPropose3Options_2CapturesFrom2DifferentPieceWhoTargetSameTarget() throws Exception {
        Game game = PreDefinedTestGame.encounterPreCaptureTestCase_WhitePlayer();
        GameStatusDTO status = gameFacade.startGame(game);
        System.out.println(game.getCurrentState().state().board().prettyPrint());

        List<PreCaptureOptionDTO> options = status.possibleOptions().stream()
                .filter(PreCaptureOptionDTO.class::isInstance)
                .map(PreCaptureOptionDTO.class::cast)
                .toList();
        System.out.println("IN TEST");
        System.out.println(options);

        assertThat(status.possibleOptions().size()).isEqualTo(3);
        assertThat(options.size()).isEqualTo(2);
    }
}