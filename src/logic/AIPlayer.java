package logic;

import model.Board;
import model.PlayerColor;
import model.Position;
import model.pieces.Bishop;
import model.pieces.King;
import model.pieces.Knight;
import model.pieces.Pawn;
import model.pieces.Piece;
import model.pieces.Queen;
import model.pieces.Rook;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer {
    private final Random random = new Random();

    public MoveOption chooseGreedyMove(Board board, PlayerColor aiColor) {
        List<MoveOption> allMoves = getAllLegalMoves(board, aiColor);

        if (allMoves.isEmpty()) {
            return null;
        }

        MoveOption bestCaptureMove = null;
        int bestCaptureValue = -1;

        for (MoveOption moveOption : allMoves) {
            Piece targetPiece = board.getPiece(
                    moveOption.getTo().getRow(),
                    moveOption.getTo().getCol()
            );

            if (targetPiece != null && targetPiece.getColor() != aiColor) {
                int value = getPieceValue(targetPiece);

                if (value > bestCaptureValue) {
                    bestCaptureValue = value;
                    bestCaptureMove = moveOption;
                }
            }
        }

        if (bestCaptureMove != null) {
            return bestCaptureMove;
        }

        return allMoves.get(random.nextInt(allMoves.size()));
    }

    private List<MoveOption> getAllLegalMoves(Board board, PlayerColor color) {
        List<MoveOption> allMoves = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);

                if (piece != null && piece.getColor() == color) {
                    List<Position> legalMoves = MoveValidator.getLegalMoves(board, piece);

                    for (Position move : legalMoves) {
                        allMoves.add(new MoveOption(piece.getPosition(), move));
                    }
                }
            }
        }

        return allMoves;
    }

    private int getPieceValue(Piece piece) {
        if (piece instanceof Pawn) {
            return 1;
        }
        if (piece instanceof Knight) {
            return 3;
        }
        if (piece instanceof Bishop) {
            return 3;
        }
        if (piece instanceof Rook) {
            return 5;
        }
        if (piece instanceof Queen) {
            return 9;
        }
        if (piece instanceof King) {
            return 100;
        }
        return 0;
    }
}