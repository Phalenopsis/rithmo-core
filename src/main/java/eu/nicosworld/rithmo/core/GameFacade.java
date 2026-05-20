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
import eu.nicosworld.rithmo.core.turn.TurnProcessorFactory;
import eu.nicosworld.rithmo.core.turn.TurnState;
import eu.nicosworld.rithmo.core.turn.action.*;
import eu.nicosworld.rithmo.core.turn.option.*;
import eu.nicosworld.rithmo.engine.model.*;


import java.util.*;

/**
 * Main entry point for the Rithmo core logic.
 * Orchestrates game state transitions, persistence, and option generation.
 */
public class GameFacade {

    private final GameRepository gameRepository;
    private final OptionRepository optionRepository;
    private final TurnProcessorFactory turnProcessorFactory;

    /**
     * Constructs the facade and initializes stateless engine components.
     */
    public GameFacade(
            GameRepository gameRepository,
            OptionRepository optionRepository
    ) {

        this.gameRepository = gameRepository;
        this.optionRepository = optionRepository;

        this.turnProcessorFactory = new TurnProcessorFactory();
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
     * Internal execution of the first automatic phase transition.
     */
    private GameStatusDTO play(Game game) throws VictoryException, PatException {
        TurnProcessor processor = turnProcessorFactory.create(game.getOptions());

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

        TurnProcessor processor = turnProcessorFactory.create(game.getOptions());
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
    private UiInformation generateActionsDecisionsAndDTOs(
            Game game,
            List<TurnOption> options
    ) {

        Map<PieceDTO, Set<PlayerOptionDTO>> playerOptionPerPiece =
                new HashMap<>();

        Set<DecisionDTO> possibleDecisions =
                new HashSet<>();

        /**
         * Permet de dédupliquer les décisions UI.
         *
         * Plusieurs actions moteur peuvent pointer
         * vers une même décision utilisateur.
         */
        Map<DecisionKey, UUID> existingDecisionIds =
                new HashMap<>();

        for (TurnOption option : options) {

            if(option instanceof MoveOption moveOption) {

                MoveAction action =
                        (MoveAction) mapToTurnAction(moveOption);

                Position actorPosition =
                        moveOption.move().from();

                Piece actor =
                        game.getCurrentState()
                                .state()
                                .board()
                                .getPieceAt(actorPosition);

                PieceDTO actorDTO =
                        PieceDTO.from(actor, actorPosition);

                DecisionDTO rawDecision =
                        DecisionDTO.from(
                                UUID.randomUUID(),
                                game.getCurrentState().state().board(),
                                action
                        );

                registerDecision(
                        possibleDecisions,
                        existingDecisionIds,
                        game.getId(),
                        action,
                        rawDecision
                );

                MoveOptionDTO playerOptionDTO =
                        MoveOptionDTO.from(moveOption);

                addOption(
                        playerOptionPerPiece,
                        actorDTO,
                        playerOptionDTO
                );
            }

            else if(option instanceof PostCaptureOption postCaptureOption) {

                PostCaptureAction action =
                        (PostCaptureAction) mapToTurnAction(postCaptureOption);

                Position actorPosition =
                        postCaptureOption.captures()
                                .getFirst()
                                .actor()
                                .position();

                Piece actor =
                        postCaptureOption.captures()
                                .getFirst()
                                .actor()
                                .specificComponent();

                PieceDTO actorDTO =
                        PieceDTO.from(actor, actorPosition);

                DecisionDTO rawDecision =
                        DecisionDTO.from(
                                UUID.randomUUID(),
                                action
                        );

                registerDecision(
                        possibleDecisions,
                        existingDecisionIds,
                        game.getId(),
                        action,
                        rawDecision
                );

                List<CaptureOptionDTO> playerOptionDTOs =
                        CaptureOptionDTO.from(postCaptureOption);

                addOption(
                        playerOptionPerPiece,
                        actorDTO,
                        playerOptionDTOs
                );
            }

            else if(option instanceof PreCaptureOption preCaptureOption) {

                List<PreCaptureAction> actions =
                        PreCaptureAction.from(preCaptureOption);

                Position actorPosition =
                        preCaptureOption.captures()
                                .getFirst()
                                .actor()
                                .position();

                Piece actor =
                        preCaptureOption.captures()
                                .getFirst()
                                .actor()
                                .specificComponent();

                PieceDTO actorDTO =
                        PieceDTO.from(actor, actorPosition);

                List<PreCaptureOptionDTO> playerOptionDTOs =
                        PreCaptureOptionDTO.from(preCaptureOption);

                addOption(
                        playerOptionPerPiece,
                        actorDTO,
                        playerOptionDTOs
                );

                for (PreCaptureAction action : actions) {

                    DecisionDTO rawDecision =
                            DecisionDTO.from(
                                    UUID.randomUUID(),
                                    action
                            );

                    registerDecision(
                            possibleDecisions,
                            existingDecisionIds,
                            game.getId(),
                            action,
                            rawDecision
                    );
                }
            }

            else if(option instanceof ReintroductionOption reintroductionOption) {

                ReintroductionAction action =
                        (ReintroductionAction) mapToTurnAction(option);

                ReintroductionOptionDTO playerOptionDTO =
                        ReintroductionOptionDTO.from(reintroductionOption);

                PieceDTO actorDTO =
                        playerOptionDTO.pieceDTO();

                DecisionDTO rawDecision =
                        DecisionDTO.from(
                                UUID.randomUUID(),
                                action
                        );

                registerDecision(
                        possibleDecisions,
                        existingDecisionIds,
                        game.getId(),
                        action,
                        rawDecision
                );

                addOption(
                        playerOptionPerPiece,
                        actorDTO,
                        playerOptionDTO
                );
            }

            else {

                TurnAction action =
                        mapToTurnAction(option);

                DecisionDTO rawDecision =
                        DecisionDTO.skipFrom(UUID.randomUUID());

                registerDecision(
                        possibleDecisions,
                        existingDecisionIds,
                        game.getId(),
                        action,
                        rawDecision
                );

                SkipOptionDTO playerOptionDTO =
                        new SkipOptionDTO();

                addOption(
                        playerOptionPerPiece,
                        PieceDTO.GLOBAL_OPTION,
                        playerOptionDTO
                );
            }
        }

        return new UiInformation(
                playerOptionPerPiece,
                possibleDecisions
        );
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
            case ReintroductionOption reintroductionOption -> ReintroductionAction.from(reintroductionOption);
            default -> throw new RuntimeException("Bad Turn Option");
        };
    }

    /**
     * Persists an available action to the repository.
     */
    private void savePending(UUID gameId, UUID id, TurnAction action) {
        optionRepository.save(new PendingAction(id, gameId, action));
    }

    private void registerDecision(
            Set<DecisionDTO> possibleDecisions,
            Map<DecisionKey, UUID> existingDecisionIds,
            UUID gameId,
            TurnAction action,
            DecisionDTO rawDecision
    ) {

        DecisionKey key =
                buildDecisionKey(rawDecision);

        UUID decisionId =
                existingDecisionIds.computeIfAbsent(
                        key,
                        k -> UUID.randomUUID()
                );

        boolean alreadyExists =
                possibleDecisions.stream()
                        .anyMatch(d ->
                                d.id().equals(decisionId)
                        );

        if(!alreadyExists) {

            DecisionDTO finalDecision;

            if(rawDecision.skip()) {

                finalDecision =
                        DecisionDTO.skipFrom(decisionId);

            } else {

                finalDecision = new DecisionDTO(
                        decisionId,
                        rawDecision.actorId(),
                        rawDecision.capturedIdList(),
                        rawDecision.landing(),
                        false
                );
            }

            possibleDecisions.add(finalDecision);
        }

        /**
         * IMPORTANT :
         * plusieurs actions moteur
         * peuvent partager
         * le même decisionId
         */
        savePending(gameId, decisionId, action);
    }

    private DecisionKey buildDecisionKey(
            DecisionDTO dto
    ) {

        return new DecisionKey(
                dto.actorId(),
                dto.capturedIdList(),
                dto.landing(),
                dto.skip()
        );
    }
}