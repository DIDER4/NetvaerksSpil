public class Shot {
    private int x;
    private int y;
    private String direction;
    private Player owner;

    public Shot(int x, int y, String direction, Player owner) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.owner = owner;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public String getDirection() { return direction; }
    public Player getOwner() { return owner; }

    public void move() {
        switch (direction) {
            case "up":    y--; break;
            case "down":  y++; break;
            case "left":  x--; break;
            case "right": x++; break;
        }
    }
}
// Ekstra kode til commit
