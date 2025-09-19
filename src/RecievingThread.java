import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class RecievingThread extends Thread {
    Socket connSocket;

    public RecievingThread(Socket connSocket) {
        this.connSocket = connSocket;
    }


    public void run() {
        try {
            BufferedReader inFromAfsender = new BufferedReader(new InputStreamReader(connSocket.getInputStream()));
            while(true){
                String message = inFromAfsender.readLine();
                GUI.handleServerMessage(message);
            }
//            System.out.println("Forbundet til server.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//
//     while (true) {
//        String message = inFromAfsender.readLine();
//        if (message == null) continue;
//        System.out.println("From sender: " + message);
//        String[] parts = message.split(" ");
//        if (parts.length == 0) continue;
//        switch (parts[0]) {
//            case "MOVE":
//                // MOVE <playerName> <x> <y> <direction>
//                if (parts.length < 5) break;
//                String name = parts[1];
//                int x = Integer.parseInt(parts[2]);
//                int y = Integer.parseInt(parts[3]);
//                String direction = parts[4];
//                Player p = getOrCreatePlayer(name);
//                p.setXpos(x);
//                p.setYpos(y);
//                p.setDirection(direction);
//                updatePlayerOnBoard(p);
//                break;
//            case "POINT":
//                // POINT <playerName> <points>
//                if (parts.length < 3) break;
//                String pname = parts[1];
//                int points = Integer.parseInt(parts[2]);
//                Player pp = getOrCreatePlayer(pname);
//                pp.point = points;
//                updateScoreList();
//                break;
//            default:
//                // Ignore other messages
//        }
//    }
//    private Player getOrCreatePlayer(String name) {
//        for (Player p : GUI.players) {
//            if (p.name.equals(name)) return p;
//        }
//        // Create new player at default position
//        Player newP = new Player(name, 0, 0, "up");
//        GUI.players.add(newP);
//        return newP;
//    }

//    private void updatePlayerOnBoard(Player p) {
//        Platform.runLater(() -> {
//            // Remove old graphic
//            for (int i = 0; i < 20; i++) {
//                for (int j = 0; j < 20; j++) {
//                    Player at = null;
//                    for (Player pl : GUI.players) {
//                        if (pl.getXpos() == i && pl.getYpos() == j && pl.name.equals(p.name)) {
//                            at = pl;
//                            break;
//                        }
//                    }
//                    if (at != null) {
//                        GUI.fields[i][j].setGraphic(new ImageView(GUI.image_floor));
//                    }
//                }
//            }
//            // Draw new graphic
//            Image heroImg = switch (p.direction) {
//                case "right" -> GUI.hero_right;
//                case "left" -> GUI.hero_left;
//                case "up" -> GUI.hero_up;
//                case "down" -> GUI.hero_down;
//                default -> GUI.hero_up;
//            };
//            GUI.fields[p.getXpos()][p.getYpos()].setGraphic(new ImageView(heroImg));
//        });
//    }

//    private void updateScoreList() {
//        Platform.runLater(() -> {
//            GUI.scoreList.setText(GUI.getScoreList());
//        });
//    }

}
