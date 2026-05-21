package eu.nicosworld.rithmo.core.turn.application;

import eu.nicosworld.rithmo.core.UiInformation;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.*;
import eu.nicosworld.rithmo.core.persistence.OptionRepository;
import eu.nicosworld.rithmo.core.turn.action.*;
import eu.nicosworld.rithmo.core.turn.application.decision.DecisionRegistry;
import eu.nicosworld.rithmo.core.turn.option.*;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
     * Orchestrates the transformation of internal options
     * into UI DTOs and executable decisions.
     */
    private UiInformation generateActionsDecisionsAndDTOs(
            Game game,
            List<TurnOption> options
    ) {

        Map<PieceDTO, Set<PlayerOptionDTO>> playerOptionPerPiece =
                new HashMap<>();

        DecisionRegistry decisionRegistry =
                new DecisionRegistry(optionRepository);

        for (TurnOption option : options) {

            if (option instanceof MoveOption moveOption) {

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

                decisionRegistry.register(
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

            else if (option instanceof PostCaptureOption postCaptureOption) {

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

                decisionRegistry.register(
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

            else if (option instanceof PreCaptureOption preCaptureOption) {

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

                    decisionRegistry.register(
                            game.getId(),
                            action,
                            rawDecision
                    );
                }
            }

            else if (option instanceof ReintroductionOption reintroductionOption) {

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

                decisionRegistry.register(
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

                decisionRegistry.register(
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
                decisionRegistry.getDecisions()
        );
    }

    private void addOption(
            Map<PieceDTO, Set<PlayerOptionDTO>> playerOptions,
            PieceDTO pieceDTO,
            PlayerOptionDTO playerOptionDTO
    ) {

        playerOptions
                .computeIfAbsent(pieceDTO, k -> new java.util.HashSet<>())
                .add(playerOptionDTO);
    }

    private void addOption(
            Map<PieceDTO, Set<PlayerOptionDTO>> playerOptions,
            PieceDTO pieceDTO,
            List<? extends PlayerOptionDTO> playerOptionDTOList
    ) {

        playerOptions
                .computeIfAbsent(pieceDTO, k -> new java.util.HashSet<>())
                .addAll(playerOptionDTOList);
    }

    /**
     * Maps a selection option
     * to its corresponding executable action.
     */
    private TurnAction mapToTurnAction(
            TurnOption option
    ) {

        return switch (option) {

            case MoveOption moveOption ->
                    MoveAction.from(moveOption);

            case PostCaptureOption postCaptureOption ->
                    PostCaptureAction.from(postCaptureOption);

            case SkipPreCaptureOption skipPreCaptureOption ->
                    SkipPreCaptureAction.from(skipPreCaptureOption);

            case SkipPostCaptureOption skipPostCaptureOption ->
                    SkipPostCaptureAction.from(skipPostCaptureOption);

            case ReintroductionOption reintroductionOption ->
                    ReintroductionAction.from(reintroductionOption);

            default ->
                    throw new RuntimeException("Bad Turn Option");
        };
    }
}