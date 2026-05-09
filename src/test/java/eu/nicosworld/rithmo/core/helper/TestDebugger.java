package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.BoardDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PlayerOptionDTO;
import eu.nicosworld.rithmo.core.turn.option.TurnOption;
import eu.nicosworld.rithmo.engine.model.PlayerColor;

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
        for (DecisionDTO decision: decisions) {
            System.out.println(decision);
        }
    }

    public static void printTurnOption(List<? extends TurnOption> list) {
        for (TurnOption op : list) {
            System.out.println(op);
        }
    }

    public static void print(Map<PieceDTO, Set<PlayerOptionDTO>> possibleOptions) {
        for (Map.Entry<PieceDTO, Set<PlayerOptionDTO>> entry : possibleOptions.entrySet()) {
            PieceDTO piece = entry.getKey();

            for (PlayerOptionDTO option : entry.getValue()) {
                System.out.println(piece + " : " + option);
            }
        }
    }

    public static void render(GameStatusDTO status) {
        BoardDTO board = status.board();
        int width = board.width();
        int height = board.height();


        // 1. Initialisation de la grille avec des chaînes de taille fixe (5 espaces)
        String[][] grid = new String[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = "  .  ";
            }
        }

        // 2. Remplissage
        for (PieceDTO piece : board.pieces()) {
            int x = piece.position().getX();
            int y = piece.position().getY();

            String colorChar = piece.owner() == PlayerColor.WHITE ? "W" : "B";
            String shapeChar = piece.shape().name().substring(0, 1);

            // On construit une chaîne de type "BT4" ou "WC12"
            String content = colorChar + shapeChar + piece.value();

            // On centre ou on aligne à gauche dans un bloc de 5 pour garder la grille droite
            grid[y][x] = String.format("%-5s", content);
        }

        // 3. Affichage
        System.out.println("\n--- PLATEAU DE RITHMOMACHIE ---");
        System.out.println("Active player : " + status.currentPlayer());

        // Header des colonnes (bien aligné avec le décalage de l'index ligne)
        System.out.print("     "); // Espace pour l'index Y
        for (int x = 0; x < width; x++) {
            System.out.printf("%-7d", x); // 7 car 5 (cellule) + 2 (crochets)
        }
        System.out.println();

        for (int y = 0; y < height; y++) {
            System.out.printf("%-4d", y); // Index ligne
            for (int x = 0; x < width; x++) {
                System.out.print("[" + grid[y][x] + "]");
            }
            System.out.println();
        }
        System.out.println("-------------------------------\n");
    }
}
