package eu.nicosworld.rithmo.core.turn;

import eu.nicosworld.rithmo.core.GameOptions;
import eu.nicosworld.rithmo.core.game.CaptureRuleOption;
import eu.nicosworld.rithmo.core.game.VictoryRuleOption;
import eu.nicosworld.rithmo.core.turn.applier.ActionApplier;
import eu.nicosworld.rithmo.core.turn.applier.CaptureApplier;
import eu.nicosworld.rithmo.core.turn.applier.MoveApplier;
import eu.nicosworld.rithmo.core.turn.applier.ReintroductionApplier;
import eu.nicosworld.rithmo.core.turn.resolver.CaptureResolver;
import eu.nicosworld.rithmo.core.turn.resolver.MoveResolver;
import eu.nicosworld.rithmo.core.turn.resolver.PhaseResolver;
import eu.nicosworld.rithmo.core.turn.resolver.ReintroductionResolver;
import eu.nicosworld.rithmo.engine.capture.CaptureEngine;
import eu.nicosworld.rithmo.engine.capture.CaptureRule;
import eu.nicosworld.rithmo.engine.capture.capturerule.AmbushRule;
import eu.nicosworld.rithmo.engine.capture.capturerule.AssaultRule;
import eu.nicosworld.rithmo.engine.capture.capturerule.EncounterRule;
import eu.nicosworld.rithmo.engine.capture.capturerule.PowerRule;
import eu.nicosworld.rithmo.engine.move.FreePathMovementValidator;
import eu.nicosworld.rithmo.engine.move.MovementEngine;
import eu.nicosworld.rithmo.engine.move.RegularMoveGenerator;
import eu.nicosworld.rithmo.engine.reintroduction.ReintroductionEngine;
import eu.nicosworld.rithmo.engine.victory.BodyVictoryRule;
import eu.nicosworld.rithmo.engine.victory.GoodsVictoryRule;
import eu.nicosworld.rithmo.engine.victory.VictoryEngine;
import eu.nicosworld.rithmo.engine.victory.VictoryRule;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TurnProcessorFactory {

    private final MoveResolver moveResolver;
    private final ReintroductionResolver reintroductionResolver;
    private final ActionApplier actionApplier;

    private final Map<CaptureRuleOption, CaptureRule> captureRegistry;

    public TurnProcessorFactory() {

        MovementEngine movementEngine = new MovementEngine();
        this.moveResolver = new MoveResolver(movementEngine);

        ReintroductionEngine reintroductionEngine = new ReintroductionEngine();
        this.reintroductionResolver = new ReintroductionResolver(reintroductionEngine);

        MoveApplier moveApplier = new MoveApplier();
        CaptureApplier captureApplier = new CaptureApplier();
        ReintroductionApplier reintroductionApplier = new ReintroductionApplier();

        this.actionApplier =
                new ActionApplier(
                        captureApplier,
                        moveApplier,
                        reintroductionApplier
                );

        RegularMoveGenerator regularGenerator =
                new RegularMoveGenerator();
        FreePathMovementValidator pathValidator =
                new FreePathMovementValidator();

        this.captureRegistry = Map.of(
                CaptureRuleOption.ENCOUNTER,
                new EncounterRule(
                        regularGenerator,
                        pathValidator
                ),
                CaptureRuleOption.AMBUSH,
                new AmbushRule(
                        regularGenerator,
                        pathValidator
                ),
                CaptureRuleOption.ASSAULT,
                new AssaultRule(
                        regularGenerator,
                        pathValidator
                ),
                CaptureRuleOption.POWER,
                new PowerRule(
                        regularGenerator,
                        pathValidator
                )
        );
    }

    public TurnProcessor create(GameOptions options) {

        List<CaptureRule> rules =
                options.captureRules().stream()
                        .map(captureRegistry::get)
                        .filter(Objects::nonNull)
                        .toList();

        CaptureEngine captureEngine = new CaptureEngine(rules);
        CaptureResolver captureResolver = new CaptureResolver(captureEngine);
        PhaseResolver phaseResolver =
                new PhaseResolver(
                        captureResolver,
                        moveResolver,
                        reintroductionResolver
                );

        VictoryEngine victoryEngine =
                new VictoryEngine(
                        resolveVictoryRules(options.victoryRules())
                );

        return new TurnProcessor(
                actionApplier,
                phaseResolver,
                victoryEngine
        );
    }

    private List<VictoryRule> resolveVictoryRules(
            Map<VictoryRuleOption, Integer> options
    ) {
        return options.entrySet().stream()
                .map(entry -> switch (entry.getKey()) {
                    case GOODS -> new GoodsVictoryRule(entry.getValue());
                    case BODY ->  new BodyVictoryRule(entry.getValue());
                })
                .toList();
    }
}