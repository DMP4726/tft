package halma;

public record Point(int x, int y) {
    public boolean isValid() {
        return x >= 0 && x < 16 && y >= 0 && y < 16;
    }
}