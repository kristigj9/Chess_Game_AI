package gui;

import model.Board;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class GameFrame extends JFrame {

    public GameFrame() {
        Board board = new Board();
        SidePanel sidePanel = new SidePanel();
        BoardPanel boardPanel = new BoardPanel(board, sidePanel);

        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> boardPanel.resetGame());

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> boardPanel.undoLastMove());

        JPanel topPanel = new JPanel();
        topPanel.add(restartButton);
        topPanel.add(undoButton);

        setTitle("Chess Game AI - Phase 17");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        add(topPanel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(sidePanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }
}