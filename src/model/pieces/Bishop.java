package model.pieces;

import model.Board;
import model.PlayerColor;
import model.Position;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {

    public Bishop(PlayerColor color, Position position) {
        super(color, position);
    }

    @Override
    public String getSymbol() {
        return color == PlayerColor.WHITE ? "B" : "b";
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();

        int[][] directions = {
                {-1, -1}, // up-left
                {-1, 1},  // up-right
                {1, -1},  // down-left
                {1, 1}    // down-right
        };

        int row = position.getRow();
        int col = position.getCol();

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            while (board.isInsideBoard(newRow, newCol)) {
                if (board.isEmpty(newRow, newCol)) {
                    moves.add(new Position(newRow, newCol));
                } else {
                    if (board.hasEnemyPiece(newRow, newCol, color)) {
                        moves.add(new Position(newRow, newCol));
                    }
                    break;
                }

                newRow += dir[0];
                newCol += dir[1];
            }
        }

        return moves;
    }
}