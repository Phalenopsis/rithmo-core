package eu.nicosworld.rithmo.core;

import eu.nicosworld.rithmo.core.game.PendingAction;
import eu.nicosworld.rithmo.core.game.dto.option.*;
import eu.nicosworld.rithmo.core.persistence.OptionRepository;
import eu.nicosworld.rithmo.core.turn.action.PreCaptureAction;
import eu.nicosworld.rithmo.core.turn.option.*;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.model.InvolvedPiece;
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

        assertThat(savedActions).anyMatch(a ->
                a.actionToExecute() instanceof PreCaptureAction && a.dto() == null
        );

        assertThat(savedActions).anyMatch(a ->
                a.actionToExecute() == null && a.dto() instanceof PreCaptureOptionDTO
        );
    }

    @Test
    @DisplayName("Devrait traiter et persister toutes les options unitaires (Move, PostCapture, Skips)")
    void shouldProcessAllUnitaryOptions() {
        // --- GIVEN ---
        Position from = new Position(0, 0);
        Position to = new Position(2, 2);
        Position targetPos = new Position(1, 1);

        MoveOption moveOpt = new MoveOption(new Move(from, to, MoveNature.REGULAR));

        CaptureAction capture = createCapture(from, targetPos);
        PostCaptureOption postOpt = new PostCaptureOption(List.of(capture));

        SkipPreCaptureOption skipPre = new SkipPreCaptureOption();
        SkipPostCaptureOption skipPost = new SkipPostCaptureOption();

        PreCaptureOption preCaptureToIgnore = new PreCaptureOption(List.of(capture), to);

        List<TurnOption> allOptions = List.of(moveOpt, postOpt, skipPre, skipPost, preCaptureToIgnore);

        // --- WHEN ---
        List<PlayerOptionDTO> results = gameFacade.processUnitaryOptions(gameId, allOptions);

        // --- THEN ---
        assertThat(results).hasSize(4);

        assertThat(results).extracting(dto -> dto.getClass().getSimpleName())
                .containsExactlyInAnyOrder(
                        "MoveOptionDTO",
                        "PostCaptureOptionDTO",
                        "SkipOptionDTO",
                        "SkipOptionDTO"
                );

        ArgumentCaptor<PendingAction> captor = ArgumentCaptor.forClass(PendingAction.class);
        verify(optionRepository, times(4)).save(captor.capture());

        List<PendingAction> savedActions = captor.getAllValues();

        for (PlayerOptionDTO dto : results) {
            UUID dtoId = getActionIdFromDTO(dto);
            assertThat(savedActions).anyMatch(action ->
                    action.id().equals(dtoId) && action.dto().equals(dto)
            );
        }
    }

    // =========================
    // HELPERS
    // =========================

    private CaptureAction createCapture(Position attackerPos, Position targetPos) {
        Piece attackerPiece = new SimplePiece(PieceType.CIRCLE, Player.BLACK, 5);
        Piece targetPiece = new SimplePiece(PieceType.CIRCLE, Player.WHITE, 5);

        // Utilisation des factories conformes à la 0.5.0
        return CaptureAction.encounter(
                InvolvedPiece.whole(attackerPiece, attackerPos),
                InvolvedPiece.whole(targetPiece, targetPos)
        );
    }

    private UUID getActionIdFromDTO(PlayerOptionDTO dto) {
        return switch (dto) {
            case MoveOptionDTO m -> m.id();
            case PostCaptureOptionDTO p -> p.id();
            case SkipOptionDTO s -> s.id();
            default -> throw new IllegalArgumentException("Unknown DTO type");
        };
    }
}