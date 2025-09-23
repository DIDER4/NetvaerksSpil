import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.TextInputDialog;

public class GUI extends Application {

    //Netværks opsætningen
    private static DataOutputStream outToServer;
    private static Socket clientSocket;

    public static final int size = 20;
    public static final int scene_height = size * 20 + 100;
    public static final int scene_width = size * 20 + 200;

    public static Image image_floor;
    public static Image image_wall;
    public static Image hero_right, hero_left, hero_up, hero_down;

    private static ShotController shotController;

    public static Image shot_Right, shot_Left, shot_Up, shot_Down;

    public static Image shot_Tail_Horizontal;
    public static Image shot_Tail_Vertical;

    public static Image[] playerShotAnim = new Image[5];

    public static Player me;
    public static List<Player> players = new ArrayList<Player>();

    public static Label[][] fields;
    public static TextArea scoreList;

    private String[] board = {    // 20x20
            "wwwwwwwwwwwwwwwwwwww",
            "w        ww        w",
            "w w  w  www w  w  ww",
            "w w  w   ww w  w  ww",
            "w  w               w",
            "w w w w w w w  w  ww",
            "w w     www w  w  ww",
            "w w     w w w  w  ww",
            "w   w w  w  w  w   w",
            "w     w  w  w  w   w",
            "w ww ww        w  ww",
            "w  w w    w    w  ww",
            "w        ww w  w  ww",
            "w         w w  w  ww",
            "w        w     w  ww",
            "w  w              ww",
            "w  w www  w w  ww ww",
            "w w      ww w     ww",
            "w   w   ww  w      w",
            "wwwwwwwwwwwwwwwwwwww"
    };


    // -------------------------------------------
    // | Maze: (0,0)              | Score: (1,0) |
    // |-----------------------------------------|
    // | boardGrid (0,1)          | scorelist    |
    // |                          | (1,1)        |
    // -------------------------------------------

    @Override
    public void start(Stage primaryStage) {
        try {
            multiPlayerOpsætning();
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(0, 10, 0, 10));

            Text mazeLabel = new Text("Maze:");
            mazeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

            Text scoreLabel = new Text("Score:");
            scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

            scoreList = new TextArea();

            GridPane boardGrid = new GridPane();

            image_wall = new Image(getClass().getResourceAsStream("Image/wall4.png"), size, size, false, false);
            image_floor = new Image(getClass().getResourceAsStream("Image/floor1.png"), size, size, false, false);

            hero_right = new Image(getClass().getResourceAsStream("Image/heroRight.png"), size, size, false, false);
            hero_left = new Image(getClass().getResourceAsStream("Image/heroLeft.png"), size, size, false, false);
            hero_up = new Image(getClass().getResourceAsStream("Image/heroUp.png"), size, size, false, false);
            hero_down = new Image(getClass().getResourceAsStream("Image/heroDown.png"), size, size, false, false);

            shot_Right = new Image(getClass().getResourceAsStream("Image/fireRight.png"), size, size, false, false);
            shot_Left = new Image(getClass().getResourceAsStream("Image/fireLeft.png"), size, size, false, false);
            shot_Up = new Image(getClass().getResourceAsStream("Image/fireUp.png"), size, size, false, false);
            shot_Down = new Image(getClass().getResourceAsStream("Image/fireDown.png"), size, size, false, false);

            shot_Tail_Horizontal = new Image(getClass().getResourceAsStream("Image/fireHorizontal.png"), size, size, false, false);
            shot_Tail_Vertical = new Image(getClass().getResourceAsStream("Image/fireVertical.png"), size, size, false, false);

            fields = new Label[20][20];
            for (int j = 0; j < 20; j++) {
                for (int i = 0; i < 20; i++) {
                    switch (board[j].charAt(i)) {
                        case 'w':
                            fields[i][j] = new Label("", new ImageView(image_wall));
                            break;
                        case ' ':
                            fields[i][j] = new Label("", new ImageView(image_floor));
                            break;
                        default:
                            throw new Exception("Illegal field value: " + board[j].charAt(i));
                    }
                    boardGrid.add(fields[i][j], i, j);
                }
            }
            scoreList.setEditable(false);


            grid.add(mazeLabel, 0, 0);
            grid.add(scoreLabel, 1, 0);
            grid.add(boardGrid, 0, 1);
            grid.add(scoreList, 1, 1);

            Scene scene = new Scene(grid, scene_width, scene_height);
            primaryStage.setScene(scene);
            primaryStage.show();

            // Prompt for player name
            TextInputDialog dialog = new TextInputDialog("Player");
            dialog.setTitle("Choose Name");
            dialog.setHeaderText("Enter your player name:");
            String playerName = dialog.showAndWait().orElse("Player");

            // Spawn local player at random empty position
            int[] pos = getRandomEmptyPosition();
            me = new Player(playerName, pos[0], pos[1], "up");
            players.add(me);
            fields[pos[0]][pos[1]].setGraphic(new ImageView(hero_up));
            scoreList.setText(getScoreList());

            // Announce to server
            outToServer.writeBytes("MOVE " + me.name + " " + me.getXpos() + " " + me.getYpos() + " " + me.getDirection() + "\n");
            outToServer.writeBytes("POINT " + me.name + " " + me.point + "\n");

            shotController = new ShotController(
                    board, fields, players, playerShotAnim,
                    shot_Left, shot_Right, shot_Up, shot_Down,
                    shot_Tail_Horizontal, shot_Tail_Vertical,
                    hero_left, hero_right, hero_up, hero_down
            );

            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                switch (event.getCode()) {
                    case UP:
                        playerMoved(0, -1, "up");
                        break;
                    case DOWN:
                        playerMoved(0, +1, "down");
                        break;
                    case LEFT:
                        playerMoved(-1, 0, "left");
                        break;
                    case RIGHT:
                        playerMoved(+1, 0, "right");
                        break;
                    case SPACE:
                        shotController.fireShot(me);
                        try {
                            outToServer.writeBytes("SHOT " + me.name + " " + me.getXpos() + " " + me.getYpos() + " " + me.getDirection() + "\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    default:
                        break;
                }
            });

            // Removed hardcoded default players
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Animation-initializering som felter
        for (int i = 0; i < 5; i++) {
            playerShotAnim[i] = new Image(getClass().getResourceAsStream("Image/playerShot" + (i + 1) + ".png"), size, size, false, false);
        }



        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                shotController.updateShots();
            }
        };

        timer.start();
    }

    public void playerMoved(int delta_x, int delta_y, String direction) {
        me.direction = direction;
        int x = me.getXpos(), y = me.getYpos();
        boolean moved = false;
        Player collidedPlayer = null;
        if (board[y + delta_y].charAt(x + delta_x) == 'w') {
            me.addPoints(-1);
        } else {
            Player p = getPlayerAt(x + delta_x, y + delta_y);
            if (p != null) {
                me.addPoints(10);
                p.addPoints(-10);
                collidedPlayer = p;
            } else {
                me.addPoints(1);
                fields[x][y].setGraphic(new ImageView(image_floor));
                x += delta_x;
                y += delta_y;
                if (direction.equals("right")) {
                    fields[x][y].setGraphic(new ImageView(hero_right));
                }
                ;
                if (direction.equals("left")) {
                    fields[x][y].setGraphic(new ImageView(hero_left));
                }
                ;
                if (direction.equals("up")) {
                    fields[x][y].setGraphic(new ImageView(hero_up));
                }
                ;
                if (direction.equals("down")) {
                    fields[x][y].setGraphic(new ImageView(hero_down));
                }
                ;
                me.setXpos(x);
                me.setYpos(y);
                moved = true;
            }
        }
        scoreList.setText(getScoreList());
        try {
            // Send MOVE message for myself
            outToServer.writeBytes("MOVE " + me.name + " " + me.getXpos() + " " + me.getYpos() + " " + me.getDirection() + "\n");
            // Send POINT message for myself
            outToServer.writeBytes("POINT " + me.name + " " + me.point + "\n");
            // If collision, send POINT for the other player
            if (collidedPlayer != null) {
                outToServer.writeBytes("POINT " + collidedPlayer.name + " " + collidedPlayer.point + "\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getScoreList() {
        StringBuffer b = new StringBuffer(100);
        for (Player p : players) {
            b.append(p + "\r\n");
        }
        return b.toString();
    }

    public Player getPlayerAt(int x, int y) {
        for (Player p : players) {
            if (p.getXpos() == x && p.getYpos() == y) {
                return p;
            }
        }
        return null;
    }

	public static void multiPlayerOpsætning() throws Exception {
		clientSocket = new Socket("localhost", 6789);
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		(new RecievingThread(clientSocket)).start();
	}

    // Håndter samtlige beskeder fra server
    public static void handleServerMessage(String message) {
        if (message == null || message.isEmpty()) return;
        String[] parts = message.split(" ");
        if (parts.length == 0) return;
        switch (parts[0]) {
            case "MOVE": {
                if (parts.length < 5) return;
                String name = parts[1];
                int x = Integer.parseInt(parts[2]);
                int y = Integer.parseInt(parts[3]);
                String direction = parts[4];
                Platform.runLater(() -> {
                    Player p = players.stream().filter(pl -> pl.name.equals(name)).findFirst().orElse(null);
                    if (p == null) {
                        p = new Player(name, x, y, direction);
                        players.add(p);
                    } else {
                        // Clear old position
                        fields[p.getXpos()][p.getYpos()].setGraphic(new ImageView(image_floor));
                        p.setXpos(x);
                        p.setYpos(y);
                        p.setDirection(direction);
                    }
                    Image heroImg = switch (direction) {
                        case "right" -> hero_right;
                        case "left" -> hero_left;
                        case "up" -> hero_up;
                        case "down" -> hero_down;
                        default -> hero_up;
                    };
                    fields[x][y].setGraphic(new ImageView(heroImg));
                    scoreList.setText(getScoreList());
                });
                break;
            }
            case "POINT": {
                if (parts.length < 3) return;
                String name = parts[1];
                int points = Integer.parseInt(parts[2]);
                Platform.runLater(() -> {
                    Player p = players.stream().filter(pl -> pl.name.equals(name)).findFirst().orElse(null);
                    if (p == null) {
                        p = new Player(name, 0, 0, "up");
                        players.add(p);
                    }
                    p.point = points;
                    scoreList.setText(getScoreList());
                });
                break;
            }
            case "SHOT": {
                if (parts.length < 5) return;
                String name = parts[1];
                int x = Integer.parseInt(parts[2]);
                int y = Integer.parseInt(parts[3]);
                String direction = parts[4];
                Platform.runLater(() -> {
                    Player shooter = players.stream().filter(pl -> pl.name.equals(name)).findFirst().orElse(null);
                    if (shooter == null) {
                        // Hvis spilleren ikke findes, opret en ny (burde ikke ske, men for sikkerhed)
                        shooter = new Player(name, x, y, direction);
                        players.add(shooter);
                    }
                    // Opret skuddet på den position og retning, som beskeden angiver
                    shotController.fireShotFromNetwork(shooter, x, y, direction);
                });
                break;
            }
            default:
                // ignore other message types
        }
    }


    // Helper: find random empty position
    private int[] getRandomEmptyPosition() {
        List<int[]> emptyFields = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                if (board[j].charAt(i) == ' ' && getPlayerAt(i, j) == null) {
                    emptyFields.add(new int[]{i, j});
                }
            }
        }
        if (emptyFields.isEmpty()) return new int[]{1, 1};
        return emptyFields.get((int) (Math.random() * emptyFields.size()));
    }
}
