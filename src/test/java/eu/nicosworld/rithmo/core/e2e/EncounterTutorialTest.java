package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.status.CaptureTypeDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

        UUID skipId = FindOptionHelper.findSkipOption(status);

        GameStatusDTO nextStatus = gameFacade.play(game.getId(), skipId);

        // Assertion utilisant directement l'enum PhaseDTO
        assertThat(nextStatus.phase()).isEqualTo(PhaseDTO.MOVE);
        //assertThat(nextStatus.possibleOptions()).allMatch(opt -> opt instanceof MoveOptionDTO);
    }

    @Test
    @DisplayName("2. Choisir une capture simple (1 cible) doit mener à une phase de MOVE")
    void shouldLeadToMovesWhenSingleCaptureIsChosen() throws Exception {
        Game game = PreDefinedTestGame.encounterPreCaptureTestCase();
        GameStatusDTO status = gameFacade.startGame(game);

        UUID landingId = FindOptionHelper.findDecisionWithCaptures(status,1);
        GameStatusDTO nextStatus = gameFacade.play(game.getId(), landingId);

        assertThat(nextStatus.phase()).isEqualTo(PhaseDTO.MOVE);
        assertThat(nextStatus.possibleOptions()
                .values()
                .stream()
                .flatMap(Set::stream)
                .allMatch(MoveOptionDTO.class::isInstance));
    }

    @Test
    @DisplayName("3. Choisir la double capture doit lever une VictoryException")
    void shouldThrowVictoryExceptionWhenDoubleCaptureIsChosen() throws Exception {
        Game game = PreDefinedTestGame.encounterPreCaptureTestCase();
        GameStatusDTO status = gameFacade.startGame(game);

        UUID landingId = FindOptionHelper.findDecisionWithCaptures(status, 2);

        assertThatThrownBy(() -> gameFacade.play(game.getId(), landingId))
                .isInstanceOf(VictoryException.class);
    }

    @Test
    @DisplayName("4. Il doit y avoir 3 options")
    void shouldPropose3Options_2CapturesFrom2DifferentPieceWhoTargetSameTarget() throws Exception {
        Game game = PreDefinedTestGame.encounterPreCaptureTestCase_WhitePlayer();
        GameStatusDTO status = gameFacade.startGame(game);
        System.out.println(game.getCurrentState().state().board().prettyPrint());

        List<PreCaptureOptionDTO> options = status.possibleOptions().values().stream()
                .flatMap(Set::stream)
                .filter(PreCaptureOptionDTO.class::isInstance)
                .map(PreCaptureOptionDTO.class::cast)
                .filter(a -> a.type().equals(CaptureTypeDTO.ENCOUNTER))
                .toList();
        System.out.println("IN TEST");
        TestDebugger.print(options);

        assertThat(status.possibleOptions().size()).isEqualTo(3);
        assertThat(options.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("4. on doit avoir une option de pre capture en ambush")
    void shouldProposeAPreCaptureOption_WhiteAttacker2PyramidsAndAnotherTarget() throws Exception {

        Game game = PreDefinedTestGame.encounterPreCaptureTest_WhiteAttacker2PyramidsAndAnotherTarget();
        GameStatusDTO status = gameFacade.startGame(game);

        PieceDTO pieceDTO = FindOptionHelper.findComponent(status.board(), new Position(2,0), 5);
        System.out.println(pieceDTO);

        TestDebugger.render(status);
        System.out.println("possible options");
        TestDebugger.print(status.possibleOptions());

        Set<PreCaptureOptionDTO> optionList = status.possibleOptions().get(pieceDTO)
                .stream()
                .filter(o -> o instanceof PreCaptureOptionDTO)
                .map(PreCaptureOptionDTO.class::cast)
                .collect(Collectors.toSet());

        assertThat(status.phase()).isEqualTo(PhaseDTO.PRE_CAPTURE);
        assertThat(status.possibleOptions().size() == 7);

        UUID id = FindOptionHelper.findPreCaptureDecisionId(status, pieceDTO, optionList, new Position(3, 1));

        System.out.println("id = " + id);

        GameStatusDTO statusAfterPreCapture = gameFacade.play(game.getId(), id);
        assertThat(statusAfterPreCapture.phase()).isEqualTo(PhaseDTO.MOVE);
    }
}