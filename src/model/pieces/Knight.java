package model.pieces;

import model.Board;
import model.Position;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {

    public Knight(model.PlayerColor color, Position position) {
        super(color, position);
    }

    @Override
    public String getSymbol() {
        return color == model.PlayerColor.WHITE ? "N" : "n";
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();

        int[][] offsets = {
                {-2, -1}, {-2, 1},
                {-1, -2}, {-1, 2},
                {1, -2}, {1, 2},
                {2, -1}, {2, 1}
        };

        int row = position.getRow();
        int col = position.getCol();

        for (int[] offset : offsets) {
            int newRow = row + offset[0];
            int newCol = col + offset[1];

            if (board.isInsideBoard(newRow, newCol)) {
                if (board.isEmpty(newRow, newCol) || board.hasEnemyPiece(newRow, newCol, color)) {
                    moves.add(new Position(newRow, newCol));
                }
            }
        }

        return moves;
    }
}