package gui;

import logic.CheckDetector;
import logic.GameSnapshot;
import logic.GameStateChecker;
import logic.MinimaxAI;
import logic.MoveOption;
import logic.MoveRecord;
import logic.MoveValidator;
import model.Board;
import model.PlayerColor;
import model.Position;
import model.pieces.King;
import model.pieces.Pawn;
import model.pieces.Piece;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class BoardPanel extends JPanel {
    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = 8;

    private final Board board;
    private final MinimaxAI aiPlayer;
    private final SidePanel sidePanel;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private List<Position> possibleMoves = new ArrayList<>();

    private PlayerColor currentTurn = PlayerColor.WHITE;

    private boolean gameOver = false;
    private String gameOverMessage = "";

    private List<MoveRecord> moveHistory = new ArrayList<>();
    private List<String> whiteCapturedPieces = new ArrayList<>();
    private List<String> blackCapturedPieces = new ArrayList<>();

    private final Stack<GameSnapshot> historyStack = new Stack<>();

    public BoardPanel(Board board, SidePanel sidePanel) {
        this.board = board;
        this.sidePanel = sidePanel;
        this.aiPlayer = new MinimaxAI();

        setPreferredSize(new Dimension(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE + 80));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });

        refreshSidePanel();
    }

    public void resetGame() {
        board.resetBoard();
        currentTurn = PlayerColor.WHITE;
        gameOver = false;
        gameOverMessage = "";
        moveHistory.clear();
        whiteCapturedPieces.clear();
        blackCapturedPieces.clear();
        historyStack.clear();
        clearSelection();
        refreshSidePanel();
        repaint();
    }

    public void undoLastMove() {
        if (historyStack.isEmpty()) {
            return;
        }

        GameSnapshot snapshot = historyStack.pop();

        board.loadFrom(snapshot.getBoard());
        currentTurn = snapshot.getCurrentTurn();
        gameOver = snapshot.isGameOver();
        gameOverMessage = snapshot.getGameOverMessage();
        moveHistory = snapshot.getMoveHistory();
        whiteCapturedPieces = snapshot.getWhiteCapturedPieces();
        blackCapturedPieces = snapshot.getBlackCapturedPieces();

        clearSelection();
        refreshSidePanel();
        repaint();
    }

    private void saveSnapshot() {
        historyStack.push(new GameSnapshot(
                board,
                currentTurn,
                gameOver,
                gameOverMessage,
                moveHistory,
                whiteCapturedPieces,
                blackCapturedPieces
        ));
    }

    private void handleMouseClick(int x, int y) {
        if (gameOver) {
            return;
        }

        if (currentTurn != PlayerColor.WHITE) {
            return;
        }

        if (y >= TILE_SIZE * BOARD_SIZE) {
            return;
        }

        int col = x / TILE_SIZE;
        int row = y / TILE_SIZE;

        if (!board.isInsideBoard(row, col)) {
            return;
        }

        Position clickedPosition = new Position(row, col);
        Piece clickedPiece = board.getPiece(row, col);

        if (selectedRow == row && selectedCol == col) {
            clearSelection();
            repaint();
            return;
        }

        if (selectedRow != -1 && selectedCol != -1 && isPossibleMove(clickedPosition)) {
            saveSnapshot();

            Position from = new Position(selectedRow, selectedCol);
            Piece movedPiece = board.getPiece(from.getRow(), from.getCol());
            Piece capturedPiece = detectCapturedPieceBeforeMove(movedPiece, from, clickedPosition);

            board.movePiece(from, clickedPosition);
            handleHumanPromotionIfNeeded(clickedPosition);
            addMoveRecord(movedPiece, from, clickedPosition, capturedPiece);

            switchTurn();
            clearSelection();
            updateGameState();

            if (!gameOver) {
                makeAIMove();
            }

            refreshSidePanel();
            repaint();
            return;
        }

        if (clickedPiece != null && clickedPiece.getColor() == currentTurn) {
            selectedRow = row;
            selectedCol = col;
            possibleMoves = MoveValidator.getLegalMoves(board, clickedPiece);
            repaint();
            return;
        }

        clearSelection();
        repaint();
    }

    private void makeAIMove() {
        if (currentTurn != PlayerColor.BLACK || gameOver) {
            return;
        }

        MoveOption aiMove = aiPlayer.chooseBestMove(board, PlayerColor.BLACK, 2);

        if (aiMove == null) {
            updateGameState();
            repaint();
            return;
        }

        Piece movedPiece = board.getPiece(aiMove.getFrom().getRow(), aiMove.getFrom().getCol());
        Piece capturedPiece = detectCapturedPieceBeforeMove(movedPiece, aiMove.getFrom(), aiMove.getTo());

        board.movePiece(aiMove.getFrom(), aiMove.getTo());
        addMoveRecord(movedPiece, aiMove.getFrom(), aiMove.getTo(), capturedPiece);

        switchTurn();
        updateGameState();
        refreshSidePanel();
        repaint();
    }

    private Piece detectCapturedPieceBeforeMove(Piece movedPiece, Position from, Position to) {
        Piece directCapture = board.getPiece(to.getRow(), to.getCol());
        if (directCapture != null) {
            return directCapture;
        }

        if (movedPiece instanceof Pawn
                && from.getCol() != to.getCol()
                && board.isEmpty(to.getRow(), to.getCol())) {

            int capturedPawnRow = (movedPiece.getColor() == PlayerColor.WHITE)
                    ? to.getRow() + 1
                    : to.getRow() - 1;

            return board.getPiece(capturedPawnRow, to.getCol());
        }

        return null;
    }

    private void handleHumanPromotionIfNeeded(Position to) {
        if (board.isPawnAt(to, PlayerColor.WHITE) && board.isPromotionSquare(to, PlayerColor.WHITE)) {
            String[] options = {"Queen", "Rook", "Bishop", "Knight"};

            String choice = (String) JOptionPane.showInputDialog(
                    this,
                    "Choose promotion piece:",
                    "Pawn Promotion",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == null) {
                choice = "Queen";
            }

            board.promotePawn(to, choice, PlayerColor.WHITE);
        }
    }

    private void addMoveRecord(Piece movedPiece, Position from, Position to, Piece capturedPiece) {
        boolean castlingMove = movedPiece instanceof King
                && Math.abs(to.getCol() - from.getCol()) == 2;

        boolean kingsideCastle = castlingMove && to.getCol() > from.getCol();

        boolean enPassantMove = movedPiece instanceof Pawn
                && from.getCol() != to.getCol()
                && capturedPiece != null
                && board.isEmpty(from.getRow(), to.getCol());

        PlayerColor opponentColor = (movedPiece.getColor() == PlayerColor.WHITE)
                ? PlayerColor.BLACK
                : PlayerColor.WHITE;

        boolean checkmate = GameStateChecker.isCheckmate(board, opponentColor);
        boolean check = !checkmate && CheckDetector.isKingInCheck(board, opponentColor);

        MoveRecord record = new MoveRecord(
                movedPiece,
                from,
                to,
                capturedPiece,
                castlingMove,
                kingsideCastle,
                enPassantMove,
                check,
                checkmate
        );

        moveHistory.add(record);

        if (capturedPiece != null) {
            if (capturedPiece.getColor() == PlayerColor.WHITE) {
                whiteCapturedPieces.add(capturedPiece.getSymbol());
            } else {
                blackCapturedPieces.add(capturedPiece.getSymbol());
            }
        }
    }

    private void refreshSidePanel() {
        StringBuilder historyText = new StringBuilder();

        for (int i = 0; i < moveHistory.size(); i++) {
            historyText.append(i + 1)
                    .append(". ")
                    .append(moveHistory.get(i).toDisplayString())
                    .append("\n");
        }

        String whiteCaptured = String.join(" ", whiteCapturedPieces);
        String blackCaptured = String.join(" ", blackCapturedPieces);

        sidePanel.setMoveHistory(historyText.toString());
        sidePanel.setCapturedPieces(whiteCaptured, blackCaptured);
    }

    private void updateGameState() {
        if (GameStateChecker.isCheckmate(board, currentTurn)) {
            gameOver = true;
            gameOverMessage = "CHECKMATE - " + oppositeColor(currentTurn) + " wins";
        } else if (GameStateChecker.isStalemate(board, currentTurn)) {
            gameOver = true;
            gameOverMessage = "STALEMATE - Draw";
        }
    }

    private PlayerColor oppositeColor(PlayerColor color) {
        return color == PlayerColor.WHITE ? PlayerColor.BLACK : PlayerColor.WHITE;
    }

    private boolean isPossibleMove(Position clickedPosition) {
        for (Position move : possibleMoves) {
            if (move.equals(clickedPosition)) {
                return true;
            }
        }
        return false;
    }

    private void switchTurn() {
        currentTurn = (currentTurn == PlayerColor.WHITE) ? PlayerColor.BLACK : PlayerColor.WHITE;
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        possibleMoves.clear();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBoard(g2d);
        drawKingInCheckHighlight(g2d);
        drawPossibleMoves(g2d);
        drawSelection(g2d);
        drawPieces(g2d);
        drawCoordinates(g2d);
        drawInfoBar(g2d);
    }

    private void drawBoard(Graphics2D g2d) {
        Color lightSquare = new Color(240, 217, 181);
        Color darkSquare = new Color(181, 136, 99);

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean isLight = (row + col) % 2 == 0;
                g2d.setColor(isLight ? lightSquare : darkSquare);
                g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawKingInCheckHighlight(Graphics2D g2d) {
        highlightKingIfInCheck(g2d, PlayerColor.WHITE);
        highlightKingIfInCheck(g2d, PlayerColor.BLACK);
    }

    private void highlightKingIfInCheck(Graphics2D g2d, PlayerColor color) {
        if (!CheckDetector.isKingInCheck(board, color)) {
            return;
        }

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece instanceof King && piece.getColor() == color) {
                    g2d.setColor(new Color(255, 70, 70, 130));
                    g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                    g2d.setColor(new Color(180, 0, 0));
                    g2d.drawRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    g2d.drawRect(col * TILE_SIZE + 1, row * TILE_SIZE + 1, TILE_SIZE - 2, TILE_SIZE - 2);
                    return;
                }
            }
        }
    }

    private void drawPossibleMoves(Graphics2D g2d) {
        for (Position move : possibleMoves) {
            int x = move.getCol() * TILE_SIZE;
            int y = move.getRow() * TILE_SIZE;

            Piece targetPiece = board.getPiece(move.getRow(), move.getCol());

            if (targetPiece == null) {
                g2d.setColor(new Color(50, 200, 50, 140));
                g2d.fillOval(x + TILE_SIZE / 3, y + TILE_SIZE / 3, TILE_SIZE / 3, TILE_SIZE / 3);
            } else {
                g2d.setColor(new Color(220, 50, 50, 160));
                g2d.drawOval(x + 8, y + 8, TILE_SIZE - 16, TILE_SIZE - 16);
                g2d.drawOval(x + 10, y + 10, TILE_SIZE - 20, TILE_SIZE - 20);
            }
        }
    }

    private void drawSelection(Graphics2D g2d) {
        if (selectedRow != -1 && selectedCol != -1) {
            g2d.setColor(new Color(255, 255, 0, 120));
            g2d.fillRect(selectedCol * TILE_SIZE, selectedRow * TILE_SIZE, TILE_SIZE, TILE_SIZE);

            g2d.setColor(Color.YELLOW);
            g2d.drawRect(selectedCol * TILE_SIZE, selectedRow * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            g2d.drawRect(selectedCol * TILE_SIZE + 1, selectedRow * TILE_SIZE + 1, TILE_SIZE - 2, TILE_SIZE - 2);
        }
    }

    private void drawPieces(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 32));

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    g2d.setColor(piece.getColor() == PlayerColor.WHITE ? Color.WHITE : Color.BLACK);

                    String symbol = piece.getSymbol();

                    int x = col * TILE_SIZE + TILE_SIZE / 2 - 10;
                    int y = row * TILE_SIZE + TILE_SIZE / 2 + 10;

                    g2d.drawString(symbol, x, y);
                }
            }
        }
    }

    private void drawCoordinates(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        for (int col = 0; col < BOARD_SIZE; col++) {
            char file = (char) ('A' + col);
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf(file), col * TILE_SIZE + TILE_SIZE - 16, TILE_SIZE * BOARD_SIZE - 6);
        }

        for (int row = 0; row < BOARD_SIZE; row++) {
            int rank = 8 - row;
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf(rank), 4, row * TILE_SIZE + 14);
        }
    }

    private void drawInfoBar(Graphics2D g2d) {
        int barY = TILE_SIZE * BOARD_SIZE;

        g2d.setColor(new Color(30, 30, 30));
        g2d.fillRect(0, barY, TILE_SIZE * BOARD_SIZE, 80);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Current Turn: " + currentTurn, 20, barY + 22);

        boolean whiteCheck = CheckDetector.isKingInCheck(board, PlayerColor.WHITE);
        boolean blackCheck = CheckDetector.isKingInCheck(board, PlayerColor.BLACK);

        g2d.setFont(new Font("Arial", Font.BOLD, 16));

        if (gameOver) {
            g2d.setColor(new Color(255, 120, 120));
            g2d.drawString(gameOverMessage, 20, barY + 48);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("Game Over", 20, barY + 68);
        } else if (whiteCheck) {
            g2d.setColor(new Color(255, 80, 80));
            g2d.drawString("WHITE is in CHECK", 20, barY + 48);
        } else if (blackCheck) {
            g2d.setColor(new Color(255, 80, 80));
            g2d.drawString("BLACK is in CHECK", 20, barY + 48);
        } else {
            g2d.setColor(new Color(120, 220, 120));
            g2d.drawString("No check detected", 20, barY + 48);
        }
    }
}