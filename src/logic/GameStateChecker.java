package logic;

import model.Board;
import model.PlayerColor;
import model.Position;
import model.pieces.Piece;

import java.util.List;

public class GameStateChecker {

    public static boolean hasAnyLegalMove(Board board, PlayerColor color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);

                if (piece != null && piece.getColor() == color) {
                    List<Position> legalMoves = MoveValidator.getLegalMoves(board, piece);
                    if (!legalMoves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isCheckmate(Board board, PlayerColor color) {
        return CheckDetector.isKingInCheck(board, color) && !hasAnyLegalMove(board, color);
    }

    public static boolean isStalemate(Board board, PlayerColor color) {
        return !CheckDetector.isKingInCheck(board, color) && !hasAnyLegalMove(board, color);
    }
}