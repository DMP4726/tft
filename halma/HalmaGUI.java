package halma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class HalmaGUI extends JFrame {
    private final HalmaBoard board;
    private final HalmaAI ai;
    private final JButton[][] buttons;
    
    private Point selectedPoint = null;
    private List<Move> currentValidMoves = new ArrayList<>();
    private boolean isAiThinking = false;

    public HalmaGUI() {
        board = new HalmaBoard();
        ai = new HalmaAI(PieceType.PLAYER_2); // AI là Player 2 (Đỏ)
        buttons = new JButton[HalmaBoard.SIZE][HalmaBoard.SIZE];

        setTitle("Game Halma 16x16 - Đến lượt bạn");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel boardPanel = new JPanel(new GridLayout(HalmaBoard.SIZE, HalmaBoard.SIZE));
        initializeButtons(boardPanel);

        add(boardPanel, BorderLayout.CENTER);
        updateBoardView();
    }

    private void initializeButtons(JPanel panel) {
        for (int r = 0; r < HalmaBoard.SIZE; r++) {
            for (int c = 0; c < HalmaBoard.SIZE; c++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(45, 45));
                button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                
                final int row = r;
                final int col = c;
                
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        handleCellClick(row, col);
                    }
                });

                buttons[r][c] = button;
                panel.add(button);
            }
        }
    }

    private void updateBoardView() {
        // 1. Tô màu hiển thị vị trí các quân cờ
        for (int r = 0; r < HalmaBoard.SIZE; r++) {
            for (int c = 0; c < HalmaBoard.SIZE; c++) {
                PieceType piece = board.getPiece(r, c);
                if (piece == PieceType.PLAYER_1) {
                    buttons[r][c].setBackground(new Color(65, 105, 225)); // Xanh dương
                } else if (piece == PieceType.PLAYER_2) {
                    buttons[r][c].setBackground(new Color(220, 20, 60));  // Đỏ
                } else {
                    buttons[r][c].setBackground(new Color(240, 240, 240)); // Trắng xám
                }
            }
        }
        
        // 2. Highlight quân cờ đang được chọn (Màu xanh lá)
        if (selectedPoint != null) {
            buttons[selectedPoint.x()][selectedPoint.y()].setBackground(Color.GREEN);
        }

        // 3. Highlight những ô có thể di chuyển tới (Màu vàng)
        for (Move move : currentValidMoves) {
            Point target = move.to();
            buttons[target.x()][target.y()].setBackground(Color.YELLOW);
        }
    }

    private void handleCellClick(int row, int col) {
        if (isAiThinking) return;

        Point clickedPoint = new Point(row, col);

        if (selectedPoint == null) {
            if (board.getPiece(row, col) == PieceType.PLAYER_1) {
                selectPiece(clickedPoint);
            }
        } else {
            Move selectedMove = null;
            for (Move move : currentValidMoves) {
                if (move.to().x() == clickedPoint.x() && move.to().y() == clickedPoint.y()) {
                    selectedMove = move;
                    break;
                }
            }

            if (selectedMove != null) {
                board.makeMove(selectedMove, PieceType.PLAYER_1);
                
                selectedPoint = null;
                currentValidMoves.clear();
                updateBoardView();

                // Kiểm tra người chơi có thắng không
                if (board.hasWon(PieceType.PLAYER_1)) {
                    JOptionPane.showMessageDialog(this, "🎉 CHÚC MỪNG! BẠN ĐÃ CHIẾN THẮNG AI!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    return; 
                }

                triggerAiTurn();
            } else {
                if (board.getPiece(row, col) == PieceType.PLAYER_1) {
                    selectPiece(clickedPoint);
                } else {
                    selectedPoint = null;
                    currentValidMoves.clear();
                    updateBoardView();
                }
            }
        }
    }

    private void selectPiece(Point p) {
        selectedPoint = p;
        currentValidMoves.clear();
        
        List<Move> allMoves = board.getAllValidMoves(PieceType.PLAYER_1);
        for (Move move : allMoves) {
            if (move.from().x() == p.x() && move.from().y() == p.y()) {
                currentValidMoves.add(move);
            }
        }
        updateBoardView();
    }

    private void triggerAiTurn() {
        isAiThinking = true;
        setTitle("Game Halma 16x16 - AI đang suy nghĩ...");

        new Thread(() -> {
            Move bestMove = ai.findBestMove(board);
            
            if (bestMove != null) {
                board.makeMove(bestMove, PieceType.PLAYER_2);
            }

            SwingUtilities.invokeLater(() -> {
                updateBoardView();
                
                // Kiểm tra AI có thắng không
                if (board.hasWon(PieceType.PLAYER_2)) {
                    setTitle("Game Halma 16x16 - Game Over");
                    JOptionPane.showMessageDialog(this, "🤖 RẤT TIẾC! AI ĐÃ THẮNG BẠN MẤT RỒI!", "Game Over", JOptionPane.WARNING_MESSAGE);
                } else {
                    setTitle("Game Halma 16x16 - Đến lượt bạn");
                    isAiThinking = false; 
                }
            });
        }).start();
    }
}