

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import TCP.TCPClient;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.*;

public class GUI extends Application {

	public static final int size = 20; 
	public static final int scene_height = size * 20 + 100;
	public static final int scene_width = size * 20 + 200;

	public static Image image_floor;
	public static Image image_wall;
	public static Image hero_right,hero_left,hero_up,hero_down;
    public static Image shot_Right,shot_Left,shot_Up,shot_Down;

    public static Image shot_Tail_Horizontal;
    public static Image shot_Tail_Vertical;

    private List<Shot> shots = new ArrayList<>(); // Liste til at gemme skud
    private List<int[]> tailPositions = new ArrayList<>(); //Liste til at gemme hale
    private List<int[]> wallHitPositions = new ArrayList<>();

	public static Player me;
	public static List<Player> players = new ArrayList<Player>();

	private Label[][] fields;
	private TextArea scoreList;
	
	private  String[] board = {    // 20x20
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

			image_wall  = new Image(getClass().getResourceAsStream("Image/wall4.png"),size,size,false,false);
			image_floor = new Image(getClass().getResourceAsStream("Image/floor1.png"),size,size,false,false);

			hero_right  = new Image(getClass().getResourceAsStream("Image/heroRight.png"),size,size,false,false);
			hero_left   = new Image(getClass().getResourceAsStream("Image/heroLeft.png"),size,size,false,false);
			hero_up     = new Image(getClass().getResourceAsStream("Image/heroUp.png"),size,size,false,false);
			hero_down   = new Image(getClass().getResourceAsStream("Image/heroDown.png"),size,size,false,false);

            shot_Right = new Image(getClass().getResourceAsStream("Image/fireRight.png"),size,size,false,false);
            shot_Left = new Image(getClass().getResourceAsStream("Image/fireLeft.png"),size,size,false,false);
            shot_Up = new Image(getClass().getResourceAsStream("Image/fireUp.png"),size,size,false,false);
            shot_Down = new Image(getClass().getResourceAsStream("Image/fireDown.png"),size,size,false,false);

            shot_Tail_Horizontal = new Image(getClass().getResourceAsStream("Image/fireHorizontal.png"),size,size,false,false);
            shot_Tail_Vertical = new Image(getClass().getResourceAsStream("Image/fireVertical.png"),size,size,false,false);

			fields = new Label[20][20];
			for (int j=0; j<20; j++) {
				for (int i=0; i<20; i++) {
					switch (board[j].charAt(i)) {
					case 'w':
						fields[i][j] = new Label("", new ImageView(image_wall));
						break;
					case ' ':					
						fields[i][j] = new Label("", new ImageView(image_floor));
						break;
					default: throw new Exception("Illegal field value: "+board[j].charAt(i) );
					}
					boardGrid.add(fields[i][j], i, j);
				}
			}
			scoreList.setEditable(false);
			
			
			grid.add(mazeLabel,  0, 0); 
			grid.add(scoreLabel, 1, 0); 
			grid.add(boardGrid,  0, 1);
			grid.add(scoreList,  1, 1);
						
			Scene scene = new Scene(grid,scene_width,scene_height);
			primaryStage.setScene(scene);
			primaryStage.show();

			scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
				switch (event.getCode()) {
				case UP:    playerMoved(0,-1,"up");    break;
				case DOWN:  playerMoved(0,+1,"down");  break;
				case LEFT:  playerMoved(-1,0,"left");  break;
				case RIGHT: playerMoved(+1,0,"right"); break;

                case SPACE: fireShot();

                default: break;



				}
			});
			
            // Setting up standard players
			
			me = new Player("Orville",9,4,"up");
			players.add(me);
			fields[9][4].setGraphic(new ImageView(hero_up));

			Player harry = new Player("Harry",14,15,"up");
			players.add(harry);
			fields[14][15].setGraphic(new ImageView(hero_up));

			scoreList.setText(getScoreList());
		} catch(Exception e) {
			e.printStackTrace();
		}

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateShots();
            }
        };
        timer.start();
	}

	public void playerMoved(int delta_x, int delta_y, String direction) {
		me.direction = direction;
		int x = me.getXpos(),y = me.getYpos();

		if (board[y+delta_y].charAt(x+delta_x)=='w') {
			me.addPoints(-1);
		} 
		else {
			Player p = getPlayerAt(x+delta_x,y+delta_y);
			if (p!=null) {
              me.addPoints(10);
              p.addPoints(-10);
			} else {
				me.addPoints(1);
			
				fields[x][y].setGraphic(new ImageView(image_floor));
				x+=delta_x;
				y+=delta_y;

				if (direction.equals("right")) {
					fields[x][y].setGraphic(new ImageView(hero_right));
				};
				if (direction.equals("left")) {
					fields[x][y].setGraphic(new ImageView(hero_left));
				};
				if (direction.equals("up")) {
					fields[x][y].setGraphic(new ImageView(hero_up));
				};
				if (direction.equals("down")) {
					fields[x][y].setGraphic(new ImageView(hero_down));
				};

				me.setXpos(x);
				me.setYpos(y);
			}
		}
		scoreList.setText(getScoreList());
	}

	public String getScoreList() {
		StringBuffer b = new StringBuffer(100);
		for (Player p : players) {
			b.append(p+"\r\n");
		}
		return b.toString();
	}

	public Player getPlayerAt(int x, int y) {
		for (Player p : players) {
			if (p.getXpos()==x && p.getYpos()==y) {
				return p;
			}
		}
		return null;
	}

    public void fireShot() {
        Shot s = new Shot(me.getXpos(), me.getYpos(), me.direction, me);
        shots.add(s);
    }

    public void updateShots() {
        List<Shot> toRemove = new ArrayList<>();

        // Fjern wallhit billeder
        for (int[] pos : wallHitPositions) {
            fields[pos[0]][pos[1]].setGraphic(new ImageView(image_floor));
        }
        wallHitPositions.clear();

        // 1. Fjern haler fra tidligere positioner
        for (int[] pos : tailPositions) {
            fields[pos[0]][pos[1]].setGraphic(new ImageView(image_floor));
        }
        tailPositions.clear();

        for (Shot s : shots) {
            // fjern gammel grafik
            fields[s.getX()][s.getY()].setGraphic(new ImageView(image_floor));

            // 2. Tegn hale og gem position
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

            // rammer væg?
            if (board[s.getY()].charAt(s.getX()) == 'w') {
                int prevX = s.getX();
                int prevY = s.getY();
                switch (s.getDirection()) {
                    case "left":  prevX += 1; break;
                    case "right": prevX -= 1; break;
                    case "up":    prevY += 1; break;
                    case "down":  prevY -= 1; break;
                }
                // Viser wallhittingimage før fjernelse af skud
                switch (s.getDirection()) {
                    case "left":
                        fields[prevX][prevY].setGraphic(new ImageView(
                                new Image(getClass().getResourceAsStream("Image/fireWallWest.png"), size, size, false, false)));
                        break;
                    case "right":
                        fields[prevX][prevY].setGraphic(new ImageView(
                                new Image(getClass().getResourceAsStream("Image/fireWallEast.png"), size, size, false, false)));
                        break;
                    case "up":
                        fields[prevX][prevY].setGraphic(new ImageView(
                                new Image(getClass().getResourceAsStream("Image/fireWallNorth.png"), size, size, false, false)));
                        break;
                    case "down":
                        fields[prevX][prevY].setGraphic(new ImageView(
                                new Image(getClass().getResourceAsStream("Image/fireWallSouth.png"), size, size, false, false)));
                        break;
                }
                // gem wallhitposition for senere fjernelse
                wallHitPositions.add(new int[]{prevX, prevY});
                toRemove.add(s);
                continue;
            }

            // rammer spiller?
            Player p = getPlayerAt(s.getX(), s.getY());
            if (p != null && p != s.getOwner()) {
                s.getOwner().addPoints(5);
                p.addPoints(-5);
                toRemove.add(s);
                continue;
            }



            // ellers tegn skuddet
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

        // Gentegn alle spillerne for at sikre de forbliver synlige
        for (Player p : players) {
            Image heroImg = switch (p.direction) {
                case "right" -> hero_right;
                case "left" -> hero_left;
                case "up" -> hero_up;
                case "down" -> hero_down;
                default -> hero_up;
            };
            fields[p.getXpos()][p.getYpos()].setGraphic(new ImageView(heroImg));
        }

        shots.removeAll(toRemove);
        scoreList.setText(getScoreList());
    }
    // ekstra kode til commit
}

