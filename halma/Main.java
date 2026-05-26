package halma;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Khởi chạy giao diện đồ họa an toàn trong luồng Event Dispatch Thread của Swing
        SwingUtilities.invokeLater(() -> {
            HalmaGUI game = new HalmaGUI();
            game.setVisible(true);
        });
    }
}