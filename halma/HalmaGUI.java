package halma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class HalmaGUI extends JFrame {

    private HalmaBoard logicBoard;
    private GameBoardPanel boardPanel;
    private Point selectedPoint = null; // Lưu toàn bộ nước đi hợp lệ của người chơi
    private final List<Point> validDestinations = new ArrayList<>();
    private int aiDepth = 2;

    private HalmaAI ai;

    private boolean playerTurn = true;

    private PieceType humanPlayer;
    private PieceType aiPlayer;

    private boolean jumpInProgress = false;

    private Point jumpingPiece = null;

    private boolean[][] visitedJumpPositions =
            new boolean[HalmaBoard.SIZE]
                    [HalmaBoard.SIZE];

    private JButton endTurnButton;

    private JPanel sidePanel;
    private JLabel turnLabel;
    private JButton optionButton;

    public HalmaGUI() {
        setTitle("Halma 16x16 - Trò chơi cờ Halma");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 355); // Kích thước vừa vặn cho Menu lúc đầu
        setLocationRelativeTo(null); // Hiển thị ở chính giữa màn hình
        
        //Hiển thị giao diện Menu chính
        add(new MainMenuPanel(), BorderLayout.CENTER);
        setVisible(true);
    }

    /**
     * 1. LỚP MAIN MENU PANEL (Giao diện Menu trước khi vào game)
     */
    private class MainMenuPanel extends JPanel {
        public MainMenuPanel() {
            setLayout(new GridLayout(5, 1, 10, 10)); // Chia làm 4 hàng dọc cân đối
            setBackground(new Color(240, 248, 255)); // Màu nền xanh nhạt dễ chịu

            // Hàng 1: Tiêu đề Game lớn
            JLabel titleLabel = new JLabel("GAME HALMA 16x16", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
            titleLabel.setForeground(new Color(0, 51, 102));
            add(titleLabel);

            // 2. Cài đặt Độ khó AI (Tùy chọn)
            JPanel settingsPanel = new JPanel();
            settingsPanel.setOpaque(false);

            settingsPanel.add(new JLabel("Độ khó: "));

            String[] difficulties = {
                    "Dễ",
                    "Trung bình",
                    "Khó"
            };

            JComboBox<String> difficultyBox = new JComboBox<>(difficulties);

            difficultyBox.setSelectedIndex(1);

            settingsPanel.add(difficultyBox);

            add(settingsPanel);

            // Hàng 3: Cài đặt chế độ chơi (Bạn đi trước đấu với AI)
            JPanel sPanel = new JPanel();
            sPanel.setOpaque(false);
            sPanel.add(new JLabel("Phe:"));

            String[] sides = {
                    "Xanh dương",
                    "Đỏ"
            };

            JComboBox<String> sideBox =
                    new JComboBox<>(sides);

            sPanel.add(sideBox);
            add(sPanel);

            // Hàng 4: Nút bấm PLAY GAME để vào bàn cờ
            JPanel playPanel = new JPanel();
            playPanel.setOpaque(false);
            JButton playButton = new JButton("PLAY GAME");
            playButton.setFont(new Font("Arial", Font.BOLD, 20));
            playButton.setBackground(new Color(50, 205, 50)); // Nút màu xanh lá nổi bật
            playButton.setForeground(Color.WHITE);
            playButton.setFocusPainted(false);
            playButton.setPreferredSize(new Dimension(220, 50));
            
            // Sự kiện click nút Play
            playButton.addActionListener(e -> {
                switch (difficultyBox.getSelectedIndex()) {
                    case 0:
                        aiDepth = 2;
                        break;
                    case 1:
                        aiDepth = 3;
                        break;
                    case 2:
                        aiDepth = 4;
                        break;
                }
                if (sideBox.getSelectedIndex() == 0) {
                    humanPlayer = PieceType.PLAYER_1;
                    aiPlayer = PieceType.PLAYER_2;
                } else {
                    humanPlayer = PieceType.PLAYER_2;
                    aiPlayer = PieceType.PLAYER_1;
                }
                startGame(); // Chuyển sang màn hình bàn cờ
            });
            playPanel.add(playButton);
            add(playPanel);

            // Hàng 5: Dòng chữ thông tin chân trang
            JLabel footerLabel = new JLabel("Chúc bạn chơi vui vẻ và chiến thắng!", SwingConstants.CENTER);
            footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            footerLabel.setForeground(Color.GRAY);
            add(footerLabel);
        }
    }

    /**
     * 2. HÀM CHUYỂN ĐỔI TỪ MENU SANG BÀN CỜ GAME CHÍNH
     */
    private void startGame() {
        // Xóa sạch phần Panel Menu cũ ra khỏi cửa sổ chính
        getContentPane().removeAll();
        
        // Mở rộng kích thước cửa sổ để hiển thị bàn cờ lớn 16x16
        setSize(820, 640);
        setLocationRelativeTo(null); // Căn giữa lại màn hình sau khi đổi size

        selectedPoint = null;
        validDestinations.clear();
        playerTurn = (humanPlayer == PieceType.PLAYER_1);
        jumpInProgress = false;
        jumpingPiece = null;

        visitedJumpPositions =
                new boolean[HalmaBoard.SIZE][HalmaBoard.SIZE];

        // Khởi tạo bảng và vị trí quân cờ ban đầu
        logicBoard = new HalmaBoard();
        ai = new HalmaAI(aiPlayer, aiDepth);
        boardPanel = new GameBoardPanel();

        add(boardPanel, BorderLayout.CENTER);

        createSidePanel();

        endTurnButton =
                new JButton("Kết thúc lượt");

        endTurnButton.setEnabled(false);

        endTurnButton.addActionListener(e -> {

            if (jumpInProgress) {
                finishPlayerTurn();
            }

        });

        sidePanel.add(
                Box.createVerticalStrut(20)
        );

        sidePanel.add(
                endTurnButton
        );
        // Làm mới và vẽ lại toàn bộ cửa sổ JFrame
        revalidate();
        repaint();
        if (!playerTurn) {
            runAITurn();
        }
    }

    private void createSidePanel() {
        sidePanel = new JPanel();
        sidePanel.setLayout(
                new BoxLayout(
                        sidePanel, BoxLayout.Y_AXIS
                )
        );
        sidePanel.setPreferredSize(
                new Dimension(180, 0)
        );
        turnLabel = new JLabel();
        optionButton = new JButton("Tùy chọn");
        updateTurnDisplay();
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(turnLabel);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(optionButton);
        createOptionMenu();
        add(sidePanel, BorderLayout.EAST);
    }

    private void createOptionMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem continueItem = new JMenuItem("Continue");
        JMenuItem menuItem = new JMenuItem("Menu");
        menu.add(continueItem);
        menu.add(menuItem);
        optionButton.addActionListener(e ->
                menu.show(
                        optionButton, 0,
                        optionButton.getHeight())
        );

        menuItem.addActionListener(e ->
                returnToMainMenu()
        );
    }

    private void returnToMainMenu() {
        getContentPane().removeAll();
        setSize(450, 355);
        add(new MainMenuPanel(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void updateTurnDisplay() {
        String color;
        if (playerTurn) {
            color = (humanPlayer == PieceType.PLAYER_1) ? "Xanh" : "Đỏ";
        } else {
            color = (aiPlayer == PieceType.PLAYER_1) ? "Xanh" : "Đỏ";
        }
        turnLabel.setText("Lượt: " + color);
    }

    /**
     * 3. LỚP BÀN CỜ GAME CHÍNH (Chứa lưới ô cờ 16x16)
     */
    private class GameBoardPanel extends JPanel {

        public GameBoardPanel() {
            // Sử dụng Grid Layout chia đều 16 dòng, 16 cột
            setLayout(new GridLayout(HalmaBoard.SIZE, HalmaBoard.SIZE));
            CellComponent[][] cells = new CellComponent[HalmaBoard.SIZE][HalmaBoard.SIZE];

            // Vòng lặp tạo ra 256 ô cờ cụ thể lắp vào lưới
            for (int r = 0; r < HalmaBoard.SIZE; r++) {
                for (int c = 0; c < HalmaBoard.SIZE; c++) {
                    cells[r][c] = new CellComponent(r, c);
                    add(cells[r][c]);
                }
            }
        }
    }

    /**
     * 4. LỚP MỘT Ô CỜ CỤ THỂ (Tự định nghĩa cách vẽ nền, vẽ quân cờ, vẽ chấm gợi ý)
     */
    private class CellComponent extends JComponent {
        private final int row, col;

        public CellComponent(int row, int col) {
            this.row = row;
            this.col = col;

            // Click chuột vào từng ô cờ
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleCellClick(row, col);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Nền ô cờ màu xám nhạt
            g2.setColor(new Color(245, 245, 245));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Đường viền kẻ ô màu xám mảnh
            g2.setColor(new Color(210, 210, 210));
            g2.drawRect(0, 0, getWidth(), getHeight());

            // Đánh dấu highlight mờ cho 2 góc chuồng xuất phát/đích
            int sumXY = row + col;
            if (sumXY <= 4) { 
                // Chuồng trên bên trái
                drawCampHighlight(g2, new Color(65, 105, 225, 40)); // Xanh dương mờ
            } else if (sumXY >= 26) { 
                // Chuồng dưới bên phải
                drawCampHighlight(g2, new Color(220, 20, 60, 40)); // Đỏ mờ
            }

            // VẼ QUÂN CỜ
            PieceType piece = logicBoard.getPiece(row, col);
            if (piece != PieceType.EMPTY) {
                int padding = 6; // Khoảng cách từ quân cờ tới viền ô cờ
                int size = Math.min(getWidth(), getHeight()) - (padding * 2);
                Shape circle = new Ellipse2D.Double(padding, padding, size, size);

                if (piece == PieceType.PLAYER_1) {
                    g2.setColor(new Color(30, 144, 255)); // Màu quân Xanh dương của bạn
                } else {
                    g2.setColor(new Color(220, 20, 60));  // Màu quân Đỏ của AI
                }
                g2.fill(circle); // Tô đặc màu hình tròn quân cờ
                
                // Đường viền quanh quân cờ
                g2.setColor(g2.getColor().darker());
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(circle);
            }

            // Highlight nền xanh lá nếu ô cờ này đang được click Chọn
            if (selectedPoint != null && selectedPoint.x() == row && selectedPoint.y() == col) {
                g2.setColor(new Color(0, 255, 0, 80)); // Màu xanh lá trong suốt nhẹ
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            // VẼ CHẤM TRÒN GỢI Ý NƯỚC ĐI CÓ THỂ ĐI ĐƯỢC
            boolean isValidDest = false;
            for (Point p : validDestinations) {
                if (p.x() == row && p.y() == col) {
                    isValidDest = true;
                    break;
                }
            }
            if (isValidDest) {
                g2.setColor(new Color(46, 139, 87, 200)); // Màu chấm xanh lá cây sẫm
                int dotSize = Math.min(getWidth(), getHeight()) / 3; // Kích thước dấu chấm bằng 1/3 ô cờ
                int xOffset = (getWidth() - dotSize) / 2;
                int yOffset = (getHeight() - dotSize) / 2;
                g2.fillOval(xOffset, yOffset, dotSize, dotSize); // Vẽ dấu chấm tròn nhỏ ở chính giữa ô trống
            }

            g2.dispose();
        }

        // Vẽ các góc mờ đánh dấu chuồng
        private void drawCampHighlight(Graphics2D g2, Color color) {
            g2.setColor(color);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * 5. HÀM XỬ LÝ CLICK CHUỘT VÀ ĐIỀU PHỐI LƯỢT CHƠI.
     */
    private void handleCellClick(int row, int col) {
        if (!playerTurn) {
            return;
        }
        // Trường hợp 1: Nếu click thẳng vào một quân cờ của mình -> Chọn quân đó (hoặc Đổi quân chọn)
        if (logicBoard.getPiece(row, col)
                == humanPlayer)
        {
            Point clicked =
                    new Point(row, col);

            if (jumpInProgress)
            {
                if (!clicked.equals(jumpingPiece)) return;
                selectedPoint = clicked;
                validDestinations.clear();
                List<Move> jumps = logicBoard
                                .getSingleJumpMoves(
                                        clicked,
                                        visitedJumpPositions);
                for (Move m : jumps)
                {
                    validDestinations.add(
                            m.to()
                    );
                }
                boardPanel.repaint();
                return;
            }

            selectedPoint = clicked;
            validDestinations.clear();
            List<Move> steps = logicBoard
                    .getSingleStepMoves(
                            clicked);
            List<Move> jumps = logicBoard
                    .getSingleJumpMoves(
                            clicked,
                            new boolean[HalmaBoard.SIZE]
                                    [HalmaBoard.SIZE]
                    );

            for (Move m : steps)
            {
                validDestinations.add(m.to());
            }
            for (Move m : jumps)
            {
                validDestinations.add(m.to());
            }
            boardPanel.repaint();
            return;
        }
        
        // Trường hợp 2: Nếu TRƯỚC ĐÓ ĐÃ CHỌN QUÂN, và click hiện tại là vào một ô trống
        if (selectedPoint != null) {
            Point targetDestination = null;
            // Kiểm tra xem ô click này có nằm trong số các chấm xanh gợi ý không
            for (Point p : validDestinations) {
                if (p.x() == row && p.y() == col) {
                    targetDestination = p;
                    break;
                }
            }

            // Nếu click vào ô chấm xanh gợi ý thì di chuyển quân
            if (targetDestination != null) {
                Point oldPosition = selectedPoint;
                Move playerMove =
                        new Move(
                                oldPosition,
                                targetDestination
                        );
                logicBoard.makeMove(
                        playerMove,
                        humanPlayer
                );

                if (logicBoard.hasWon(humanPlayer)) {

                    String[] options = {
                            "Play Again",
                            "Menu"
                    };

                    int choice =
                            JOptionPane.showOptionDialog(
                                    this,
                                    "YOU WIN!!!",
                                    "Game Over",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    options,
                                    options[0]
                            );

                    if (choice == 0) {
                        startGame();
                    } else {
                        returnToMainMenu();
                    }

                    return;
                }

                boolean isJump =
                        Math.abs(targetDestination.x()
                                        - oldPosition.x()) > 1
                                ||
                                Math.abs(targetDestination.y()
                                        - oldPosition.y()) > 1;
                if (!isJump)
                {
                    finishPlayerTurn();
                    return;
                }
                boolean firstJump = !jumpInProgress;
                jumpInProgress = true;
                jumpingPiece = targetDestination;
                if (firstJump) {
                    visitedJumpPositions =
                            new boolean[
                                    HalmaBoard.SIZE]
                                    [HalmaBoard.SIZE];
                    visitedJumpPositions[oldPosition.x()]
                            [oldPosition.y()] = true;
                }

                selectedPoint = targetDestination;

                visitedJumpPositions[targetDestination.x()]
                        [targetDestination.y()] = true;

                validDestinations.clear();

                List<Move> nextJumps = logicBoard
                        .getSingleJumpMoves(
                                targetDestination,
                                visitedJumpPositions
                        );

                for (Move m : nextJumps)
                {
                    validDestinations.add(m.to());
                }
                if (nextJumps.isEmpty())
                {
                    finishPlayerTurn();
                    return;
                }
                endTurnButton.setEnabled(true);
                boardPanel.repaint();
                return;
            }
        }

        // Trường hợp 3: Click bừa ra ngoài ô trống không có gợi ý -> Hủy chọn quân
        selectedPoint = null;
        validDestinations.clear();
        boardPanel.repaint();
    }

    private void finishPlayerTurn()
    {
        jumpInProgress = false;
        jumpingPiece = null;
        visitedJumpPositions = new boolean[HalmaBoard.SIZE][HalmaBoard.SIZE];
        selectedPoint = null;
        validDestinations.clear();
        endTurnButton.setEnabled(false);
        playerTurn = false;
        updateTurnDisplay();
        boardPanel.repaint();
        runAITurn();
    }

    private void runAITurn()
    {
        new Thread(() -> {
            Move aiMove = ai.findBestMove(logicBoard);
            if (aiMove != null) {
                logicBoard.makeMove(
                        aiMove,
                        aiPlayer);
            }

            SwingUtilities.invokeLater(() -> {
                playerTurn = true;
                updateTurnDisplay();
                boardPanel.repaint();
                if (aiMove != null &&
                        logicBoard.hasWon(aiPlayer))
                {
                    String[] options = {
                            "Play Again",
                            "Menu"
                    };

                    int choice =
                            JOptionPane.showOptionDialog(
                                    HalmaGUI.this,
                                    "AI đã thắng!",
                                    "Game Over",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    options,
                                    options[0]
                            );

                    if (choice == 0) {
                        startGame();
                    } else {
                        returnToMainMenu();
                    }
                }
            });
        }).start();
    }
}