package logic;

import model.Board;
import model.PlayerColor;

import java.util.ArrayList;
import java.util.List;

public class GameSnapshot {
    private final Board board;
    private final PlayerColor currentTurn;
    private final boolean gameOver;
    private final String gameOverMessage;
    private final List<MoveRecord> moveHistory;
    private final List<String> whiteCapturedPieces;
    private final List<String> blackCapturedPieces;

    public GameSnapshot(Board board,
                        PlayerColor currentTurn,
                        boolean gameOver,
                        String gameOverMessage,
                        List<MoveRecord> moveHistory,
                        List<String> whiteCapturedPieces,
                        List<String> blackCapturedPieces) {
        this.board = board.copy();
        this.currentTurn = currentTurn;
        this.gameOver = gameOver;
        this.gameOverMessage = gameOverMessage;
        this.moveHistory = new ArrayList<>(moveHistory);
        this.whiteCapturedPieces = new ArrayList<>(whiteCapturedPieces);
        this.blackCapturedPieces = new ArrayList<>(blackCapturedPieces);
    }

    public Board getBoard() {
        return board;
    }

    public PlayerColor getCurrentTurn() {
        return currentTurn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getGameOverMessage() {
        return gameOverMessage;
    }

    public List<MoveRecord> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    public List<String> getWhiteCapturedPieces() {
        return new ArrayList<>(whiteCapturedPieces);
    }

    public List<String> getBlackCapturedPieces() {
        return new ArrayList<>(blackCapturedPieces);
    }
}