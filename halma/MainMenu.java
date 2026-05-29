package halma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JFrame {

    public MainMenu() {
        // Cài đặt cửa sổ Menu
        setTitle("Halma 16x16 - Main Menu");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Hiển thị ở giữa màn hình
        setLayout(new GridLayout(4, 1, 10, 10)); // Chia làm 4 hàng dọc
        getContentPane().setBackground(new Color(240, 248, 255)); // Màu nền xanh nhạt

        // 1. Tiêu đề Game
        JLabel titleLabel = new JLabel("GAME HALMA 16x16", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 51, 102));
        add(titleLabel);

        // 2. Cài đặt Độ khó AI (Tùy chọn)
        JPanel settingsPanel = new JPanel();
        settingsPanel.setOpaque(false);

        settingsPanel.add(new JLabel("Độ khó AI: "));

        String[] difficulties = {
                "Dễ",
                "Trung bình",
                "Khó"
        };

        JComboBox<String> difficultyBox =
                new JComboBox<>(difficulties);

        difficultyBox.setSelectedIndex(1);

        settingsPanel.add(difficultyBox);

        add(settingsPanel);

        // 3. Nút PLAY GAME
        JPanel playPanel = new JPanel();
        playPanel.setOpaque(false);
        JButton playButton = new JButton("▶ PLAY GAME");
        playButton.setFont(new Font("Arial", Font.BOLD, 20));
        playButton.setBackground(new Color(50, 205, 50)); // Nút màu xanh lá
        playButton.setForeground(Color.WHITE);
        playButton.setFocusPainted(false);
        playButton.setPreferredSize(new Dimension(200, 50));
        
        // Sự kiện khi bấm nút PLAY
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Lấy độ khó người dùng vừa chọn (1, 2, hoặc 3)
                int aiDepth = difficultyBox.getSelectedIndex() + 1;
                
                // Đóng cửa sổ Menu
                dispose(); 
                
                // MỞ CỬA SỔ GAME CHÍNH Ở ĐÂY
                startGame(aiDepth);
            }
        });
        playPanel.add(playButton);
        add(playPanel);

        // 4. Tác giả / Info (Trang trí thêm cho đỡ trống)
        JLabel footerLabel = new JLabel("Chúc bạn chơi vui vẻ!", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerLabel.setForeground(Color.GRAY);
        add(footerLabel);
    }

    // Hàm gọi cửa sổ Game chính
    private void startGame(int aiDepth) {
        SwingUtilities.invokeLater(() -> {
            /* * QUAN TRỌNG: 
             * Dưới đây là ví dụ gọi class Game của bạn.
             * Bạn hãy thay đổi tên "HalmaGUI" thành tên class chứa giao diện Bàn Cờ thực tế của bạn nhé!
             */
            
            // Ví dụ: HalmaGUI gameWindow = new HalmaGUI();
            // gameWindow.setVisible(true);
            
          
            
            // 2. Tạo cửa sổ giao diện bàn cờ 
            // Bạn hãy điền CHÍNH XÁC tên Class file bàn cờ của bạn vào đây thay cho HalmaGUI nhé!
            HalmaGUI gameWindow = new HalmaGUI(); 
            
            // 3. Nếu class AI của bạn cần nhận độ khó từ Menu, hãy truyền vào đây:
            // HalmaAI ai = new HalmaAI(aiDepth);
            
            // 4. Hiển thị cửa sổ lên màn hình
            gameWindow.setVisible(true);
            
            // 5. Ép giao diện vẽ lại toàn bộ quân cờ (Quan trọng để tránh bị trống trơn)
            gameWindow.repaint();        
        });
    }
}