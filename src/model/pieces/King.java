package model.pieces;

import logic.CheckDetector;
import model.Board;
import model.PlayerColor;
import model.Position;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    public King(PlayerColor color, Position position) {
        super(color, position);
    }

    @Override
    public String getSymbol() {
        return color == PlayerColor.WHITE ? "K" : "k";
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();

        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},           {0, 1},
                {1, -1},  {1, 0},  {1, 1}
        };

        int row = position.getRow();
        int col = position.getCol();

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (board.isInsideBoard(newRow, newCol)) {
                if (board.isEmpty(newRow, newCol) || board.hasEnemyPiece(newRow, newCol, color)) {
                    moves.add(new Position(newRow, newCol));
                }
            }
        }

        addCastlingMoves(board, moves);

        return moves;
    }

    private void addCastlingMoves(Board board, List<Position> moves) {
        if (hasMoved()) {
            return;
        }

        PlayerColor enemyColor = (color == PlayerColor.WHITE) ? PlayerColor.BLACK : PlayerColor.WHITE;

        if (CheckDetector.isSquareUnderAttack(board, position, enemyColor)) {
            return;
        }

        int row = position.getRow();
        int col = position.getCol();

        if (canCastleKingside(board, row, col, enemyColor)) {
            moves.add(new Position(row, col + 2));
        }

        if (canCastleQueenside(board, row, col, enemyColor)) {
            moves.add(new Position(row, col - 2));
        }
    }

    private boolean canCastleKingside(Board board, int row, int col, PlayerColor enemyColor) {
        Piece rook = board.getPiece(row, 7);

        if (!(rook instanceof Rook) || rook.getColor() != color || rook.hasMoved()) {
            return false;
        }

        if (!board.isEmpty(row, col + 1) || !board.isEmpty(row, col + 2)) {
            return false;
        }

        return !CheckDetector.isSquareUnderAttack(board, new Position(row, col + 1), enemyColor)
                && !CheckDetector.isSquareUnderAttack(board, new Position(row, col + 2), enemyColor);
    }

    private boolean canCastleQueenside(Board board, int row, int col, PlayerColor enemyColor) {
        Piece rook = board.getPiece(row, 0);

        if (!(rook instanceof Rook) || rook.getColor() != color || rook.hasMoved()) {
            return false;
        }

        if (!board.isEmpty(row, col - 1) || !board.isEmpty(row, col - 2) || !board.isEmpty(row, col - 3)) {
            return false;
        }

        return !CheckDetector.isSquareUnderAttack(board, new Position(row, col - 1), enemyColor)
                && !CheckDetector.isSquareUnderAttack(board, new Position(row, col - 2), enemyColor);
    }
}