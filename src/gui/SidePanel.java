package gui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

public class SidePanel extends JPanel {
    private final JTextArea moveHistoryArea;
    private final JLabel whiteCapturedLabel;
    private final JLabel blackCapturedLabel;

    public SidePanel() {
        setPreferredSize(new Dimension(260, 720));
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel historyTitle = new JLabel("Move History");
        historyTitle.setFont(new Font("Arial", Font.BOLD, 18));

        moveHistoryArea = new JTextArea();
        moveHistoryArea.setEditable(false);
        moveHistoryArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(moveHistoryArea);
        scrollPane.setPreferredSize(new Dimension(240, 420));

        JPanel capturedPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        capturedPanel.setBorder(BorderFactory.createTitledBorder("Captured Pieces"));

        whiteCapturedLabel = new JLabel("White captured: ");
        blackCapturedLabel = new JLabel("Black captured: ");

        whiteCapturedLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        blackCapturedLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        capturedPanel.add(whiteCapturedLabel);
        capturedPanel.add(blackCapturedLabel);

        add(historyTitle, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(capturedPanel, BorderLayout.SOUTH);
    }

    public void setMoveHistory(String text) {
        moveHistoryArea.setText(text);
    }

    public void setCapturedPieces(String whiteCaptured, String blackCaptured) {
        whiteCapturedLabel.setText("White captured: " + whiteCaptured);
        blackCapturedLabel.setText("Black captured: " + blackCaptured);
    }

    public void clearAll() {
        moveHistoryArea.setText("");
        whiteCapturedLabel.setText("White captured: ");
        blackCapturedLabel.setText("Black captured: ");
    }
}