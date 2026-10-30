package model.pieces;

import model.Board;
import model.PlayerColor;
import model.Position;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

    public Rook(PlayerColor color, Position position) {
        super(color, position);
    }

    @Override
    public String getSymbol() {
        return color == PlayerColor.WHITE ? "R" : "r";
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();

        int[][] directions = {
                {-1, 0}, // up
                {1, 0},  // down
                {0, -1}, // left
                {0, 1}   // right
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