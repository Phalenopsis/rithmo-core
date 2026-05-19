package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.BoardDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceShape;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PlayerOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.turn.option.TurnOption;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestDebugger {

    public static void print(List<? extends PlayerOptionDTO> list) {

        for (PlayerOptionDTO op : list) {
            System.out.println(op);
        }
    }

    public static void print(Set<DecisionDTO> decisions) {

        System.out.println("START TEST DEBUGGER");

        for (DecisionDTO decision : decisions) {
            System.out.println(decision);
        }

        System.out.println("END TEST DEBUGGER");
    }

    public static void printTurnOption(List<? extends TurnOption> list) {

        for (TurnOption op : list) {
            System.out.println(op);
        }
    }

    public static void print(Map<PieceDTO, Set<PlayerOptionDTO>> possibleOptions) {

        System.out.println("** PRINT POSSIBLES OPTIONS **");

        for (Map.Entry<PieceDTO, Set<PlayerOptionDTO>> entry : possibleOptions.entrySet()) {

            PieceDTO piece = entry.getKey();

            for (PlayerOptionDTO option : entry.getValue()) {
                System.out.println(
                        PieceRepresentationHelper.toRepresentation(piece)
                                + " : "
                                + option
                );
            }
        }

        System.out.println("**** END PRINT POSSIBLES OPTIONS ****");
    }

    public static void render(GameStatusDTO status) {

        BoardDTO board = status.board();

        int width = board.width();
        int height = board.height();

        String whitePyramid = "";
        String blackPyramid = "";

        String[][] grid = new String[height][width];

        // init grid
        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {
                grid[y][x] = "  .  ";
            }
        }

        // fill grid
        for (PieceDTO piece : board.pieces()) {

            int x = piece.position().getX();
            int y = piece.position().getY();

            String content =
                    PieceRepresentationHelper.toShortRepresentation(piece);

            grid[y][x] =
                    String.format("%-5s", content);

            if (piece.shape().equals(PieceShape.PYRAMID)) {

                if (piece.owner().equals(PlayerColorDTO.BLACK)) {

                    blackPyramid =
                            getPyramidDetailsStringRepresentation(piece);

                } else {

                    whitePyramid =
                            getPyramidDetailsStringRepresentation(piece);
                }
            }
        }

        // render
        System.out.println("\n--- PLATEAU DE RITHMOMACHIE ---");
        System.out.println("Active player : " + status.currentPlayer());
        System.out.println("Phase :" + status.phase());

        if (!whitePyramid.isBlank()) {
            System.out.println(whitePyramid);
        }

        if (!blackPyramid.isBlank()) {
            System.out.println(blackPyramid);
        }

        // x axis
        System.out.print("     ");

        for (int x = 0; x < width; x++) {
            System.out.printf("%-7d", x);
        }

        System.out.println();

        // board
        for (int y = 0; y < height; y++) {

            System.out.printf("%-4d", y);

            for (int x = 0; x < width; x++) {
                System.out.print("[" + grid[y][x] + "]");
            }

            System.out.println();
        }

        System.out.println("-------------------------------\n");
    }

    private static String getPyramidDetailsStringRepresentation(
            PieceDTO pieceDTO
    ) {

        StringBuilder sb = new StringBuilder(
                PieceRepresentationHelper.toShortRepresentation(pieceDTO)
        );

        sb.append(" :");

        for (PieceDTO component : pieceDTO.components()) {

            sb.append(" ");
            sb.append(
                    PieceRepresentationHelper.toShortRepresentation(component)
            );
        }

        return sb.toString();
    }

    public static void printCapturedPieces(
            GameStatusDTO gameStatusDTO,
            DecisionDTO decisionDTO
    ) {

        Set<String> capturedIds =
                decisionDTO.capturedIdList();

        for (PieceDTO pieceDTO : gameStatusDTO.board().pieces()) {

            if (capturedIds.contains(pieceDTO.id())) {

                System.out.println(
                        PieceRepresentationHelper.toRepresentation(pieceDTO)
                );
            }

            if (pieceDTO.shape().equals(PieceShape.PYRAMID)) {

                for (PieceDTO component : pieceDTO.components()) {

                    if (capturedIds.contains(component.id())) {

                        System.out.println(
                                "Component : "
                                        + PieceRepresentationHelper
                                        .toRepresentation(component)
                        );
                    }
                }
            }
        }
    }
}