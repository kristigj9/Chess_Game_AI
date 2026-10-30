package model.pieces;

import model.Board;
import model.PlayerColor;
import model.Position;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    public Pawn(PlayerColor color, Position position) {
        super(color, position);
    }

    @Override
    public String getSymbol() {
        return color == PlayerColor.WHITE ? "P" : "p";
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();

        int direction = (color == PlayerColor.WHITE) ? -1 : 1;
        int startRow = (color == PlayerColor.WHITE) ? 6 : 1;

        int row = position.getRow();
        int col = position.getCol();

        int nextRow = row + direction;

        // One step forward
        if (board.isInsideBoard(nextRow, col) && board.isEmpty(nextRow, col)) {
            moves.add(new Position(nextRow, col));

            // Two steps forward from initial position
            int twoStepsRow = row + 2 * direction;
            if (row == startRow && board.isInsideBoard(twoStepsRow, col) && board.isEmpty(twoStepsRow, col)) {
                moves.add(new Position(twoStepsRow, col));
            }
        }

        // Capture diagonally left
        int leftCol = col - 1;
        if (board.isInsideBoard(nextRow, leftCol) && board.hasEnemyPiece(nextRow, leftCol, color)) {
            moves.add(new Position(nextRow, leftCol));
        }

        // Capture diagonally right
        int rightCol = col + 1;
        if (board.isInsideBoard(nextRow, rightCol) && board.hasEnemyPiece(nextRow, rightCol, color)) {
            moves.add(new Position(nextRow, rightCol));
        }

        return moves;
    }
}