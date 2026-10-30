package logic;

import model.Position;
import model.pieces.Pawn;
import model.pieces.Piece;

public class MoveRecord {
    private final Piece movedPiece;
    private final Position from;
    private final Position to;
    private final Piece capturedPiece;
    private final boolean castlingMove;
    private final boolean kingsideCastle;
    private final boolean enPassantMove;
    private final boolean check;
    private final boolean checkmate;

    public MoveRecord(Piece movedPiece,
                      Position from,
                      Position to,
                      Piece capturedPiece,
                      boolean castlingMove,
                      boolean kingsideCastle,
                      boolean enPassantMove,
                      boolean check,
                      boolean checkmate) {
        this.movedPiece = movedPiece;
        this.from = from;
        this.to = to;
        this.capturedPiece = capturedPiece;
        this.castlingMove = castlingMove;
        this.kingsideCastle = kingsideCastle;
        this.enPassantMove = enPassantMove;
        this.check = check;
        this.checkmate = checkmate;
    }

    public String toDisplayString() {
        String notation;

        if (castlingMove) {
            notation = kingsideCastle ? "O-O" : "O-O-O";
        } else {
            String destination = toSquare(to);

            if (movedPiece instanceof Pawn) {
                String fromFile = String.valueOf((char) ('a' + from.getCol()));

                if (capturedPiece != null) {
                    notation = fromFile + "x" + destination;
                    if (enPassantMove) {
                        notation += " e.p.";
                    }
                } else {
                    notation = destination;
                }
            } else {
                String pieceLetter = getPieceLetter(movedPiece);

                if (capturedPiece != null) {
                    notation = pieceLetter + "x" + destination;
                } else {
                    notation = pieceLetter + destination;
                }
            }
        }

        if (checkmate) {
            notation += "#";
        } else if (check) {
            notation += "+";
        }

        return notation;
    }

    private String getPieceLetter(Piece piece) {
        String symbol = piece.getSymbol().toUpperCase();

        switch (symbol) {
            case "N":
                return "N";
            case "B":
                return "B";
            case "R":
                return "R";
            case "Q":
                return "Q";
            case "K":
                return "K";
            default:
                return symbol;
        }
    }

    private String toSquare(Position position) {
        char file = (char) ('a' + position.getCol());
        int rank = 8 - position.getRow();
        return "" + file + rank;
    }
}