package logic;

import model.Board;
import model.PlayerColor;
import model.Position;
import model.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

public class MinimaxAI {
    private final BoardEvaluator evaluator = new BoardEvaluator();

    public MoveOption chooseBestMove(Board board, PlayerColor aiColor, int depth) {
        List<MoveOption> legalMoves = getAllLegalMoves(board, aiColor);

        if (legalMoves.isEmpty()) {
            return null;
        }

        MoveOption bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (MoveOption move : legalMoves) {
            Board simulatedBoard = board.copy();
            simulatedBoard.movePiece(move.getFrom(), move.getTo());

            int score = alphaBeta(simulatedBoard, depth - 1, false, alpha, beta);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }

            alpha = Math.max(alpha, bestScore);
        }

        return bestMove;
    }

    private int alphaBeta(Board board, int depth, boolean maximizingPlayer, int alpha, int beta) {
        PlayerColor currentColor = maximizingPlayer ? PlayerColor.BLACK : PlayerColor.WHITE;

        if (depth == 0) {
            return evaluator.evaluate(board);
        }

        if (GameStateChecker.isCheckmate(board, currentColor)) {
            return maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }

        if (GameStateChecker.isStalemate(board, currentColor)) {
            return 0;
        }

        List<MoveOption> legalMoves = getAllLegalMoves(board, currentColor);

        if (legalMoves.isEmpty()) {
            return evaluator.evaluate(board);
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;

            for (MoveOption move : legalMoves) {
                Board simulatedBoard = board.copy();
                simulatedBoard.movePiece(move.getFrom(), move.getTo());

                int eval = alphaBeta(simulatedBoard, depth - 1, false, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);

                if (beta <= alpha) {
                    break;
                }
            }

            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;

            for (MoveOption move : legalMoves) {
                Board simulatedBoard = board.copy();
                simulatedBoard.movePiece(move.getFrom(), move.getTo());

                int eval = alphaBeta(simulatedBoard, depth - 1, true, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);

                if (beta <= alpha) {
                    break;
                }
            }

            return minEval;
        }
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
}