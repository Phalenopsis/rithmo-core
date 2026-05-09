package eu.nicosworld.rithmo.core;

import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.exception.logical.NoPhaseException;
import eu.nicosworld.rithmo.core.game.*;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
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

/**
 * Main entry point for the Rithmo core logic.
 * Orchestrates game state transitions, persistence, and option generation.
 */
public class GameFacade {

    private final GameRepository gameRepository;
    private final OptionRepository optionRepository;
    private final Map<CaptureRuleOption, CaptureRule> captureRegistry;
    private final MoveResolver moveResolver;
    private final ActionApplier actionApplier;

    /**
     * Constructs the facade and initializes stateless engine components.
     */
    public GameFacade(GameRepository gameRepository, OptionRepository optionRepository) {
        this.gameRepository = gameRepository;
        this.optionRepository = optionRepository;

        // Initialize reusable engine mechanics
        MovementEngine movementEngine = new MovementEngine();
        this.moveResolver = new MoveResolver(movementEngine);

        MoveApplier moveApplier = new MoveApplier();
        CaptureApplier captureApplier = new CaptureApplier();
        this.actionApplier = new ActionApplier(captureApplier, moveApplier);

        RegularMoveGenerator regularGenerator = new RegularMoveGenerator();
        FreePathMovementValidator pathValidator = new FreePathMovementValidator();

        // Populate the capture rule registry
        this.captureRegistry = Map.of(
                CaptureRuleOption.ENCOUNTER, new EncounterRule(regularGenerator, pathValidator),
                CaptureRuleOption.AMBUSH, new AmbushRule(regularGenerator, pathValidator),
                CaptureRuleOption.ASSAULT, new AssaultRule(regularGenerator, pathValidator),
                CaptureRuleOption.POWER, new PowerRule(regularGenerator, pathValidator)
        );
    }

    /**
     * Initializes and starts a new game with a specific board and options.
     *
     * @param gameOptions Chosen rules and victory conditions.
     * @param board       The initial board configuration.
     * @return Current game status and available options.
     * @throws VictoryException If initial state triggers a win.
     * @throws PatException     If initial state triggers a stalemate.
     */
    public GameStatusDTO startGame(GameOptions gameOptions, Board board) throws VictoryException, PatException {
        GameState gameState = GameState.initial(board, Player.BLACK);
        TurnState turnState = TurnState.of(gameState, TurnPhase.START);

        return play(new Game(gameOptions, turnState));
    }

    /**
     * Starts or resumes a game using a pre-constructed Game object.
     *
     * @param game The game instance.
     * @return Current game status and available options.
     */
    public GameStatusDTO startGame(Game game) throws VictoryException, PatException {
        return play(game);
    }

    /**
     * Configures a TurnProcessor tailored to specific game rules.
     */
    private TurnProcessor createProcessorForGame(GameOptions options) {
        // Filter the registry to keep only selected capture rules
        List<CaptureRule> rules = options.captureRules().stream()
                .map(captureRegistry::get)
                .filter(Objects::nonNull)
                .toList();

        CaptureEngine captureEngine = new CaptureEngine(rules);
        CaptureResolver captureResolver = new CaptureResolver(captureEngine);
        PhaseResolver phaseResolver = new PhaseResolver(captureResolver, moveResolver);

        List<VictoryRule> victories = resolveVictoryRules(options.victoryRules());
        VictoryEngine victoryEngine = new VictoryEngine(victories);

        return new TurnProcessor(actionApplier, phaseResolver, victoryEngine);
    }

    /**
     * Maps victory rule options to concrete engine rules.
     */
    private List<VictoryRule> resolveVictoryRules(Map<VictoryRuleOption, Integer> options) {
        return options.entrySet().stream()
                .map(entry -> switch (entry.getKey()) {
                    case GOODS -> new GoodsVictoryRule(entry.getValue());
                    case BODY -> new BodyVictoryRule(entry.getValue());
                })
                .toList();
    }

    /**
     * Internal execution of the first automatic phase transition.
     */
    private GameStatusDTO play(Game game) throws VictoryException, PatException {
        TurnProcessor processor = createProcessorForGame(game.getOptions());

        // Process initial START phase to reach the first decision point
        TurnState nextState = processor.process(game.getCurrentState());

        Game updatedGame = Game.from(game, nextState);
        return finalizeTurnUpdate(updatedGame);
    }

    /**
     * Processes a user action identified by an option ID.
     *
     * @param gameId   ID of the target game.
     * @param optionId ID of the selected option.
     * @return Updated game status.
     */
    public GameStatusDTO play(UUID gameId, UUID optionId) throws VictoryException, PatException {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        PendingAction pending = optionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Option expired or invalid: " + optionId));

        TurnProcessor processor = createProcessorForGame(game.getOptions());
        TurnState nextState = processor.process(game.getCurrentState(), pending.actionToExecute());

        Game updatedGame = Game.from(game, nextState);
        return finalizeTurnUpdate(updatedGame);
    }

    /**
     * Saves game state, clears old options, and generates new ones.
     */
    private GameStatusDTO finalizeTurnUpdate(Game game) throws NoPhaseException {
        gameRepository.save(game);
        optionRepository.clearOptionsForGame(game.getId());

        UiInformation uiInformation = generateActionsDecisionsAndDTOs(game, game.getCurrentState().options());

        return GameStatusDTO.from(game, uiInformation.playerOptionPerPiece(), uiInformation.possibleDecisions());
    }

    /**
     * Orchestrates the transformation of internal options into DTOs and persistent actions.
     */
    private UiInformation generateActionsDecisionsAndDTOs(Game game, List<TurnOption> options) {
        Map<PieceDTO, Set<PlayerOptionDTO>> playerOptionPerPiece = new HashMap<>();
        Map<DecisionDTO, UUID> possibleDecisions = new HashMap<>();
        for(TurnOption option: options) {
            if(option instanceof MoveOption moveOption) {
                MoveAction action = (MoveAction) mapToTurnAction(moveOption);
                Position actorPosition = moveOption.move().from();
                Piece actor = game.getCurrentState().state().board().getPieceAt(actorPosition);

                PieceDTO actorDTO = PieceDTO.from(actor, actorPosition);
                DecisionDTO decisionDTO = DecisionDTO.from(game.getCurrentState().state().board(), action);

                UUID actionId = UUID.randomUUID();
                possibleDecisions.put(decisionDTO, actionId);
                savePending(game.getId(), actionId, action);
 
                MoveOptionDTO playerOptionDTO = MoveOptionDTO.from(moveOption);
                addOption(playerOptionPerPiece, actorDTO, playerOptionDTO);
            }
            else if(option instanceof PostCaptureOption postCaptureOption) {
                PostCaptureAction action = (PostCaptureAction) mapToTurnAction(postCaptureOption);
                Position actorPosition = postCaptureOption.captures().getFirst().actor().position();
                Piece actor = postCaptureOption.captures().getFirst().actor().specificComponent();

                PieceDTO actorDTO = PieceDTO.from(actor, actorPosition);
                DecisionDTO decisionDTO = DecisionDTO.from(action);

                UUID actionId = UUID.randomUUID();
                possibleDecisions.put(decisionDTO, actionId);
                savePending(game.getId(), actionId, action);

                List<CaptureOptionDTO> playerOptionDTOs = CaptureOptionDTO.from(postCaptureOption);
                addOption(playerOptionPerPiece, actorDTO, playerOptionDTOs);
            }
            else if(option instanceof PreCaptureOption preCaptureOption) {
                PreCaptureAction action = (PreCaptureAction)  mapToTurnAction(preCaptureOption);
                System.out.println(action);
                Position actorPosition = preCaptureOption.captures().getFirst().actor().position();
                Piece actor = preCaptureOption.captures().getFirst().actor().specificComponent();

                PieceDTO actorDTO = PieceDTO.from(actor, actorPosition);
                DecisionDTO decisionDTO = DecisionDTO.from(action);

                UUID actionId = UUID.randomUUID();
                possibleDecisions.put(decisionDTO, actionId);
                savePending(game.getId(), actionId, action);

                List<PreCaptureOptionDTO> playerOptionDTOs = PreCaptureOptionDTO.from(preCaptureOption);
                addOption(playerOptionPerPiece, actorDTO, playerOptionDTOs);
            } else {
                TurnAction action = mapToTurnAction(option);
                DecisionDTO decisionDTO = DecisionDTO.skipFrom();

                UUID actionId = UUID.randomUUID();
                possibleDecisions.put(decisionDTO, actionId);
                savePending(game.getId(), actionId, action);

                SkipOptionDTO playerOptionDTO = new SkipOptionDTO();

                addOption(playerOptionPerPiece, PieceDTO.GLOBAL_OPTION, playerOptionDTO);
            }
        }

        return new UiInformation(playerOptionPerPiece, possibleDecisions);
    }

    private void addOption(Map<PieceDTO, Set<PlayerOptionDTO>> playerOptions,
                           PieceDTO pieceDTO,
                           PlayerOptionDTO playerOptionDTO) {
        playerOptions
                .computeIfAbsent(pieceDTO, k -> new HashSet<>())
                .add(playerOptionDTO);
    }

    private void addOption(Map<PieceDTO, Set<PlayerOptionDTO>> playerOptions,
                           PieceDTO pieceDTO,
                           List<? extends PlayerOptionDTO> playerOptionDTOList) {
        playerOptions
                .computeIfAbsent(pieceDTO, k -> new HashSet<>())
                .addAll(playerOptionDTOList);
    }

    /**
     * Maps a selection option to its corresponding executable action.
     */
    private TurnAction mapToTurnAction(TurnOption option) {
        return switch (option) {
            case MoveOption moveOption -> MoveAction.from(moveOption);
            case PostCaptureOption postCaptureOption -> PostCaptureAction.from(postCaptureOption);
            case SkipPreCaptureOption skipPreCaptureOption -> SkipPreCaptureAction.from(skipPreCaptureOption);
            case SkipPostCaptureOption skipPostCaptureOption -> SkipPostCaptureAction.from(skipPostCaptureOption);
            case PreCaptureOption preCaptureOption -> PreCaptureAction.from(preCaptureOption);
        };
    }

    /**
     * Persists an available action to the repository.
     */
    private void savePending(UUID gameId, UUID id, TurnAction action) {
        optionRepository.save(new PendingAction(id, gameId, action));
    }
}