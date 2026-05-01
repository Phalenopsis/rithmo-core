package eu.nicosworld.rithmo.core;

import eu.nicosworld.rithmo.core.game.PendingAction;
import eu.nicosworld.rithmo.core.game.dto.option.*;
import eu.nicosworld.rithmo.core.persistence.OptionRepository;
import eu.nicosworld.rithmo.core.turn.action.PreCaptureAction;
import eu.nicosworld.rithmo.core.turn.option.*;
import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.CaptureType;
import eu.nicosworld.rithmo.engine.model.*;
import eu.nicosworld.rithmo.engine.move.Move;
import eu.nicosworld.rithmo.engine.move.MoveNature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GameFacadeTest {

    @Mock
    private OptionRepository optionRepository;

    @InjectMocks
    private GameFacade gameFacade;

    private final UUID gameId = UUID.randomUUID();

    @Test
    @DisplayName("Devrait grouper les options de capture par cibles identiques")
    void shouldGroupPreCapturesByTargets() {
        // GIVEN
        Position attackerPos = new Position(0, 0);
        Position targetPos = new Position(1, 1);
        Position landingA = new Position(2, 2);
        Position landingB = new Position(3, 3);

        CaptureAction capture = createCapture(attackerPos, targetPos);

        // Deux options avec la même cible, mais des atterrissages différents
        PreCaptureOption opt1 = new PreCaptureOption(List.of(capture), landingA);
        PreCaptureOption opt2 = new PreCaptureOption(List.of(capture), landingB);

        List<TurnOption> options = List.of(opt1, opt2);

        // WHEN
        List<PlayerOptionDTO> result = gameFacade.processPreCaptureOptions(gameId, options);

        // THEN
        assertThat(result).hasSize(1);
        PreCaptureOptionDTO dto = (PreCaptureOptionDTO) result.getFirst();

        assertThat(dto.targets()).containsExactly(targetPos);
        assertThat(dto.choices()).hasSize(2);
        assertThat(dto.choices().stream().map(LandingChoiceDTO::landingPosition))
                .containsExactlyInAnyOrder(landingA, landingB);

        verify(optionRepository, times(3)).save(any(PendingAction.class));
    }

    @Test
    @DisplayName("Devrait créer deux DTOs distincts pour des cibles différentes")
    void shouldCreateSeparateDTOsForDifferentTargets() {
        // GIVEN
        Position attackerPos = new Position(0, 0);
        Position targetPos1 = new Position(1, 1);
        Position targetPos2 = new Position(2, 2);

        CaptureAction cap1 = createCapture(attackerPos, targetPos1);
        CaptureAction cap2 = createCapture(attackerPos, targetPos2);

        // Si on capture Target1, on atterrit en (1,1). Si on capture Target2, en (2,2).
        PreCaptureOption optTarget1 = new PreCaptureOption(List.of(cap1), targetPos1);
        PreCaptureOption optTarget2 = new PreCaptureOption(List.of(cap2), targetPos2);

        // WHEN
        List<PlayerOptionDTO> result = gameFacade.processPreCaptureOptions(gameId, List.of(optTarget1, optTarget2));

        // THEN
        assertThat(result).hasSize(2);

        List<List<Position>> allTargets = result.stream()
                .map(dto -> ((PreCaptureOptionDTO) dto).targets())
                .toList();

        assertThat(allTargets).containsExactlyInAnyOrder(List.of(targetPos1), List.of(targetPos2));
    }

    @Test
    @DisplayName("Devrait sauvegarder les actions atomiques sans DTO et le groupe avec DTO")
    void shouldSaveAtomicActionsAndGroupedDTO() {
        // GIVEN
        Position attackerPos = new Position(0, 0);
        Position targetPos = new Position(1, 1);
        Position landing = new Position(2, 2);

        CaptureAction cap = createCapture(attackerPos, targetPos);
        PreCaptureOption opt = new PreCaptureOption(List.of(cap), landing);

        // WHEN
        gameFacade.processPreCaptureOptions(gameId, List.of(opt));

        // THEN
        ArgumentCaptor<PendingAction> captor = ArgumentCaptor.forClass(PendingAction.class);
        verify(optionRepository, times(2)).save(captor.capture());

        List<PendingAction> savedActions = captor.getAllValues();

        // 1. L'action technique (pour le clic sur la case d'arrivée)
        assertThat(savedActions).anyMatch(a ->
                a.actionToExecute() instanceof PreCaptureAction && a.dto() == null
        );

        // 2. Le DTO groupé (pour l'affichage des boutons/UI)
        assertThat(savedActions).anyMatch(a ->
                a.actionToExecute() == null && a.dto() instanceof PreCaptureOptionDTO
        );
    }

    // =========================
    // HELPERS
    // =========================

    private CaptureAction createCapture(Position attackerPos, Position targetPos) {
        Piece attackerPiece = new SimplePiece(PieceType.CIRCLE, Player.BLACK, 5);
        Piece targetPiece = new SimplePiece(PieceType.CIRCLE, Player.WHITE, 5);

        return new CaptureAction(
                attackerPiece,
                attackerPos,
                targetPiece,
                targetPos,
                targetPiece,
                true,
                CaptureType.ENCOUNTER
        );
    }

    @Test
    @DisplayName("Devrait traiter et persister toutes les options unitaires (Move, PostCapture, Skips)")
    void shouldProcessAllUnitaryOptions() {
        // --- GIVEN ---
        Position from = new Position(0, 0);
        Position to = new Position(2, 2);
        Position targetPos = new Position(1, 1);

        // 1. Une option de mouvement
        MoveOption moveOpt = new MoveOption(new Move(from, to, MoveNature.REGULAR));

        // 2. Une option de post-capture
        CaptureAction capture = createCapture(from, targetPos); // Utilise ton helper
        PostCaptureOption postOpt = new PostCaptureOption(List.of(capture));

        // 3. Les options de Skip
        SkipPreCaptureOption skipPre = new SkipPreCaptureOption();
        SkipPostCaptureOption skipPost = new SkipPostCaptureOption();

        // On mélange avec une PreCaptureOption pour vérifier qu'elle est bien filtrée (ignorée)
        PreCaptureOption preCaptureToIgnore = new PreCaptureOption(List.of(capture), to);

        List<TurnOption> allOptions = List.of(moveOpt, postOpt, skipPre, skipPost, preCaptureToIgnore);

        // --- WHEN ---
        List<PlayerOptionDTO> results = gameFacade.processUnitaryOptions(gameId, allOptions);

        // --- THEN ---
        // On attend 4 DTOs (le PreCapture est filtré car traité par une autre méthode)
        assertThat(results).hasSize(4);

        // Vérification des types de DTOs produits
        assertThat(results).extracting(dto -> dto.getClass().getSimpleName())
                .containsExactlyInAnyOrder(
                        "MoveOptionDTO",
                        "PostCaptureOptionDTO",
                        "SkipOptionDTO",
                        "SkipOptionDTO"
                );

        // Vérification de la persistance via ArgumentCaptor
        ArgumentCaptor<PendingAction> captor = ArgumentCaptor.forClass(PendingAction.class);
        // On vérifie que save() a été appelé 4 fois
        verify(optionRepository, times(4)).save(captor.capture());

        List<PendingAction> savedActions = captor.getAllValues();

        // Vérification croisée : Est-ce que les IDs dans les DTOs correspondent aux IDs en base ?
        for (PlayerOptionDTO dto : results) {
            UUID dtoId = getActionIdFromDTO(dto);

            // On vérifie qu'il existe une action en base avec cet ID et ce DTO
            assertThat(savedActions).anyMatch(action ->
                    action.id().equals(dtoId) && action.dto().equals(dto)
            );
        }
    }

    /**
     * Helper pour extraire l'ID selon le type de DTO (puisqu'ils n'ont pas d'interface commune ID)
     */
    private UUID getActionIdFromDTO(PlayerOptionDTO dto) {
        return switch (dto) {
            case MoveOptionDTO m -> m.id();
            case PostCaptureOptionDTO p -> p.id();
            case SkipOptionDTO s -> s.id();
            default -> throw new IllegalArgumentException("Unknown DTO type");
        };
    }
}