package logic;

import model.Board;
import model.PlayerColor;
import model.Position;
import model.pieces.King;
import model.pieces.Piece;

import java.util.List;

public class CheckDetector {

    public static boolean isKingInCheck(Board board, PlayerColor kingColor) {
        Position kingPosition = findKingPosition(board, kingColor);

        if (kingPosition == null) {
            return false;
        }

        return isSquareUnderAttack(board, kingPosition, oppositeColor(kingColor));
    }

    public static boolean isSquareUnderAttack(Board board, Position target, PlayerColor attackerColor) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);

                if (piece != null && piece.getColor() == attackerColor) {
                    if (piece instanceof King) {
                        if (kingAttacksSquare(piece.getPosition(), target)) {
                            return true;
                        }
                    } else {
                        List<Position> moves = piece.getPossibleMoves(board);
                        for (Position move : moves) {
                            if (move.equals(target)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private static boolean kingAttacksSquare(Position kingPos, Position target) {
        int rowDiff = Math.abs(kingPos.getRow() - target.getRow());
        int colDiff = Math.abs(kingPos.getCol() - target.getCol());
        return rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff > 0);
    }

    private static Position findKingPosition(Board board, PlayerColor color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);

                if (piece instanceof King && piece.getColor() == color) {
                    return piece.getPosition();
                }
            }
        }

        return null;
    }

    private static PlayerColor oppositeColor(PlayerColor color) {
        return color == PlayerColor.WHITE ? PlayerColor.BLACK : PlayerColor.WHITE;
    }
}