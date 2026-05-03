package eu.nicosworld.rithmo.core.turn.testutils;

import eu.nicosworld.rithmo.core.turn.TurnProcessor;
import eu.nicosworld.rithmo.core.turn.TurnState;
import eu.nicosworld.rithmo.core.turn.applier.ActionApplier;
import eu.nicosworld.rithmo.core.turn.applier.CaptureApplier;
import eu.nicosworld.rithmo.core.turn.applier.MoveApplier;
import eu.nicosworld.rithmo.core.turn.option.MoveOption;
import eu.nicosworld.rithmo.core.turn.option.PostCaptureOption;
import eu.nicosworld.rithmo.core.turn.option.PreCaptureOption;
import eu.nicosworld.rithmo.core.turn.option.TurnOption;
import eu.nicosworld.rithmo.core.turn.resolver.CaptureResolver;
import eu.nicosworld.rithmo.core.turn.resolver.MoveResolver;
import eu.nicosworld.rithmo.core.turn.resolver.PhaseResolver;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.CaptureEngine;
import eu.nicosworld.rithmo.engine.capture.CaptureRule;
import eu.nicosworld.rithmo.engine.model.Position;
import eu.nicosworld.rithmo.engine.move.Move;
import eu.nicosworld.rithmo.engine.move.MovementEngine;
import eu.nicosworld.rithmo.engine.victory.BodyVictoryRule;
import eu.nicosworld.rithmo.engine.victory.VictoryEngine;
import eu.nicosworld.rithmo.engine.victory.VictoryRule;

import java.util.List;

public class TurnHelper {
    public static TurnProcessor setupProcessor(List<CaptureRule> captureRules) {
        BodyVictoryRule bodyVictoryRule = new BodyVictoryRule(1);

        return setupProcessor(captureRules, List.of(bodyVictoryRule));
    }

    public static TurnProcessor setupProcessor(List<CaptureRule> captureRules, List<VictoryRule> victoryRules) {
        CaptureApplier captureApplier = new CaptureApplier();
        MoveApplier moveApplier = new MoveApplier();
        ActionApplier actionApplier = new ActionApplier(captureApplier, moveApplier);

        CaptureEngine captureEngine = new CaptureEngine(captureRules);
        CaptureResolver captureResolver = new CaptureResolver(captureEngine);

        MovementEngine movementEngine = new MovementEngine();
        MoveResolver moveResolver = new MoveResolver(movementEngine);

        PhaseResolver phaseResolver = new PhaseResolver(captureResolver, moveResolver);

        VictoryEngine victoryEngine = new VictoryEngine(victoryRules);

        return new TurnProcessor(actionApplier,
                phaseResolver,
                victoryEngine);
    }

    public static void showOptions(TurnState turnState) {
        System.out.println(turnState.options());
    }

    public static PreCaptureOption findPreCaptureOption(
            List<TurnOption> options,
            Position attackerPos,
            Position landingPos,
            List<Position> targetPositions
    ) {
        return options.stream()
                .filter(PreCaptureOption.class::isInstance)
                .map(PreCaptureOption.class::cast)
                .filter(opt -> {

                    System.out.println(opt);
                    // 1. Check Landing
                    if (!opt.landing().equals(landingPos)) return false;

                    // 2. Extract targets from captures
                    List<Position> targetsInOption = opt.captures().stream()
                            .map(CaptureAction::targetPosition)
                            .toList();

                    // 3. Check Attacker (on the first action, they are all the same attacker)
                    boolean sameAttacker = opt.captures().stream()
                            .anyMatch(a -> a.actor().position().equals(attackerPos));

                    // 4. Compare target lists
                    return sameAttacker
                            && targetsInOption.size() == targetPositions.size()
                            && targetsInOption.containsAll(targetPositions);
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        String.format("No PreCaptureOption found for attacker at %s, landing at %s, targeting %s",
                                attackerPos, landingPos, targetPositions)));
    }

    /**
     * Convenience overload using varargs for targets.
     */
    public static PreCaptureOption findPreCaptureOption(
            List<TurnOption> options,
            Position attackerPos,
            Position landingPos,
            Position... targetPositions
    ) {
        return findPreCaptureOption(options, attackerPos, landingPos, List.of(targetPositions));
    }

    public static List<Move> getAllMoves(TurnState turnState, Position attackerPos) {
        return turnState.options().stream()
                .filter(MoveOption.class::isInstance)
                .map(o -> ((MoveOption) o).move())
                .filter(m -> m.from().equals(attackerPos))
                .toList();
    }

    public static Move getMove(TurnState turnState, Position attackerPos, Position landing) {
        return turnState.options().stream()
                .filter(MoveOption.class::isInstance)
                .map(o -> ((MoveOption) o).move())
                .filter(m -> m.from().equals(attackerPos) && m.to().equals(landing))
                .findFirst().orElseThrow(() -> new AssertionError(
                        String.format("Aucun choix de Move trouvé pour l'attaquant en %s vers la case %s",
                                attackerPos, landing)));
    }

    public static PostCaptureOption findPostCaptureOption(List<TurnOption> options, List<Position> targetPositions) {
        return options.stream()
                .filter(PostCaptureOption.class::isInstance)
                .map(PostCaptureOption.class::cast)
                .filter(option -> {
                    // On extrait les positions cibles de cette option post-capture
                    List<Position> targetsInOption = option.captures().stream()
                            .map(CaptureAction::targetPosition)
                            .toList();

                    // On vérifie que toutes les cibles attendues sont là et qu'il n'y en a pas d'autres
                    return targetsInOption.size() == targetPositions.size()
                            && targetsInOption.containsAll(targetPositions);
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        String.format("Aucune option de capture post-mouvement trouvée pour les cibles %s",
                                targetPositions)));
    }

    /**
     * Surcharge pratique pour passer les positions directement (varargs)
     */
    public static PostCaptureOption findPostCaptureOption(List<TurnOption> options, Position... targetPositions) {
        return findPostCaptureOption(options, List.of(targetPositions));
    }
}
