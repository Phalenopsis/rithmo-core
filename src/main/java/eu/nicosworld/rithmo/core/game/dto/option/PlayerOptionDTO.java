package eu.nicosworld.rithmo.core.game.dto.option;

/**
 * UI-facing representation of a legal choice available to the player.
 *
 * <p>PlayerOptionDTO is the projection layer equivalent of a {@code TurnOption} produced by the
 * engine resolvers.
 *
 * <p>It represents a pure "what the player can choose" view, independent of:
 *
 * <ul>
 *   <li>execution logic ({@code TurnAction})
 *   <li>decision identity ({@code DecisionDTO})
 * </ul>
 *
 * <p>Each option is immutable and purely descriptive. It does not contain any executable behavior
 * or side-effect.
 *
 * <p>Concrete implementations map directly from engine-level TurnOptions.
 */
public sealed interface PlayerOptionDTO
    permits CaptureOptionDTO,
        MoveOptionDTO,
        PreCaptureOptionDTO,
        ReintroductionOptionDTO,
        SkipOptionDTO {}
