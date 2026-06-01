package halma;

import java.util.ArrayList;
import java.util.List;

public class HalmaAI {
    private final int maxDepth; // Cấu hình tối ưu độ sâu suy nghĩ
    private final PieceType aiPlayer;
    private final PieceType opponent;
    
    private final Point aiTarget;
    private final Point oppTarget;

    public HalmaAI(PieceType aiPlayer, int maxDepth) {
        this.aiPlayer = aiPlayer;
        this.maxDepth = maxDepth;

        this.opponent =
                (aiPlayer == PieceType.PLAYER_1)
                        ? PieceType.PLAYER_2
                        : PieceType.PLAYER_1;

        this.aiTarget =
                (aiPlayer == PieceType.PLAYER_1)
                        ? new Point(15, 15)
                        : new Point(0, 0);

        this.oppTarget =
                (opponent == PieceType.PLAYER_1)
                        ? new Point(15, 15)
                        : new Point(0, 0);
    }

    public Move findBestMove(HalmaBoard board) {
        int bestValue = Integer.MIN_VALUE;
        Move bestMove = null;
        
        List<Move> moves = board.getAllValidMoves(aiPlayer);
        sortMoves(moves, aiTarget);

        for (Move move : moves) {
            HalmaBoard simulatedBoard = new HalmaBoard(board);
            simulatedBoard.makeMove(move, aiPlayer);
            if (simulatedBoard.hasWon(aiPlayer)) {
                return move;
            }
            int moveValue =
                    minimax(simulatedBoard,
                            maxDepth - 1,
                            Integer.MIN_VALUE,
                            Integer.MAX_VALUE,
                            false);
            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private int minimax(HalmaBoard board, int depth, int alpha, int beta, boolean isMaximizing) {
        if (board.hasWon(aiPlayer)) {
            return 1_000_000;
        }
        if (board.hasWon(opponent)) {
            return -1_000_000;
        }
        if (depth == 0) {
            return evaluateBoard(board);
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            List<Move> moves = board.getAllValidMoves(aiPlayer);
            sortMoves(moves, aiTarget);

            for (Move move : moves) {
                HalmaBoard sim = new HalmaBoard(board);
                sim.makeMove(move, aiPlayer);
                int eval = minimax(sim, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            List<Move> moves = board.getAllValidMoves(opponent);
            sortMoves(moves, oppTarget);

            for (Move move : moves) {
                HalmaBoard sim = new HalmaBoard(board);
                sim.makeMove(move, opponent);
                int eval = minimax(sim, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

   private int evaluateBoard(HalmaBoard board) {
        int score = 0;
        List<Point> aiPieces = new ArrayList<>();
        List<Point> oppPieces = new ArrayList<>();

        for (int i = 0; i < HalmaBoard.SIZE; i++) {
            for (int j = 0; j < HalmaBoard.SIZE; j++) {
                PieceType piece = board.getPiece(i, j);
                if (piece == aiPlayer) aiPieces.add(new Point(i, j));
                else if (piece == opponent) oppPieces.add(new Point(i, j));
            }
        }

        // 1. TÍNH ĐIỂM CHO QUÂN AI (Ưu tiên Tấn Công - Chạy đua)
        Point aiCenterOfMass = calculateCenterOfMass(aiPieces);
        for (Point p : aiPieces) {
            score -= calculateDistance(p, aiTarget) * 20;
            if (isInOwnCamp(p, aiPlayer)) {
                score -= 200;
            }
            if (isInTargetCamp(p, aiPlayer)) {
                score += 500;
            }
            if (aiCenterOfMass != null) {
                score -= calculateDistance(p, aiCenterOfMass) * 2;
            }
        }

        // 2. TÍNH ĐIỂM CHO QUÂN ĐỐI PHƯƠNG (Giảm trọng số Phòng Thủ - Bớt lo chuyện bao đồng)
        Point oppCenterOfMass = calculateCenterOfMass(oppPieces);
        for (Point p : oppPieces) {
            score += calculateDistance(p, oppTarget) * 5;
            
            if (isInOwnCamp(p, opponent)) {
                score += 10;
            }
            if (isInTargetCamp(p, opponent)) {
                score -= 50;
            }
            
            if (oppCenterOfMass != null) {
                score += calculateDistance(p, oppCenterOfMass);
            }
        }
        return score;
    }
    private int calculateDistance(Point p, Point target) {
        return Math.abs(p.x() - target.x()) + Math.abs(p.y() - target.y());
    }

    private Point calculateCenterOfMass(List<Point> pieces) {
        if (pieces.isEmpty()) return null;
        int sumX = 0, sumY = 0;
        for (Point p : pieces) {
            sumX += p.x();
            sumY += p.y();
        }
        return new Point(sumX / pieces.size(), sumY / pieces.size());
    }

    private boolean isInOwnCamp(Point p, PieceType player) {
        if (player == PieceType.PLAYER_1) return (p.x() + p.y() <= 4);
        return (p.x() + p.y() >= 26);
    }

    private boolean isInTargetCamp(Point p, PieceType player) {
        if (player == PieceType.PLAYER_1) return (p.x() + p.y() >= 26);
        return (p.x() + p.y() <= 4);
    }

    private void sortMoves(List<Move> moves, Point target) {
        moves.sort((m1, m2) -> {
            int dist1 = calculateDistance(m1.to(), target) - calculateDistance(m1.from(), target);
            int dist2 = calculateDistance(m2.to(), target) - calculateDistance(m2.from(), target);
            return Integer.compare(dist1, dist2);
        });
    }
}