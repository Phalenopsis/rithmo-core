package eu.nicosworld.rithmo.core.turn.resolver;

import eu.nicosworld.rithmo.core.turn.TurnPhase;
import eu.nicosworld.rithmo.core.turn.option.*;
import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.PieceAtPosition;
import eu.nicosworld.rithmo.engine.model.Player;
import eu.nicosworld.rithmo.engine.model.Position;
import eu.nicosworld.rithmo.engine.move.Move;

import java.util.ArrayList;
import java.util.List;

public class PhaseResolver {

    private final CaptureResolver captureResolver;
    private final MoveResolver movementResolver;

    public PhaseResolver(
            CaptureResolver captureResolver,
            MoveResolver movementResolver
    ) {
        this.captureResolver = captureResolver;
        this.movementResolver = movementResolver;
    }

    // =========================
    // PRE CAPTURE
    // =========================
    public List<TurnOption> resolvePreCapture(GameState state) {

        List<PreCaptureChoice> choices =
                captureResolver.resolvePreCaptures(state);

        List<TurnOption> options = new ArrayList<>();

        for (PreCaptureChoice choice : choices) {
            options.add(new PreCaptureOption(choice));
        }

        if (!options.isEmpty()) {
            options.add(new SkipPreCaptureOption());
        }

        return options;
    }

    // =========================
    // MOVE
    // =========================
    public List<TurnOption> resolveMove(GameState state) {

        List<Move> moves = movementResolver.resolveMove(state);

        List<TurnOption> options = new ArrayList<>();

        for (Move move : moves) {
            options.add(new MoveOption(move));
        }

        return options;
    }
    // =========================
    // MOVE
    // =========================
    public List<TurnOption> resolveMove(GameState state, PieceAtPosition pap) {

        List<Move> moves = movementResolver.resolveMove(state, pap);

        List<TurnOption> options = new ArrayList<>();

        for (Move move : moves) {
            options.add(new MoveOption(move));
        }

        return options;
    }

    // =========================
    // POST CAPTURE
    // =========================
    public List<TurnOption> resolvePostCapture(GameState state, Position attackerPos) {

        List<PostCaptureChoice> choices =
                captureResolver.resolvePostCaptures(state, attackerPos);

        List<TurnOption> options = new ArrayList<>();

        for (PostCaptureChoice choice : choices) {
            options.add(new PostCaptureOption(choice.actions()));
        }

        if (!options.isEmpty()) {
            options.add(new SkipPostCaptureOption());
        }

        return options;
    }


}