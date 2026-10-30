package logic;

import model.Board;
import model.PlayerColor;
import model.pieces.Bishop;
import model.pieces.King;
import model.pieces.Knight;
import model.pieces.Pawn;
import model.pieces.Piece;
import model.pieces.Queen;
import model.pieces.Rook;

public class BoardEvaluator {

    public int evaluate(Board board) {
        int score = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);

                if (piece != null) {
                    int value = getPieceValue(piece);

                    if (piece.getColor() == PlayerColor.BLACK) {
                        score += value;
                    } else {
                        score -= value;
                    }
                }
            }
        }

        return score;
    }

    private int getPieceValue(Piece piece) {
        if (piece instanceof Pawn) return 100;
        if (piece instanceof Knight) return 320;
        if (piece instanceof Bishop) return 330;
        if (piece instanceof Rook) return 500;
        if (piece instanceof Queen) return 900;
        if (piece instanceof King) return 20000;
        return 0;
    }
}