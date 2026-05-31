package eu.nicosworld.rithmo.core.game.dto.board;

import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.engine.capture.model.InvolvedPiece;
import eu.nicosworld.rithmo.engine.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UI-facing representation of a game piece in its current or contextual state.
 *
 * <p>A {@code PieceDTO} is a read-model projection of a domain {@link Piece} or {@link
 * PieceAtPosition}, used exclusively for UI rendering and option display. It is not part of the
 * engine model and carries no behavior.
 *
 * <p>This DTO is designed to support both:
 *
 * <ul>
 *   <li>simple pieces (direct mapping of a single engine piece)
 *   <li>composite pieces (such as pyramids, which expose their internal components)
 * </ul>
 *
 * <p>For composite structures like pyramids, the {@code components} field contains a flattened
 * representation of the internal pieces, all contextualized at the same board position.
 *
 * <p>Special cases:
 *
 * <ul>
 *   <li>{@code #GLOBAL_OPTION} is used as a sentinel value for non-piece-related UI options (e.g.
 *       skip actions)
 *   <li>{@code #empty()} represents a null-equivalent placeholder and should only be used
 *       internally
 * </ul>
 *
 * @param id unique identifier of the piece
 * @param position current position of the piece on the board (may be {@code null} in some
 *     projections)
 * @param shape geometric or logical shape of the piece
 * @param value numeric value associated with the piece (game-specific rule)
 * @param owner owner/player of the piece
 * @param components list of sub-components if the piece is composite (empty otherwise)
 */
public record PieceDTO(
    String id,
    Position position,
    PieceShape shape,
    int value,
    PlayerColorDTO owner,
    List<PieceDTO> components // only for PYRAMID
    ) {
  public static final PieceDTO GLOBAL_OPTION = PieceDTO.empty();

  public static PieceDTO empty() {
    return new PieceDTO(null, null, null, 0, null, null);
  }

  public static PieceDTO from(PieceAtPosition pieceAtPosition) {
    List<PieceDTO> components = new ArrayList<>();

    if (pieceAtPosition.piece() instanceof Pyramid pyramid) {
      for (Piece component : pyramid.getComponents()) {
        components.add(PieceDTO.componentFrom(component, pieceAtPosition.position()));
      }
    }

    return new PieceDTO(
        pieceAtPosition.piece().getId(),
        pieceAtPosition.position(),
        PieceShape.mapShape(pieceAtPosition.piece().getType()),
        pieceAtPosition.piece().getValue(),
        PlayerColorDTO.mapColor(pieceAtPosition.piece().getPlayer().getColor()),
        components);
  }

  public static PieceDTO componentFrom(Piece piece, Position position) {
    if (piece instanceof Pyramid) {
      throw new IllegalArgumentException(
          "PieceDTO.mapComponentFrom() : A component cannot be pyramid.");
    }
    return new PieceDTO(
        piece.getId(),
        position,
        PieceShape.mapShape(piece.getType()),
        piece.getValue(),
        PlayerColorDTO.mapColor(piece.getPlayer().getColor()),
        List.of());
  }

  public static PieceDTO from(Piece piece, Position position) {
    return from(new PieceAtPosition(piece, position));
  }

  public static PieceDTO from(InvolvedPiece dto) {
    return from(dto.specificComponent(), dto.position());
  }

  public static PieceDTO from(Piece piece) {
    return from(piece, null);
  }
}
