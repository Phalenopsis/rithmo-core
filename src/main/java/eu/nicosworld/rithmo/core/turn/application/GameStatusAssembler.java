package eu.nicosworld.rithmo.core.turn.application;

import eu.nicosworld.rithmo.core.UiInformation;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.*;
import eu.nicosworld.rithmo.core.persistence.OptionRepository;
import eu.nicosworld.rithmo.core.turn.action.*;
import eu.nicosworld.rithmo.core.turn.application.decision.DecisionRegistry;
import eu.nicosworld.rithmo.core.turn.application.projection.ExecutableDecision;
import eu.nicosworld.rithmo.core.turn.application.projection.TurnProjection;
import eu.nicosworld.rithmo.core.turn.option.*;
import java.util.*;

/**
 * Orchestrates the projection of the current game state into a UI-consumable structure.
 *
 * <p>This class is responsible for transforming engine-generated {@link TurnOption}s into a
 * structured representation of:
 *
 * <ul>
 *   <li>visible player options
 *   <li>executable decisions bound to engine actions
 * </ul>
 *
 * <p>It acts as the boundary between the engine layer and the external UI layer (web, JavaFX, AI
 * clients, etc.), ensuring that no engine-specific logic leaks into UI consumption models.
 *
 * <p>The assembly process follows a two-step pipeline:
 *
 * <ol>
 *   <li>Each {@link TurnOption} is projected into a {@link TurnProjection}
 *   <li>Projections are aggregated into a final {@link UiInformation} object
 * </ol>
 *
 * <p>This design removes all index-based coupling between actions and decisions by relying on
 * explicit {@link ExecutableDecision} bindings.
 *
 * <p>Internally, a {@link DecisionRegistry} is used to:
 *
 * <ul>
 *   <li>ensure stable decision identity across the game session
 *   <li>persist executable actions associated with decisions
 *   <li>deduplicate equivalent decision structures
 * </ul>
 *
 * <p>This class is intentionally stateless with respect to game logic, and only performs projection
 * and orchestration responsibilities.
 */
public class GameStatusAssembler {

  private final OptionRepository optionRepository;

  public GameStatusAssembler(OptionRepository optionRepository) {
    this.optionRepository = optionRepository;
  }

  public UiInformation assemble(Game game) {

    Map<PieceDTO, Set<PlayerOptionDTO>> playerOptionPerPiece = new HashMap<>();

    DecisionRegistry decisionRegistry = new DecisionRegistry(optionRepository);

    for (TurnOption option : game.getCurrentState().options()) {

      TurnProjection projection = presentOption(option);

      addOptions(playerOptionPerPiece, projection.piece(), projection.options());

      for (ExecutableDecision executable : projection.executableDecisions()) {

        decisionRegistry.register(game.getId(), executable.action(), executable.decision());
      }
    }

    return new UiInformation(playerOptionPerPiece, decisionRegistry.getDecisions());
  }

  private TurnProjection presentOption(TurnOption option) {

    return switch (option) {
      case MoveOption moveOption -> presentMoveOption(moveOption);

      case PostCaptureOption postCaptureOption -> presentPostCaptureOption(postCaptureOption);

      case PreCaptureOption preCaptureOption -> presentPreCaptureOption(preCaptureOption);

      case ReintroductionOption reintroductionOption ->
          presentReintroductionOption(reintroductionOption);

      case SkipPreCaptureOption skipPreCaptureOption -> presentSkipOption(skipPreCaptureOption);

      case SkipPostCaptureOption skipPostCaptureOption -> presentSkipOption(skipPostCaptureOption);
    };
  }

  private TurnProjection presentMoveOption(MoveOption moveOption) {

    MoveAction action = MoveAction.from(moveOption);

    DecisionDTO decision = DecisionDTO.from(UUID.randomUUID(), action);

    MoveOptionDTO optionDTO = MoveOptionDTO.from(moveOption);

    ExecutableDecision executable = new ExecutableDecision(decision, action);

    return new TurnProjection(optionDTO.actor(), List.of(optionDTO), List.of(executable));
  }

  private TurnProjection presentPostCaptureOption(PostCaptureOption postCaptureOption) {

    PostCaptureAction action = PostCaptureAction.from(postCaptureOption);

    PieceDTO actorDTO = PieceDTO.from(postCaptureOption.actor());

    DecisionDTO rawDecision = DecisionDTO.from(UUID.randomUUID(), action);

    List<PlayerOptionDTO> optionDTOs =
        new ArrayList<>(PostCaptureOptionDTO.from(postCaptureOption));

    return new TurnProjection(
        actorDTO, optionDTOs, List.of(new ExecutableDecision(rawDecision, action)));
  }

  private TurnProjection presentPreCaptureOption(PreCaptureOption option) {

    List<PreCaptureAction> actions = PreCaptureAction.from(option);

    PieceDTO actorDTO = PieceDTO.from(option.actor());

    List<PlayerOptionDTO> optionDTOs = new ArrayList<>(PreCaptureOptionDTO.from(option));

    List<ExecutableDecision> executableDecisions =
        actions.stream()
            .map(
                action -> {
                  DecisionDTO decision = DecisionDTO.from(UUID.randomUUID(), action);

                  return new ExecutableDecision(decision, action);
                })
            .toList();

    return new TurnProjection(actorDTO, optionDTOs, executableDecisions);
  }

  private TurnProjection presentReintroductionOption(ReintroductionOption reintroductionOption) {

    ReintroductionAction action = ReintroductionAction.from(reintroductionOption);

    ReintroductionOptionDTO optionDTO = ReintroductionOptionDTO.from(reintroductionOption);

    DecisionDTO rawDecision = DecisionDTO.from(UUID.randomUUID(), action);

    return new TurnProjection(
        optionDTO.pieceDTO(),
        List.of(optionDTO),
        List.of(new ExecutableDecision(rawDecision, action)));
  }

  private TurnProjection presentSkipOption(TurnOption option) {

    TurnAction action = mapToSkipTurnAction(option);

    DecisionDTO decision = DecisionDTO.skipFrom(UUID.randomUUID());

    return new TurnProjection(
        PieceDTO.GLOBAL_OPTION,
        List.of(new SkipOptionDTO()),
        List.of(new ExecutableDecision(decision, action)));
  }

  private void addOptions(
      Map<PieceDTO, Set<PlayerOptionDTO>> playerOptions,
      PieceDTO pieceDTO,
      List<PlayerOptionDTO> optionDTOs) {

    playerOptions.computeIfAbsent(pieceDTO, k -> new HashSet<>()).addAll(optionDTOs);
  }

  /** Maps a selection option to its corresponding executable action. */
  private TurnAction mapToSkipTurnAction(TurnOption option) {

    return switch (option) {
      case SkipPreCaptureOption skipPreCaptureOption ->
          SkipPreCaptureAction.from(skipPreCaptureOption);

      case SkipPostCaptureOption skipPostCaptureOption ->
          SkipPostCaptureAction.from(skipPostCaptureOption);

      default -> throw new RuntimeException("Bad Turn Option");
    };
  }
}
