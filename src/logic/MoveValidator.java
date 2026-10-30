package logic;

import model.Board;
import model.PlayerColor;
import model.Position;
import model.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

public class MoveValidator {

    public static List<Position> getLegalMoves(Board board, Piece piece) {
        List<Position> legalMoves = new ArrayList<>();
        List<Position> possibleMoves = piece.getPossibleMoves(board);

        for (Position move : possibleMoves) {
            Board simulatedBoard = board.copy();

            Position from = piece.getPosition();
            simulatedBoard.movePiece(from, move);

            boolean kingInCheck = CheckDetector.isKingInCheck(simulatedBoard, piece.getColor());

            if (!kingInCheck) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }
}