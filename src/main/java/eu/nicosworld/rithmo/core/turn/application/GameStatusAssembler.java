package eu.nicosworld.rithmo.core.turn.application;

import eu.nicosworld.rithmo.core.UiInformation;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.*;
import eu.nicosworld.rithmo.core.persistence.OptionRepository;
import eu.nicosworld.rithmo.core.turn.action.*;
import eu.nicosworld.rithmo.core.turn.application.decision.DecisionRegistry;
import eu.nicosworld.rithmo.core.turn.application.presenter.TurnProjection;
import eu.nicosworld.rithmo.core.turn.application.projection.ExecutableDecision;
import eu.nicosworld.rithmo.core.turn.option.*;

import java.util.*;

public class GameStatusAssembler {

    private final OptionRepository optionRepository;

    public GameStatusAssembler(
            OptionRepository optionRepository
    ) {
        this.optionRepository = optionRepository;
    }

    public UiInformation assemble(Game game) {

        Map<PieceDTO, Set<PlayerOptionDTO>> playerOptionPerPiece =
                new HashMap<>();

        DecisionRegistry decisionRegistry =
                new DecisionRegistry(optionRepository);

        for (TurnOption option : game.getCurrentState().options()) {

            TurnProjection projection = presentOption(option);

            addOptions(
                    playerOptionPerPiece,
                    projection.piece(),
                    projection.options()
            );

            for (ExecutableDecision executable : projection.executableDecisions()) {

                decisionRegistry.register(
                        game.getId(),
                        executable.action(),
                        executable.decision()
                );
            }
        }

        return new UiInformation(
                playerOptionPerPiece,
                decisionRegistry.getDecisions()
        );
    }

    private TurnProjection presentOption(TurnOption option) {

        return switch (option) {

            case MoveOption moveOption ->
                    presentMoveOption(moveOption);

            case PostCaptureOption postCaptureOption ->
                    presentPostCaptureOption(postCaptureOption);

            case PreCaptureOption preCaptureOption ->
                    presentPreCaptureOption(preCaptureOption);

            case ReintroductionOption reintroductionOption ->
                    presentReintroductionOption(reintroductionOption);

            case SkipPreCaptureOption skipPreCaptureOption ->
                    presentSkipOption(skipPreCaptureOption);

            case SkipPostCaptureOption skipPostCaptureOption ->
                    presentSkipOption(skipPostCaptureOption);
        };
    }

    private TurnProjection presentMoveOption(MoveOption moveOption) {

        MoveAction action = MoveAction.from(moveOption);

        DecisionDTO decision =
                DecisionDTO.from(UUID.randomUUID(), action);

        MoveOptionDTO optionDTO =
                MoveOptionDTO.from(moveOption);

        ExecutableDecision executable =
                new ExecutableDecision(decision, action);

        return new TurnProjection(
                optionDTO.actor(),
                List.of(optionDTO),
                List.of(executable)
        );
    }

    private TurnProjection presentPostCaptureOption(
            PostCaptureOption postCaptureOption
    ) {

        PostCaptureAction action =
                PostCaptureAction.from(postCaptureOption);

        PieceDTO actorDTO =
                PieceDTO.from(postCaptureOption.actor());

        DecisionDTO rawDecision =
                DecisionDTO.from(
                        UUID.randomUUID(),
                        action
                );

        List<PlayerOptionDTO> optionDTOs =
                new ArrayList<>(
                        CaptureOptionDTO.from(postCaptureOption)
                );

        return new TurnProjection(
                actorDTO,
                optionDTOs,
                List.of(new ExecutableDecision(rawDecision, action))
        );
    }

    private TurnProjection presentPreCaptureOption(PreCaptureOption option) {

        List<PreCaptureAction> actions =
                PreCaptureAction.from(option);

        PieceDTO actorDTO =
                PieceDTO.from(option.actor());

        List<PlayerOptionDTO> optionDTOs =
                new ArrayList<>(PreCaptureOptionDTO.from(option));

        List<ExecutableDecision> executableDecisions =
                actions.stream()
                        .map(action -> {
                            DecisionDTO decision =
                                    DecisionDTO.from(UUID.randomUUID(), action);

                            return new ExecutableDecision(decision, action);
                        })
                        .toList();

        return new TurnProjection(
                actorDTO,
                optionDTOs,
                executableDecisions
        );
    }

    private TurnProjection presentReintroductionOption(
            ReintroductionOption reintroductionOption
    ) {

        ReintroductionAction action =
                ReintroductionAction.from(reintroductionOption);

        ReintroductionOptionDTO optionDTO =
                ReintroductionOptionDTO.from(reintroductionOption);

        DecisionDTO rawDecision =
                DecisionDTO.from(
                        UUID.randomUUID(),
                        action
                );

        return new TurnProjection(
                optionDTO.pieceDTO(),
                List.of(optionDTO),
                List.of(
                new ExecutableDecision(
                        rawDecision,
                        action
                )
        ));
    }

    private TurnProjection presentSkipOption(TurnOption option) {

        TurnAction action = mapToSkipTurnAction(option);

        DecisionDTO decision =
                DecisionDTO.skipFrom(UUID.randomUUID());

        return new TurnProjection(
                PieceDTO.GLOBAL_OPTION,
                List.of(new SkipOptionDTO()),
                List.of(new ExecutableDecision(decision, action))
        );
    }

    private void addOptions(
            Map<PieceDTO, Set<PlayerOptionDTO>> playerOptions,
            PieceDTO pieceDTO,
            List<PlayerOptionDTO> optionDTOs
    ) {

        playerOptions
                .computeIfAbsent(pieceDTO, k -> new HashSet<>())
                .addAll(optionDTOs);
    }

    /**
     * Maps a selection option
     * to its corresponding executable action.
     */
    private TurnAction mapToSkipTurnAction(
            TurnOption option
    ) {

        return switch (option) {
            case SkipPreCaptureOption skipPreCaptureOption ->
                    SkipPreCaptureAction.from(skipPreCaptureOption);

            case SkipPostCaptureOption skipPostCaptureOption ->
                    SkipPostCaptureAction.from(skipPostCaptureOption);

            default ->
                    throw new RuntimeException("Bad Turn Option");
        };
    }
}