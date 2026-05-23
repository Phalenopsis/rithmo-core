package eu.nicosworld.rithmo.core.turn.application;

import eu.nicosworld.rithmo.core.UiInformation;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.*;
import eu.nicosworld.rithmo.core.persistence.OptionRepository;
import eu.nicosworld.rithmo.core.turn.action.*;
import eu.nicosworld.rithmo.core.turn.application.decision.DecisionRegistry;
import eu.nicosworld.rithmo.core.turn.application.presenter.PresentationResult;
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

        Map<PieceDTO, Set<PlayerOptionDTO>> playerOptionPerPiece =
                new HashMap<>();

        DecisionRegistry decisionRegistry =
                new DecisionRegistry(optionRepository);

        for (TurnOption option : game.getCurrentState().options()) {

            PresentationResult result =
                    presentOption(option);

            addOptions(
                    playerOptionPerPiece,
                    result.piece(),
                    result.options()
            );

            for (int i = 0; i < result.actions().size(); i++) {

                decisionRegistry.register(
                        game.getId(),
                        result.actions().get(i),
                        result.decisions().get(i)
                );
            }
        }

        return new UiInformation(
                playerOptionPerPiece,
                decisionRegistry.getDecisions()
        );
    }

    private PresentationResult presentOption(TurnOption option) {

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

    private PresentationResult presentMoveOption(MoveOption moveOption) {

        MoveAction action =
                MoveAction.from(moveOption);

        DecisionDTO rawDecision =
                DecisionDTO.from(
                        UUID.randomUUID(),
                        action
                );

        MoveOptionDTO optionDTO =
                MoveOptionDTO.from(moveOption);

        return new PresentationResult(
                optionDTO.actor(),
                List.of(optionDTO),
                List.of(action),
                List.of(rawDecision)
        );
    }

    private PresentationResult presentPostCaptureOption(
            PostCaptureOption postCaptureOption
    ) {

        PostCaptureAction action =
                PostCaptureAction.from(postCaptureOption);

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

        List<PlayerOptionDTO> optionDTOs =
                new ArrayList<>(
                        CaptureOptionDTO.from(postCaptureOption)
                );

        return new PresentationResult(
                actorDTO,
                optionDTOs,
                List.of(action),
                List.of(rawDecision)
        );
    }

    private PresentationResult presentPreCaptureOption(
            PreCaptureOption preCaptureOption
    ) {

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

        List<PlayerOptionDTO> optionDTOs =
                new ArrayList<>(
                        PreCaptureOptionDTO.from(preCaptureOption)
                );

        List<DecisionDTO> decisions =
                actions.stream()
                        .map(action ->
                                DecisionDTO.from(
                                        UUID.randomUUID(),
                                        action
                                )
                        )
                        .toList();

        return new PresentationResult(
                actorDTO,
                optionDTOs,
                new ArrayList<>(actions),
                decisions
        );
    }

    private PresentationResult presentReintroductionOption(
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

        return new PresentationResult(
                optionDTO.pieceDTO(),
                List.of(optionDTO),
                List.of(action),
                List.of(rawDecision)
        );
    }

    private PresentationResult presentSkipOption(
            TurnOption option
    ) {

        TurnAction action =
                mapToTurnAction(option);

        DecisionDTO rawDecision =
                DecisionDTO.skipFrom(UUID.randomUUID());

        return new PresentationResult(
                PieceDTO.GLOBAL_OPTION,
                List.of(new SkipOptionDTO()),
                List.of(action),
                List.of(rawDecision)
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