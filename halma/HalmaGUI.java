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
    private Point selectedPoint = null;
    // THÊM DÒNG NÀY VÀO: Lưu sẵn toàn bộ nước đi hợp lệ của người chơi để dùng luôn không cần tính lại
    private List<Move> allPlayerMoves = new ArrayList<>();
    // Danh sách lưu các ô đích hợp lệ để vẽ dấu chấm gợi ý
    private List<Point> validDestinations = new ArrayList<>();

    public HalmaGUI() {
        setTitle("Halma 16x16 - Trò chơi cờ Halma");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 355); // Kích thước vừa vặn cho Menu lúc đầu
        setLocationRelativeTo(null); // Hiển thị ở chính giữa màn hình
        
        // BẮT ĐẦU: Hiển thị giao diện Menu chính
        add(new MainMenuPanel(), BorderLayout.CENTER);
        setVisible(true);
    }

    /**
     * 1. LỚP MAIN MENU PANEL (Giao diện Menu trước khi vào game)
     */
    private class MainMenuPanel extends JPanel {
        public MainMenuPanel() {
            setLayout(new GridLayout(4, 1, 10, 10)); // Chia làm 4 hàng dọc cân đối
            setBackground(new Color(240, 248, 255)); // Màu nền xanh nhạt dễ chịu

            // Hàng 1: Tiêu đề Game lớn
            JLabel titleLabel = new JLabel("GAME HALMA 16x16", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
            titleLabel.setForeground(new Color(0, 51, 102));
            add(titleLabel);

            // Hàng 2: Cài đặt chế độ chơi (Bạn đi trước đấu với AI)
            JPanel settingsPanel = new JPanel();
            settingsPanel.setOpaque(false);
            JLabel infoMatchLabel = new JLabel("Chế độ chơi: Bạn (Xanh dương) 🆚 AI (Đỏ)");
            infoMatchLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            settingsPanel.add(infoMatchLabel);
            add(settingsPanel);

            // Hàng 3: Nút bấm PLAY GAME để vào bàn cờ
            JPanel playPanel = new JPanel();
            playPanel.setOpaque(false);
            JButton playButton = new JButton("▶ PLAY GAME");
            playButton.setFont(new Font("Arial", Font.BOLD, 20));
            playButton.setBackground(new Color(50, 205, 50)); // Nút màu xanh lá nổi bật
            playButton.setForeground(Color.WHITE);
            playButton.setFocusPainted(false);
            playButton.setPreferredSize(new Dimension(220, 50));
            
            // Sự kiện click nút Play
            playButton.addActionListener(e -> {
                startGame(); // Chuyển sang màn hình bàn cờ
            });
            playPanel.add(playButton);
            add(playPanel);

            // Hàng 4: Dòng chữ thông tin chân trang
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
        setSize(850, 850); 
        setLocationRelativeTo(null); // Căn giữa lại màn hình sau khi đổi size

        // Khởi tạo bảng logic chứa luật chơi và vị trí quân cờ ban đầu
        logicBoard = new HalmaBoard();

        allPlayerMoves = logicBoard.getAllValidMoves(PieceType.PLAYER_1);
        // Khởi tạo bảng giao diện đồ họa bàn cờ
        boardPanel = new GameBoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // Làm mới và vẽ lại toàn bộ cửa sổ JFrame
        revalidate();
        repaint();
    }

    /**
     * 3. LỚP BÀN CỜ GAME CHÍNH (Chứa lưới ô cờ 16x16)
     */
    private class GameBoardPanel extends JPanel {
        private CellComponent[][] cells;

        public GameBoardPanel() {
            // Sử dụng Grid Layout chia đều 16 dòng, 16 cột
            setLayout(new GridLayout(HalmaBoard.SIZE, HalmaBoard.SIZE));
            cells = new CellComponent[HalmaBoard.SIZE][HalmaBoard.SIZE];

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
        private int row, col;

        public CellComponent(int row, int col) {
            this.row = row;
            this.col = col;

            // Đăng ký sự kiện click chuột vào từng ô cờ đơn lẻ
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
            // Bật chế độ chống răng cưa giúp hình vẽ quân cờ tròn mượt, không bị gai viền
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Bước A: Vẽ nền ô cờ màu xám nhạt
            g2.setColor(new Color(245, 245, 245));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Bước B: Vẽ đường viền kẻ ô màu xám mảnh
            g2.setColor(new Color(210, 210, 210));
            g2.drawRect(0, 0, getWidth(), getHeight());

            // Bước C: Đánh dấu highlight mờ cho 2 góc chuồng xuất phát/đích
            int sumXY = row + col;
            if (sumXY <= 4) { 
                // Chuồng trên bên trái (Góc đích của Đỏ / Góc xuất phát của bạn nếu đổi bên)
                drawCampHighlight(g2, new Color(65, 105, 225, 40)); // Xanh dương mờ
            } else if (sumXY >= 26) { 
                // Chuồng dưới bên phải (Góc xuất phát của Đỏ)
                drawCampHighlight(g2, new Color(220, 20, 60, 40)); // Đỏ mờ
            }

            // Bước D: VẼ QUÂN CỜ HÌNH TRÒN ĐẬM (SOLID) ĐẸP MẮT
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
                
                // Vẽ thêm một đường viền đậm hơn một chút quanh quân cờ trông cho sắc nét
                g2.setColor(g2.getColor().darker());
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(circle);
            }

            // Bước E: Vẽ Highlight nền xanh lá nếu ô cờ này đang được Người chơi click Chọn
            if (selectedPoint != null && selectedPoint.x() == row && selectedPoint.y() == col) {
                g2.setColor(new Color(0, 255, 0, 80)); // Màu xanh lá trong suốt nhẹ
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            // Bước F: VẼ CHẤM TRÒN GỢI Ý NƯỚC ĐI CÓ THỂ ĐI ĐƯỢC
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

        // Hàm phụ trợ vẽ các góc mờ đánh dấu chuồng
        private void drawCampHighlight(Graphics2D g2, Color color) {
            g2.setColor(color);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * 5. HÀM XỬ LÝ CLICK CHUỘT VÀ ĐIỀU PHỐI LƯỢT CHƠI (BẠN -> AI)
     */
    private void handleCellClick(int row, int col) {
        // Trường hợp 1: Nếu click thẳng vào một quân cờ của mình -> Chọn quân đó (hoặc Đổi quân chọn)
        if (logicBoard.getPiece(row, col) == PieceType.PLAYER_1) {
            selectedPoint = new Point(row, col);
            validDestinations.clear(); 

            // Quét danh sách tính sẵn để nạp các ô chấm xanh gợi ý
            for (Move m : allPlayerMoves) {
                if (m.from().x() == selectedPoint.x() && m.from().y() == selectedPoint.y()) {
                    validDestinations.add(m.to()); 
                }
            }
            
            boardPanel.repaint(); 
            return; // Chọn quân xong thì dừng, đợi click tiếp theo
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

            // Nếu ĐÚNG là click trúng vào ô chấm xanh gợi ý -> Tiến hành di chuyển ngay!
            if (targetDestination != null) {
                Move playerMove = new Move(selectedPoint, targetDestination);
                logicBoard.makeMove(playerMove, PieceType.PLAYER_1); 

                // Đi xong thì xóa trạng thái chọn để chuẩn bị lượt mới
                selectedPoint = null;
                validDestinations.clear();
                boardPanel.repaint(); // Quân cờ của bạn nhảy tới ô mới lập tức!

                if (logicBoard.hasWon(PieceType.PLAYER_1)) {
                    JOptionPane.showMessageDialog(this, "Chúc mừng! Bạn đã CHIẾN THẮNG! 🎉");
                    return;
                }

                // --- LƯỢT TỰ ĐỘNG CỦA AI ĐỎ (CHẠY NGẦM) ---
                new Thread(() -> {
                    HalmaAI ai = new HalmaAI(PieceType.PLAYER_2);
                    Move aiMove = ai.findBestMove(logicBoard);
                    
                    if (aiMove != null) {
                        logicBoard.makeMove(aiMove, PieceType.PLAYER_2); 
                    }
                    
                    // Tính sẵn nước đi cho lượt sau của bạn trong lúc AI nghỉ
                    List<Move> nextPlayerMoves = logicBoard.getAllValidMoves(PieceType.PLAYER_1);

                    // Đẩy dữ liệu về luồng vẽ màn hình
                    SwingUtilities.invokeLater(() -> {
                        allPlayerMoves = nextPlayerMoves; 
                        boardPanel.repaint(); // Vẽ quân AI lên màn hình

                        if (aiMove != null && logicBoard.hasWon(PieceType.PLAYER_2)) {
                            JOptionPane.showMessageDialog(HalmaGUI.this, "AI Đỏ đã lấp đầy chuồng! BẠN ĐÃ THUA CUỘC! 🤖");
                        }
                    });
                }).start();
                
                return; // Di chuyển xong thì thoát hàm
            }
        }

        // Trường hợp 3: Click bừa ra ngoài ô trống không có gợi ý -> Hủy chọn quân
        selectedPoint = null;
        validDestinations.clear();
        boardPanel.repaint();
    }

    /**
     * 6. HÀM MAIN - ĐIỂM KÍCH HOẠT CHẠY GAME
     */
    public static void main(String[] args) {
        // Cài đặt giao diện cửa sổ theo chuẩn hệ điều hành cho hiện đại, đẹp mắt
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tạo tiến trình an toàn khởi động GUI
        SwingUtilities.invokeLater(() -> {
            new HalmaGUI();
        });
    }
}