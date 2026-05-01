package eu.nicosworld.rithmo.core;

import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.exception.logical.NoPhaseException;
import eu.nicosworld.rithmo.core.game.*;
import eu.nicosworld.rithmo.core.game.dto.option.*;
import eu.nicosworld.rithmo.core.persistence.GameRepository;
import eu.nicosworld.rithmo.core.persistence.OptionRepository;
import eu.nicosworld.rithmo.core.turn.TurnPhase;
import eu.nicosworld.rithmo.core.turn.TurnProcessor;
import eu.nicosworld.rithmo.core.turn.TurnState;
import eu.nicosworld.rithmo.core.turn.action.*;
import eu.nicosworld.rithmo.core.turn.applier.ActionApplier;
import eu.nicosworld.rithmo.core.turn.applier.CaptureApplier;
import eu.nicosworld.rithmo.core.turn.applier.MoveApplier;
import eu.nicosworld.rithmo.core.turn.option.*;
import eu.nicosworld.rithmo.core.turn.resolver.CaptureResolver;
import eu.nicosworld.rithmo.core.turn.resolver.MoveResolver;
import eu.nicosworld.rithmo.core.turn.resolver.PhaseResolver;
import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.CaptureEngine;
import eu.nicosworld.rithmo.engine.capture.CaptureRule;
import eu.nicosworld.rithmo.engine.capture.capturerule.AmbushRule;
import eu.nicosworld.rithmo.engine.capture.capturerule.AssaultRule;
import eu.nicosworld.rithmo.engine.capture.capturerule.EncounterRule;
import eu.nicosworld.rithmo.engine.capture.capturerule.PowerRule;
import eu.nicosworld.rithmo.engine.model.*;
import eu.nicosworld.rithmo.engine.move.FreePathMovementValidator;
import eu.nicosworld.rithmo.engine.move.MovementEngine;
import eu.nicosworld.rithmo.engine.move.RegularMoveGenerator;
import eu.nicosworld.rithmo.engine.victory.BodyVictoryRule;
import eu.nicosworld.rithmo.engine.victory.GoodsVictoryRule;
import eu.nicosworld.rithmo.engine.victory.VictoryEngine;
import eu.nicosworld.rithmo.engine.victory.VictoryRule;

import java.util.*;
import java.util.stream.Collectors;

public class GameFacade {

    private final GameRepository gameRepository;
    private final OptionRepository optionRepository;

    // Objets de logique partagés (Stateless)
    private final Map<CaptureRuleOption, CaptureRule> captureRegistry;

    private final MoveResolver moveResolver;
    private final ActionApplier actionApplier;

    public GameFacade(GameRepository gameRepository, OptionRepository optionRepository) {
        this.gameRepository = gameRepository;
        this.optionRepository = optionRepository;

        // On pré-instancie tout la mécanique une seule fois
        MovementEngine movementEngine = new MovementEngine();
        this.moveResolver = new MoveResolver(movementEngine);

        MoveApplier moveApplier = new MoveApplier();
        CaptureApplier captureApplier = new CaptureApplier();
        actionApplier = new ActionApplier(captureApplier, moveApplier);

        RegularMoveGenerator regularGenerator = new RegularMoveGenerator();
        FreePathMovementValidator pathValidator = new FreePathMovementValidator();

        // Remplissage du registre
        this.captureRegistry = Map.of(
                CaptureRuleOption.ENCOUNTER, new EncounterRule(regularGenerator, pathValidator),
                CaptureRuleOption.AMBUSH, new AmbushRule(regularGenerator, pathValidator),
                CaptureRuleOption.ASSAULT, new AssaultRule(regularGenerator, pathValidator),
                CaptureRuleOption.POWER, new PowerRule(regularGenerator, pathValidator)
        );
    }


    public GameStatusDTO startGame(GameOptions gameOptions, Board board) throws VictoryException, PatException, NoPhaseException {
        GameState gameState = GameState.initial(board, Player.BLACK);
        TurnState turnState = TurnState.of(gameState, TurnPhase.START);

        return play(new Game(gameOptions, turnState));
    }

    public GameStatusDTO startGame(Game game) throws VictoryException, PatException, NoPhaseException {
        return play(game);
    }

    private TurnProcessor createProcessorForGame(GameOptions options) {
        // On filtre la Map pour ne garder que les règles choisies pour cette partie
        List<CaptureRule> rules = options.captureRules().stream()
                .map(captureRegistry::get)
                .filter(Objects::nonNull)
                .toList();
        CaptureEngine captureEngine = new CaptureEngine(rules);
        CaptureResolver captureResolver = new CaptureResolver(captureEngine);
        PhaseResolver phaseResolver = new PhaseResolver(captureResolver, moveResolver);

        List<VictoryRule> victories = resolveVictoryRules(options.victoryRules());
        VictoryEngine victoryEngine = new VictoryEngine(victories);

        // On instancie le processeur avec la configuration spécifique
        // (Note: TurnProcessor est aussi stateless dans sa structure,
        // il ne fait que transformer un TurnState en un autre)
        return new TurnProcessor(actionApplier, phaseResolver, victoryEngine);
    }

    private List<VictoryRule> resolveVictoryRules(Map<VictoryRuleOption, Integer> options) {
        return options.entrySet().stream()
                .map(entry -> switch (entry.getKey()) {
                    case GOODS -> new GoodsVictoryRule(entry.getValue());
                    case BODY -> new BodyVictoryRule(entry.getValue());
                    // etc...
                })
                .toList();
    }

    private GameStatusDTO play(Game game) throws VictoryException, PatException, NoPhaseException {
        // 1. On crée le processeur adapté à cette partie
        TurnProcessor processor = createProcessorForGame(game.getOptions());

        // 2. On lance le premier tour (sans action utilisateur puisque c'est le START)
        // Le processeur va avancer jusqu'à la première phase de décision (ex: MOVE ou PRE_CAPTURE)
        TurnState nextState = processor.process(game.getCurrentState(), null);

        // 3. On met à jour le game et on délègue à une méthode commune pour la finalisation
        Game updatedGame = Game.from(game, nextState);
        return finalizeTurnUpdate(updatedGame);
    }

    public GameStatusDTO play(UUID gameId, UUID optionId) throws VictoryException, PatException, NoPhaseException {
        // 1. Récupération du contexte
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        // On récupère l'action technique associée à l'UUID choisi
        PendingAction pending = optionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Option expired or invalid: " + optionId));

        // 2. Exécution
        TurnProcessor processor = createProcessorForGame(game.getOptions());
        TurnState nextState = processor.process(game.getCurrentState(), pending.actionToExecute());

        // 3. Mise à jour et finalisation
        Game updatedGame = Game.from(game, nextState);
        return finalizeTurnUpdate(updatedGame);
    }

    private GameStatusDTO finalizeTurnUpdate(Game game) throws NoPhaseException {
        gameRepository.save(game);
        optionRepository.clearOptionsForGame(game.getId());

        List<PlayerOptionDTO> displayOptions = generateAndStorePendingActions(game.getId(), game.getCurrentState().options());

        return GameStatusDTO.from(game, displayOptions);
    }

    private List<PlayerOptionDTO> generateAndStorePendingActions(UUID gameId, List<TurnOption> options) {
        List<PlayerOptionDTO> displayOptions = new ArrayList<>();

        // 1. Traitement spécifique des pré-captures (Groupement)
        displayOptions.addAll(processPreCaptureOptions(gameId, options));

        // 2. Traitement des autres options (Unitaires)
        displayOptions.addAll(processUnitaryOptions(gameId, options));

        return displayOptions;
    }

    List<PlayerOptionDTO> processUnitaryOptions(UUID gameId, List<TurnOption> options) {
        return options.stream()
                .filter(opt -> !(opt instanceof PreCaptureOption))
                .map(option -> {
                    UUID id = UUID.randomUUID();
                    PlayerOptionDTO dto = createUnitaryDTO(id, option);

                    // Sauvegarde de l'action atomique associée
                    savePending(gameId, id, mapToTurnAction(option), dto);

                    return dto;
                })
                .toList();
    }

    private PlayerOptionDTO createUnitaryDTO(UUID id, TurnOption option) {
        return switch (option) {
            case MoveOption mo -> new MoveOptionDTO(id, mo.move().from(), mo.move().to(), mo.move().nature());
            case PostCaptureOption po -> new PostCaptureOptionDTO(
                    id,
                    po.captures().getFirst().attackerPosition(),
                    po.captures().stream().map(CaptureAction::targetPosition).toList()
            );
            case SkipPreCaptureOption ignored -> new SkipOptionDTO(id);
            case SkipPostCaptureOption ignored -> new SkipOptionDTO(id);
            default -> throw new IllegalStateException("Unexpected unitary option: " + option);
        };
    }

    private TurnAction mapToTurnAction(TurnOption option) {
        return switch (option) {
            case MoveOption moveOption -> MoveAction.from(moveOption);
            case PostCaptureOption postCaptureOption -> PostCaptureAction.from(postCaptureOption);
            case SkipPreCaptureOption skipPreCaptureOption -> SkipPreCaptureAction.from(skipPreCaptureOption);
            case SkipPostCaptureOption skipPostCaptureOption -> SkipPostCaptureAction.from(skipPostCaptureOption);
            default -> throw new IllegalStateException("Unexpected: " + option);
        };
    }

    List<PlayerOptionDTO> processPreCaptureOptions(UUID gameId, List<TurnOption> options) {
        List<PlayerOptionDTO> displayOptions = new ArrayList<>();
        // --- 1. PRE-CAPTURES (GROUPÉES) ---
        Map<List<Position>, List<PreCaptureOption>> groupedPreCaptures = options.stream()
                .filter(PreCaptureOption.class::isInstance)
                .map(PreCaptureOption.class::cast)
                .collect(Collectors.groupingBy(opt -> opt.actions().stream()
                        .map(CaptureAction::targetPosition)
                        .toList()));

        groupedPreCaptures.forEach((targets, opts) -> {
            Position attackerPos = opts.getFirst().actions().getFirst().attackerPosition();

            List<LandingChoiceDTO> landingChoices = opts.stream()
                    .map(opt -> {
                        UUID actionId = UUID.randomUUID();
                        // On sauve l'action atomique (sans DTO car c'est un sous-choix)
                        savePending(gameId, actionId, new PreCaptureAction(opt.actions(), opt.landing()), null);
                        return new LandingChoiceDTO(actionId, opt.landing());
                    })
                    .toList();

            PreCaptureOptionDTO groupDto = new PreCaptureOptionDTO(attackerPos, targets, landingChoices);
            displayOptions.add(groupDto);

            // On sauve quand même le DTO groupé en base pour le "Save Game"
            // (avec un ID bidon car on n'exécutera jamais cet ID directement)
            savePending(gameId, UUID.randomUUID(), null, groupDto);
        });
        return displayOptions;
    }

    private void savePending(UUID gameId, UUID id, TurnAction action, PlayerOptionDTO dto) {
        optionRepository.save(new PendingAction(id, gameId, action, dto));
    }
}
