package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.PreDefinedGame;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PlayerOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PreCaptureOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.MoveOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PostCaptureOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.SkipOptionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
    void fullGameFlowTest() throws Exception {
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
        PlayerOptionDTO firstOptionDto = statusAfterStart.possibleOptions().get(0);
        UUID actionIdToPlay = extractPlayableId(firstOptionDto);

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

    /**
     * Helper pour simuler le choix de l'UI.
     * Pour une PreCapture, l'ID jouable est dans LandingChoiceDTO.
     * Pour les autres, il est à la racine du record.
     */
    private UUID extractPlayableId(PlayerOptionDTO dto) {
        if (dto instanceof PreCaptureOptionDTO pre) {
            return pre.choices().getFirst().actionId(); // On prend le premier atterrissage possible
        } else if (dto instanceof MoveOptionDTO move) {
            return move.id();
        } else if (dto instanceof PostCaptureOptionDTO post) {
            return post.id();
        } else if (dto instanceof SkipOptionDTO skip) {
            return skip.id();
        }
        throw new IllegalArgumentException("Type d'option inconnu : " + dto.getClass());
    }
}