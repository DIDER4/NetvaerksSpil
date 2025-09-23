import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class ShotController {

    private final String[] board;
    private final Label[][] fields;
    private final List<Player> players;
    private final Image[] playerShotAnim;

    private final Image shot_Left, shot_Right, shot_Up, shot_Down;
    private final Image shot_Tail_Horizontal, shot_Tail_Vertical;
    private final Image hero_left, hero_right, hero_up, hero_down;

    private final List<Shot> shots = new ArrayList<>();
    private final List<int[]> tailPositions = new ArrayList<>();
    private final List<int[]> wallHitPositions = new ArrayList<>();
    private final List<Player> animatingPlayers = new ArrayList<>();

    public ShotController(String[] board, Label[][] fields, List<Player> players, Image[] playerShotAnim,
                          Image shot_Left, Image shot_Right, Image shot_Up, Image shot_Down,
                          Image shot_Tail_Horizontal, Image shot_Tail_Vertical,
                          Image hero_left, Image hero_right, Image hero_up, Image hero_down) {
        this.board = board;
        this.fields = fields;
        this.players = players;
        this.playerShotAnim = playerShotAnim;
        this.shot_Left = shot_Left;
        this.shot_Right = shot_Right;
        this.shot_Up = shot_Up;
        this.shot_Down = shot_Down;
        this.shot_Tail_Horizontal = shot_Tail_Horizontal;
        this.shot_Tail_Vertical = shot_Tail_Vertical;
        this.hero_left = hero_left;
        this.hero_right = hero_right;
        this.hero_up = hero_up;
        this.hero_down = hero_down;
    }

    public void fireShot(Player owner) {
        shots.add(new Shot(owner.getXpos(), owner.getYpos(), owner.direction, owner));
    }

    // Kald denne når der modtages et SHOT fra netværket
    public void fireShotFromNetwork(Player owner, int x, int y, String direction) {
        shots.add(new Shot(x, y, direction, owner));
    }


    public void updateShots() {
        List<Shot> toRemove = new ArrayList<>();

        // Fjern tidligere wall-hit grafik
        for (int[] pos : wallHitPositions) {
            fields[pos[0]][pos[1]].setGraphic(new ImageView(GUI.image_floor));
        }
        wallHitPositions.clear();

        // Fjern tidligere haler
        for (int[] pos : tailPositions) {
            fields[pos[0]][pos[1]].setGraphic(new ImageView(GUI.image_floor));
        }
        tailPositions.clear();

        for (Shot s : shots) {
            fields[s.getX()][s.getY()].setGraphic(new ImageView(GUI.image_floor));

            // Tegn hale
            switch (s.getDirection()) {
                case "left":
                case "right":
                    fields[s.getX()][s.getY()].setGraphic(new ImageView(shot_Tail_Horizontal));
                    break;
                case "up":
                case "down":
                    fields[s.getX()][s.getY()].setGraphic(new ImageView(shot_Tail_Vertical));
                    break;
            }
            tailPositions.add(new int[]{s.getX(), s.getY()});

            s.move();

            // Rammer væg?
            if (board[s.getY()].charAt(s.getX()) == 'w') {
                int prevX = s.getX(), prevY = s.getY();
                switch (s.getDirection()) {
                    case "left":  prevX += 1; break;
                    case "right": prevX -= 1; break;
                    case "up":    prevY += 1; break;
                    case "down":  prevY -= 1; break;
                }

                String wallImage = switch (s.getDirection()) {
                    case "left" -> "Image/fireWallWest.png";
                    case "right" -> "Image/fireWallEast.png";
                    case "up" -> "Image/fireWallNorth.png";
                    case "down" -> "Image/fireWallSouth.png";
                    default -> null;
                };

                if (wallImage != null) {
                    fields[prevX][prevY].setGraphic(new ImageView(
                            new Image(getClass().getResourceAsStream(wallImage), GUI.size, GUI.size, false, false)));
                    wallHitPositions.add(new int[]{prevX, prevY});
                }

                toRemove.add(s);
                continue;
            }

            // Rammer spiller?
            Player p = getPlayerAt(s.getX(), s.getY());
            if (p != null && p != s.getOwner()) {
                s.getOwner().addPoints(20);
                p.addPoints(-20);

                int px = p.getXpos();
                int py = p.getYpos();

                animatingPlayers.add(p);

                Timeline anim = new Timeline();
                for (int i = 0; i < playerShotAnim.length; i++) {
                    final int frame = i;
                    anim.getKeyFrames().add(new KeyFrame(Duration.millis(50 * i), e -> {
                        fields[px][py].setGraphic(new ImageView(playerShotAnim[frame]));
                    }));
                }

                anim.getKeyFrames().add(new KeyFrame(Duration.millis(50 * playerShotAnim.length), e -> {
                    List<int[]> emptyFields = new ArrayList<>();
                    for (int i = 0; i < 20; i++) {
                        for (int j = 0; j < 20; j++) {
                            if (board[j].charAt(i) == ' ' && getPlayerAt(i, j) == null) {
                                emptyFields.add(new int[]{i, j});
                            }
                        }
                    }

                    if (!emptyFields.isEmpty()) {
                        int[] pos2 = emptyFields.get((int) (Math.random() * emptyFields.size()));
                        p.setXpos(pos2[0]);
                        p.setYpos(pos2[1]);
                        Image heroImg = switch (p.direction) {
                            case "right" -> hero_right;
                            case "left" -> hero_left;
                            case "up" -> hero_up;
                            case "down" -> hero_down;
                            default -> hero_up;
                        };
                        fields[pos2[0]][pos2[1]].setGraphic(new ImageView(heroImg));
                    }
                    animatingPlayers.remove(p);
                }));
                anim.play();
                toRemove.add(s);
                continue;
            }

            // Tegn skud
            switch (s.getDirection()) {
                case "left":
                    fields[s.getX()][s.getY()].setGraphic(new ImageView(shot_Left));
                    break;
                case "right":
                    fields[s.getX()][s.getY()].setGraphic(new ImageView(shot_Right));
                    break;
                case "up":
                    fields[s.getX()][s.getY()].setGraphic(new ImageView(shot_Up));
                    break;
                case "down":
                    fields[s.getX()][s.getY()].setGraphic(new ImageView(shot_Down));
                    break;
            }
        }

        shots.removeAll(toRemove);

        // Gentegn spillere
        for (Player pl : players) {
            if (!animatingPlayers.contains(pl)) {
                Image heroImg = switch (pl.direction) {
                    case "right" -> hero_right;
                    case "left" -> hero_left;
                    case "up" -> hero_up;
                    case "down" -> hero_down;
                    default -> hero_up;
                };
                fields[pl.getXpos()][pl.getYpos()].setGraphic(new ImageView(heroImg));
            }
        }

        GUI.scoreList.setText(GUI.getScoreList());
    }

    private Player getPlayerAt(int x, int y) {
        for (Player p : players) {
            if (p.getXpos() == x && p.getYpos() == y) {
                return p;
            }
        }
        return null;
    }
}

// Unødvendig committe-kode2