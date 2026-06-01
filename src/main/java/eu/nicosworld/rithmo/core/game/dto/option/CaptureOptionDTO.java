package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.status.CaptureTypeDTO;
import java.util.List;

/**
 * UI-facing shared abstraction for capture-based player options.
 *
 * <p>{@code CaptureOptionDTO} represents the common projection contract for all legal capture
 * choices exposed to the client layer, independently of the phase in which the capture occurs.
 *
 * <p>It defines the shared descriptive metadata required to present a capture opportunity:
 *
 * <ul>
 *   <li>the target piece being captured
 *   <li>the semantic capture type
 *   <li>the supporting allied pieces involved in the capture, if any
 * </ul>
 *
 * <p>Concrete implementations specialize this contract according to capture timing semantics:
 *
 * <ul>
 *   <li>{@link PreCaptureOptionDTO} for captures available before movement
 *   <li>{@link PostCaptureOptionDTO} for captures available during post-capture resolution
 * </ul>
 *
 * <p>This abstraction exists to enable uniform client-side handling of capture options while
 * preserving phase-specific extensions where required.
 */
public sealed interface CaptureOptionDTO extends PlayerOptionDTO
    permits PostCaptureOptionDTO, PreCaptureOptionDTO {
  PieceDTO target();

  CaptureTypeDTO type();

  List<PieceDTO> ally();
}
