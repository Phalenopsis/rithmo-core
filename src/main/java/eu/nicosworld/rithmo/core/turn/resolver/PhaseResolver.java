package eu.nicosworld.rithmo.core.turn.resolver;

import eu.nicosworld.rithmo.core.turn.option.*;
import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.model.PieceAtPosition;
import eu.nicosworld.rithmo.engine.model.Position;
import eu.nicosworld.rithmo.engine.move.Move;
import eu.nicosworld.rithmo.engine.reintroduction.Reintroduction;

import java.util.ArrayList;
import java.util.List;

/**
 * High-level coordinator responsible for determining the available {@link TurnOption}s
 * based on the current {@link eu.nicosworld.rithmo.core.turn.TurnPhase}.
 * <p>
 * It leverages specialized resolvers for movement and capture logic to build
 * the list of choices presented to the player.
 */
public class PhaseResolver {

    private final CaptureResolver captureResolver;
    private final MoveResolver movementResolver;
    private final ReintroductionResolver reintroductionResolver;

    public PhaseResolver(
            CaptureResolver captureResolver,
            MoveResolver movementResolver,
            ReintroductionResolver reintroductionResolver
    ) {
        this.captureResolver = captureResolver;
        this.movementResolver = movementResolver;
        this.reintroductionResolver = reintroductionResolver;
    }

    /**
     * Resolves options for the pre-movement capture phase.
     * If captures are available, a {@link SkipPreCaptureOption} is automatically added.
     *
     * @param state The current game state.
     * @return A list of available {@link TurnOption}s (PreCapture or Skip).
     */
    public List<TurnOption> resolvePreCapture(GameState state) {
        List<PreCaptureOption> choices =
                captureResolver.resolvePreCaptures(state);

        List<TurnOption> options = new ArrayList<>(choices);

        if (!options.isEmpty()) {
            options.add(new SkipPreCaptureOption());
        }

        return options;
    }

    /**
     * Resolves all possible movement options for the current player.
     *
     * @param state The current game state.
     * @return A list of {@link MoveOption}s.
     */
    public List<TurnOption> resolveMove(GameState state) {
        List<Move> moves = movementResolver.resolveMove(state);

        List<TurnOption> options = new ArrayList<>();
        for (Move move : moves) {
            Piece piece = state.board().getPieceAt(move.from());
            options.add(MoveOption.from(piece, move));
        }

        List<Reintroduction> reintroductions = reintroductionResolver.resolveReintroductions(state);
        for (Reintroduction reintroduction: reintroductions) {
            options.add(new  ReintroductionOption(reintroduction));
        }

        return options;
    }

    /**
     * Resolves movement options restricted to a specific piece.
     * Useful for multi-step movements or forced actions.
     *
     * @param state The current game state.
     * @param pap   The specific piece (Piece At Position) allowed to move.
     * @return A list of {@link MoveOption}s for the specified piece.
     */
    public List<TurnOption> resolveMove(GameState state, PieceAtPosition pap) {
        List<Move> moves = movementResolver.resolveMove(state, pap);

        List<TurnOption> options = new ArrayList<>();
        for (Move move : moves) {
            options.add(MoveOption.from(pap.piece(), move));
        }

        return options;
    }

    /**
     * Resolves options for the capture phase occurring after a move.
     * The capture logic is restricted to the piece that just moved.
     *
     * @param state    The current game state.
     * @param actorPos The position of the piece that performed the movement.
     * @return A list of available {@link TurnOption}s (PostCapture or Skip).
     */
    public List<TurnOption> resolvePostCapture(GameState state, Position actorPos) {
        List<PostCaptureOption> choices =
                captureResolver.resolvePostCaptures(state, actorPos);

        List<TurnOption> options = new ArrayList<>(choices);

        if (!options.isEmpty()) {
            options.add(new SkipPostCaptureOption());
        }

        return options;
    }
}