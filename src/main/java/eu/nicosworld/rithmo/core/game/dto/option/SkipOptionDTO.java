package eu.nicosworld.rithmo.core.game.dto.option;

/**
 * UI-facing representation of an explicit "skip" option available to the player.
 *
 * <p>A {@code SkipOptionDTO} is derived from a skip-type engine option produced by the {@code
 * PhaseResolver}.
 *
 * <p>It represents the player's ability to deliberately skip an optional phase of the turn, such
 * as:
 *
 * <ul>
 *   <li>the pre-capture phase ({@code SkipPreCaptureOption})
 *   <li>the post-capture phase ({@code SkipPostCaptureOption})
 * </ul>
 *
 * <p>Important: the move phase cannot be skipped. It is always mandatory once the game reaches that
 * stage of the turn lifecycle.
 *
 * <p>Unlike other {@link PlayerOptionDTO}s, this option is global and does not target any specific
 * piece or board position. It directly leads to a {@link
 * eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO} marked as a skip.
 *
 * <p>This DTO is strictly a presentation artifact and is not executable. Execution is handled
 * through the associated decision selection in the UI.
 */
public record SkipOptionDTO() implements PlayerOptionDTO {}
