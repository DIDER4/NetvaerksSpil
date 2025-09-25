

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class ShotController {
    private final List<Shot> shots;
    private final List<int[]> tailPositions;
    private final List<int[]> wallHitPositions;
    private final List<Player> players;
    private final Label[][] fields;
    private final String[] board;
    private final List<Player> animatingPlayers;
    private final Image[] playerShotAnim;
    private final Image hero_right, hero_left, hero_up, hero_down;
    private final Image shot_Right, shot_Left, shot_Up, shot_Down;
    private final Image shot_Tail_Horizontal, shot_Tail_Vertical;

    public ShotController(List<Shot> shots, List<int[]> tailPositions, List<int[]> wallHitPositions,
                          List<Player> players, Label[][] fields, String[] board, List<Player> animatingPlayers,
                          Image[] playerShotAnim, Image hero_right, Image hero_left, Image hero_up, Image hero_down,
                          Image shot_Right, Image shot_Left, Image shot_Up, Image shot_Down,
                          Image shot_Tail_Horizontal, Image shot_Tail_Vertical) {
        this.shots = shots;
        this.tailPositions = tailPositions;
        this.wallHitPositions = wallHitPositions;
        this.players = players;
        this.fields = fields;
        this.board = board;
        this.animatingPlayers = animatingPlayers;
        this.playerShotAnim = playerShotAnim;
        this.hero_right = hero_right;
        this.hero_left = hero_left;
        this.hero_up = hero_up;
        this.hero_down = hero_down;
        this.shot_Right = shot_Right;
        this.shot_Left = shot_Left;
        this.shot_Up = shot_Up;
        this.shot_Down = shot_Down;
        this.shot_Tail_Horizontal = shot_Tail_Horizontal;
        this.shot_Tail_Vertical = shot_Tail_Vertical;
    }

    public void updateShots(GUI gui) {
        List<Shot> toRemove = new ArrayList<>();

        for (int[] pos : wallHitPositions) {
            fields[pos[0]][pos[1]].setGraphic(new ImageView(gui.image_floor));
        }
        wallHitPositions.clear();

        for (int[] pos : tailPositions) {
            fields[pos[0]][pos[1]].setGraphic(new ImageView(gui.image_floor));
        }
        tailPositions.clear();

        for (Shot s : shots) {
            fields[s.getX()][s.getY()].setGraphic(new ImageView(gui.image_floor));
            switch (s.getDirection()) {
                case "left", "right" -> fields[s.getX()][s.getY()].setGraphic(new ImageView(shot_Tail_Horizontal));
                case "up", "down" -> fields[s.getX()][s.getY()].setGraphic(new ImageView(shot_Tail_Vertical));
            }
            tailPositions.add(new int[]{s.getX(), s.getY()});
            s.move();

            if (board[s.getY()].charAt(s.getX()) == 'w') {
                int prevX = s.getX(), prevY = s.getY();
                switch (s.getDirection()) {
                    case "left" -> prevX += 1;
                    case "right" -> prevX -= 1;
                    case "up" -> prevY += 1;
                    case "down" -> prevY -= 1;
                }
                switch (s.getDirection()) {
                    case "left" -> fields[prevX][prevY].setGraphic(new ImageView(
                            new Image(getClass().getResourceAsStream("Image/fireWallWest.png"), GUI.size, GUI.size, false, false)));
                    case "right" -> fields[prevX][prevY].setGraphic(new ImageView(
                            new Image(getClass().getResourceAsStream("Image/fireWallEast.png"), GUI.size, GUI.size, false, false)));
                    case "up" -> fields[prevX][prevY].setGraphic(new ImageView(
                            new Image(getClass().getResourceAsStream("Image/fireWallNorth.png"), GUI.size, GUI.size, false, false)));
                    case "down" -> fields[prevX][prevY].setGraphic(new ImageView(
                            new Image(getClass().getResourceAsStream("Image/fireWallSouth.png"), GUI.size, GUI.size, false, false)));
                }
                wallHitPositions.add(new int[]{prevX, prevY});
                toRemove.add(s);
                continue;
            }

            Player p = gui.getPlayerAt(s.getX(), s.getY());
            if (p != null && p != s.getOwner()) {
                s.getOwner().addPoints(20);
                p.addPoints(-20);
                int px = p.getXpos(), py = p.getYpos();
                animatingPlayers.add(p);

                Timeline anim = new Timeline();
                for (int i = 0; i < playerShotAnim.length; i++) {
                    final int frame = i;
                    anim.getKeyFrames().add(new KeyFrame(Duration.millis(50 * i), e -> {
                        fields[px][py].setGraphic(new ImageView(playerShotAnim[frame]));
                    }));
                }
                anim.getKeyFrames().add(new KeyFrame(Duration.millis(50 * playerShotAnim.length), e -> {
                    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(0));
                    pause.setOnFinished(ev -> {
                        List<int[]> emptyFields = new ArrayList<>();
                        for (int i = 0; i < 20; i++) {
                            for (int j = 0; j < 20; j++) {
                                if (board[j].charAt(i) == ' ' && gui.getPlayerAt(i, j) == null) {
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
                    });
                    pause.play();
                }));
                anim.play();
                toRemove.add(s);
            }

            switch (s.getDirection()) {
                case "left" -> fields[s.getX()][s.getY()].setGraphic(new ImageView(shot_Left));
                case "right" -> fields[s.getX()][s.getY()].setGraphic(new ImageView(shot_Right));
                case "up" -> fields[s.getX()][s.getY()].setGraphic(new ImageView(shot_Up));
                case "down" -> fields[s.getX()][s.getY()].setGraphic(new ImageView(shot_Down));
            }
        }

        shots.removeAll(toRemove);

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
}