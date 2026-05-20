package eu.nicosworld.rithmo.core.turn.application;

import eu.nicosworld.rithmo.core.DecisionKey;
import eu.nicosworld.rithmo.core.UiInformation;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.PendingAction;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.*;
import eu.nicosworld.rithmo.core.persistence.OptionRepository;
import eu.nicosworld.rithmo.core.turn.action.*;
import eu.nicosworld.rithmo.core.turn.option.*;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.*;

public class GameStatusAssembler {

    private final OptionRepository optionRepository;

    public GameStatusAssembler(
            OptionRepository optionRepository
    ) {
        this.optionRepository = optionRepository;
    }

    public UiInformation assemble(Game game) {

        return generateActionsDecisionsAndDTOs(
                game,
                game.getCurrentState().options()
        );
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
