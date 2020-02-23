import java.io.*;
import java.net.*;

public class Server {

    private final int PORT = 7778;
    private ServerSocket serverSocket;
    private int numberOfPlayers;
    private ServerSideConnection player1;
    private ServerSideConnection player2;
    private int turnsMade;
    private int maxTurns;
    private int[] values;
    private int player1ButtonNumber;
    private int player2buttonNumber;

    public Server() {
        System.out.println("----- Server -----");
        numberOfPlayers = 0;
        turnsMade = 0;
        maxTurns = 2;
        values = new int[]{0, 0, 1, 0};

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void acceptConnections() {
        try {
            System.out.println("Waiting for connections...");

            while (numberOfPlayers < 2) {
                Socket socket = serverSocket.accept();
                numberOfPlayers++;
                System.out.println("Player " + numberOfPlayers + " has connected.");
                ServerSideConnection ssc = new ServerSideConnection(socket, numberOfPlayers);
                if (numberOfPlayers == 1) {
                    player1 = ssc;
                } else {
                    player2 = ssc;
                }
                Thread t = new Thread(ssc);
                t.start();
            }
            System.out.println("We now have 2 players, the game will start.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class ServerSideConnection implements Runnable {

        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private int playerId;

        public ServerSideConnection(Socket socket, int id) {
            this.socket = socket;
            this.playerId = id;

            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                out.writeInt(playerId);
                out.writeInt(maxTurns);
                out.writeInt(values[0]);
                out.writeInt(values[1]);
                out.writeInt(values[2]);
                out.writeInt(values[3]);
                out.flush();

                while (true) {
                    if (playerId == 1) {
                        player1ButtonNumber = in.readInt();
                        player2.sendButtonNumber(player1ButtonNumber);
                    } else {
                        player2buttonNumber = in.readInt();
                        player1.sendButtonNumber(player2buttonNumber);
                    }
                    turnsMade++;
                    if (turnsMade == maxTurns) {
                        break;
                    }
                }
                player1.closeConnection();
                player2.closeConnection();
            } catch (IOException ex) {
                System.out.println("-----CONNECTION CLOSED-----");
                System.exit(0);
            }
        }

        public void sendButtonNumber(int number) {
            try {
                out.writeInt(number);
                out.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public void closeConnection() {
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.acceptConnections();
    }
}