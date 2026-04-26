package eu.nicosworld.rithmo.core.turn.testutils;

import eu.nicosworld.rithmo.core.turn.TurnProcessor;
import eu.nicosworld.rithmo.core.turn.TurnState;
import eu.nicosworld.rithmo.core.turn.applier.ActionApplier;
import eu.nicosworld.rithmo.core.turn.applier.CaptureApplier;
import eu.nicosworld.rithmo.core.turn.applier.MoveApplier;
import eu.nicosworld.rithmo.core.turn.option.PreCaptureOption;
import eu.nicosworld.rithmo.core.turn.option.TurnOption;
import eu.nicosworld.rithmo.core.turn.resolver.CaptureResolver;
import eu.nicosworld.rithmo.core.turn.resolver.MoveResolver;
import eu.nicosworld.rithmo.core.turn.resolver.PhaseResolver;
import eu.nicosworld.rithmo.core.turn.resolver.PreCaptureChoice;
import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.CaptureEngine;
import eu.nicosworld.rithmo.engine.capture.CaptureRule;
import eu.nicosworld.rithmo.engine.model.Position;
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

    public static PreCaptureChoice findPreCaptureChoice(List<TurnOption> options, Position attackerPos, List<Position> targetPositions) {
        return options.stream()
                .filter(PreCaptureOption.class::isInstance)
                .map(o -> ((PreCaptureOption) o).choice())
                .map(PreCaptureChoice.class::cast)
                .filter(c -> {
                    // On extrait toutes les positions cibles de ce choix
                    List<Position> targetsInChoice = c.actions().stream()
                            .map(CaptureAction::targetPosition)
                            .toList();

                    // On vérifie que l'attaquant est le bon ET que TOUTES les cibles attendues sont là
                    boolean sameAttacker = c.actions().stream()
                            .anyMatch(a -> a.attackerPosition().equals(attackerPos));

                    return sameAttacker && targetsInChoice.containsAll(targetPositions)
                            && targetsInChoice.size() == targetPositions.size();
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        String.format("Aucun choix trouvé pour l'attaquant en %s vers les cibles %s",
                                attackerPos, targetPositions)));
    }

    public static PreCaptureChoice findPreCaptureChoice(List<TurnOption> options, Position attackerPos, Position... targetPositions) {
        return findPreCaptureChoice(options, attackerPos, List.of(targetPositions));
    }

}
