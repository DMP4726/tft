package halma;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class HalmaBoard {
    public static final int SIZE = 16;
    private PieceType[][] grid;

    // 8 hướng di chuyển xung quanh một ô cờ (Lên, xuống, trái, phải và 4 đường chéo)
    private static final int[][] DIRECTIONS = {
        {-1, -1}, {-1, 0}, {-1, 1},
        {0, -1},           {0, 1},
        {1, -1},  {1, 0},  {1, 1}
    };

    // Khởi tạo bàn cờ mới ban đầu
    public HalmaBoard() {
        grid = new PieceType[SIZE][SIZE];
        initializeBoard();
    }

    // Hàm sao chép bàn cờ (Deep Copy) phục vụ cho thuật toán tìm kiếm của AI
    public HalmaBoard(HalmaBoard other) {
        this.grid = new PieceType[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(other.grid[i], 0, this.grid[i], 0, SIZE);
        }
    }

    // Đặt trạng thái bàn cờ ban đầu trống và xếp quân vào chuồng cho 2 bên
    private void initializeBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = PieceType.EMPTY;
            }
        }
        // PLAYER_1 (Xanh dương) xuất phát ở góc trên bên trái (0,0)
        setupCamp(PieceType.PLAYER_1, 0, 0, 1);
        // PLAYER_2 (Đỏ - AI) xuất phát ở góc dưới bên phải (15,15)
        setupCamp(PieceType.PLAYER_2, SIZE - 1, SIZE - 1, -1);
    }

    // Hàm xếp 15 quân cờ vào chuồng hình tam giác cân góc bàn cờ
    private void setupCamp(PieceType player, int startX, int startY, int dir) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i + j <= 4) { // Tạo hình dạng hàng rào chuồng tiêu chuẩn (15 ô)
                    int x = startX + (dir * i);
                    int y = startY + (dir * j);
                    if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
                        grid[x][y] = player;
                    }
                }
            }
        }
    }

    // Lấy loại quân cờ tại một ô cụ thể
    public PieceType getPiece(int x, int y) {
        return grid[x][y];
    }

    // Thực hiện di chuyển quân cờ từ vị trí này sang vị trí khác
    public void makeMove(Move move, PieceType player) {
        grid[move.from().x()][move.from().y()] = PieceType.EMPTY;
        grid[move.to().x()][move.to().y()] = player;
    }

    // Tìm TẤT CẢ các nước đi hợp lệ của một phe trên bàn cờ
    

    // Logic tìm các nước đi bước đơn 1 ô xung quanh
    private void getSingleSteps(Point start, List<Move> moves) {
        for (int[] dir : DIRECTIONS) {
            int nextX = start.x() + dir[0];
            int nextY = start.y() + dir[1];
            Point target = new Point(nextX, nextY);

            if (target.isValid() && grid[nextX][nextY] == PieceType.EMPTY) {
                moves.add(new Move(start, target));
            }
        }
    }

    // Logic dùng thuật toán BFS để quét toàn bộ các cú nhảy liên hoàn tầm xa
    private void getChainJumps(Point start, List<Move> moves) {
        Queue<Point> queue = new LinkedList<>();
        boolean[][] visited = new boolean[SIZE][SIZE];

        queue.add(start);
        visited[start.x()][start.y()] = true;

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            for (int[] dir : DIRECTIONS) {
                int neighborX = current.x() + dir[0];
                int neighborY = current.y() + dir[1];
                Point neighbor = new Point(neighborX, neighborY);

                // Nếu ô cạnh bên có quân cờ (bất kể phe nào) để làm vật cản nhảy qua
                if (neighbor.isValid() && grid[neighborX][neighborY] != PieceType.EMPTY) {
                    int landingX = neighborX + dir[0];
                    int landingY = neighborY + dir[1];
                    Point landing = new Point(landingX, landingY);

                    // Nếu ô đối diện ngay sau vật cản là ô trống thì có thể nhảy đáp xuống
                    if (landing.isValid() && grid[landingX][landingY] == PieceType.EMPTY) {
                        if (!visited[landingX][landingY]) {
                            visited[landingX][landingY] = true;
                            queue.add(landing); // Tiếp tục xếp hàng đợi để quét nhảy liên hoàn từ ô mới này
                            moves.add(new Move(start, landing));
                        }
                    }
                }
            }
        }
    }

    /**
     * KIỂM TRA ĐIỀU KIỆN CHIẾN THẮNG CHẶT CHẼ (Tích hợp luật Anti-Troll)
     */
    public boolean hasWon(PieceType player) {
        PieceType opponent = (player == PieceType.PLAYER_1) ? PieceType.PLAYER_2 : PieceType.PLAYER_1;
        
        int playerInTarget = 0;
        int oppInTarget = 0;
        int playerInOwnCamp = 0;

        // Đếm phân loại vị trí quân cờ hiện tại trên bàn cờ
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (grid[x][y] == player) {
                    if (isInsideTargetCamp(x, y, player)) playerInTarget++;
                    if (isInsideOwnCamp(x, y, player)) playerInOwnCamp++;
                } else if (grid[x][y] == opponent) {
                    if (isInsideTargetCamp(x, y, player)) oppInTarget++;
                }
            }
        }

        // ĐIỀU KIỆN THẮNG 1: Đưa được trọn vẹn cả 15 quân vào chuồng đích thành công
        if (playerInTarget == 15) {
            return true;
        }

        // ĐIỀU KIỆN THẮNG 2 (Chống đổ bê tông): 
        // - Chuồng đích đã bị lấp đầy kín toàn bộ không còn ô trống (Tổng quân ta + quân cản đường của địch = 15)
        // - Bản thân người chơi đã rút hết toàn bộ quân ra khỏi nhà mình (Không giữ quân ở nhà ăn vạ)
        // - Người chơi đã đưa được ít nhất 5 quân sang chuồng đối phương (Để tránh kích hoạt nhầm lúc đầu game)
        if ((playerInTarget + oppInTarget == 15) && playerInOwnCamp == 0 && playerInTarget >= 5) {
            return true;
        }

        return false;
    }

    // Hàm kiểm tra xem tọa độ (x, y) có thuộc phạm vi chuồng ĐÍCH của người chơi không
    private boolean isInsideTargetCamp(int x, int y, PieceType player) {
        if (player == PieceType.PLAYER_1) {
            return (x + y >= 26); // Chuồng đối diện phía dưới bên phải
        } else {
            return (x + y <= 4);  // Chuồng đối diện phía trên bên trái
        }
    }

    // Hàm kiểm tra xem tọa độ (x, y) có thuộc phạm vi chuồng XUẤT PHÁT của người chơi không
    private boolean isInsideOwnCamp(int x, int y, PieceType player) {
        if (player == PieceType.PLAYER_1) {
            return (x + y <= 4);  // Góc trên bên trái
        } else {
            return (x + y >= 26); // Góc dưới bên phải
        }
    }
    
    // TRONG FILE HalmaBoard.java
public List<Move> getAllValidMoves(PieceType player) {
    List<Move> validMoves = new ArrayList<>();
    // Quét toàn bộ bàn cờ tìm quân của người chơi
    for (int x = 0; x < SIZE; x++) {
        for (int y = 0; y < SIZE; y++) {
            if (grid[x][y] == player) {
                Point startPoint = new Point(x, y);
                
                // --- KIỂM TRA DÒNG NÀY ---
                // Nó bắt buộc phải tồn tại và không bị comment (//)
                getSingleSteps(startPoint, validMoves); 
                // -------------------------

                getChainJumps(startPoint, validMoves);
            }
        }
    }
    return validMoves;
}
}