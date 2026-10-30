package model;

import model.pieces.*;

public class Board {
    private final Piece[][] grid;

    public Board() {
        grid = new Piece[8][8];
        initializeBoard();
    }

    public Board(boolean initialize) {
        grid = new Piece[8][8];
        if (initialize) {
            initializeBoard();
        }
    }

    public Piece getPiece(int row, int col) {
        if (!isInsideBoard(row, col)) {
            return null;
        }
        return grid[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        if (isInsideBoard(row, col)) {
            grid[row][col] = piece;
            if (piece != null) {
                piece.setPosition(new Position(row, col));
            }
        }
    }

    public boolean isInsideBoard(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public boolean isEmpty(int row, int col) {
        return isInsideBoard(row, col) && grid[row][col] == null;
    }

    public boolean hasEnemyPiece(int row, int col, PlayerColor color) {
        if (!isInsideBoard(row, col)) {
            return false;
        }

        Piece piece = grid[row][col];
        return piece != null && piece.getColor() != color;
    }

    public boolean hasFriendlyPiece(int row, int col, PlayerColor color) {
        if (!isInsideBoard(row, col)) {
            return false;
        }

        Piece piece = grid[row][col];
        return piece != null && piece.getColor() == color;
    }

    public void movePiece(Position from, Position to) {
        Piece piece = getPiece(from.getRow(), from.getCol());

        if (piece == null) {
            return;
        }

        grid[to.getRow()][to.getCol()] = piece;
        grid[from.getRow()][from.getCol()] = null;

        piece.setPosition(to);
        piece.setMoved(true);

        handleAutomaticPawnPromotion(piece);
    }

    private void handleAutomaticPawnPromotion(Piece piece) {
        if (!(piece instanceof Pawn)) {
            return;
        }

        int row = piece.getPosition().getRow();
        int col = piece.getPosition().getCol();

        if (piece.getColor() == PlayerColor.WHITE && row == 0) {
            grid[row][col] = new Queen(PlayerColor.WHITE, new Position(row, col));
        } else if (piece.getColor() == PlayerColor.BLACK && row == 7) {
            grid[row][col] = new Queen(PlayerColor.BLACK, new Position(row, col));
        }
    }

    public boolean isPromotionSquare(Position position, PlayerColor color) {
        if (color == PlayerColor.WHITE) {
            return position.getRow() == 0;
        }
        return position.getRow() == 7;
    }

    public void promotePawn(Position position, String pieceType, PlayerColor color) {
        int row = position.getRow();
        int col = position.getCol();

        switch (pieceType) {
            case "Rook":
                grid[row][col] = new Rook(color, position);
                break;
            case "Bishop":
                grid[row][col] = new Bishop(color, position);
                break;
            case "Knight":
                grid[row][col] = new Knight(color, position);
                break;
            case "Queen":
            default:
                grid[row][col] = new Queen(color, position);
                break;
        }
    }

    public boolean isPawnAt(Position position, PlayerColor color) {
        Piece piece = getPiece(position.getRow(), position.getCol());
        return piece instanceof Pawn && piece.getColor() == color;
    }

    public void resetBoard() {
        clearBoard();
        initializeBoard();
    }

    public void loadFrom(Board otherBoard) {
        clearBoard();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = otherBoard.getPiece(row, col);
                if (piece != null) {
                    setPiece(row, col, clonePiece(piece));
                }
            }
        }
    }

    private void clearBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                grid[row][col] = null;
            }
        }
    }

    public Board copy() {
        Board newBoard = new Board(false);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = this.getPiece(row, col);
                if (piece != null) {
                    newBoard.setPiece(row, col, clonePiece(piece));
                }
            }
        }

        return newBoard;
    }

    private Piece clonePiece(Piece piece) {
        Position pos = new Position(piece.getPosition().getRow(), piece.getPosition().getCol());
        Piece clonedPiece = null;

        if (piece instanceof Pawn) {
            clonedPiece = new Pawn(piece.getColor(), pos);
        } else if (piece instanceof Rook) {
            clonedPiece = new Rook(piece.getColor(), pos);
        } else if (piece instanceof Knight) {
            clonedPiece = new Knight(piece.getColor(), pos);
        } else if (piece instanceof Bishop) {
            clonedPiece = new Bishop(piece.getColor(), pos);
        } else if (piece instanceof Queen) {
            clonedPiece = new Queen(piece.getColor(), pos);
        } else if (piece instanceof King) {
            clonedPiece = new King(piece.getColor(), pos);
        }

        if (clonedPiece != null) {
            clonedPiece.setMoved(piece.hasMoved());
        }

        return clonedPiece;
    }

    private void initializeBoard() {
        // Black
        grid[0][0] = new Rook(PlayerColor.BLACK, new Position(0, 0));
        grid[0][1] = new Knight(PlayerColor.BLACK, new Position(0, 1));
        grid[0][2] = new Bishop(PlayerColor.BLACK, new Position(0, 2));
        grid[0][3] = new Queen(PlayerColor.BLACK, new Position(0, 3));
        grid[0][4] = new King(PlayerColor.BLACK, new Position(0, 4));
        grid[0][5] = new Bishop(PlayerColor.BLACK, new Position(0, 5));
        grid[0][6] = new Knight(PlayerColor.BLACK, new Position(0, 6));
        grid[0][7] = new Rook(PlayerColor.BLACK, new Position(0, 7));

        for (int col = 0; col < 8; col++) {
            grid[1][col] = new Pawn(PlayerColor.BLACK, new Position(1, col));
        }

        // White
        grid[7][0] = new Rook(PlayerColor.WHITE, new Position(7, 0));
        grid[7][1] = new Knight(PlayerColor.WHITE, new Position(7, 1));
        grid[7][2] = new Bishop(PlayerColor.WHITE, new Position(7, 2));
        grid[7][3] = new Queen(PlayerColor.WHITE, new Position(7, 3));
        grid[7][4] = new King(PlayerColor.WHITE, new Position(7, 4));
        grid[7][5] = new Bishop(PlayerColor.WHITE, new Position(7, 5));
        grid[7][6] = new Knight(PlayerColor.WHITE, new Position(7, 6));
        grid[7][7] = new Rook(PlayerColor.WHITE, new Position(7, 7));

        for (int col = 0; col < 8; col++) {
            grid[6][col] = new Pawn(PlayerColor.WHITE, new Position(6, col));
        }
    }
}