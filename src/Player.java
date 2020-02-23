import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.awt.event.*;

public class Player extends JFrame {
    private int width;
    private int height;
    private JPanel panel;
    private JTextArea question;
    private JButton b1;
    private JButton b2;
    private JButton b3;
    private JButton b4;
    private int playerId;
    private int otherPlayer;
    private int[] values;
    private int maxTurns;
    private int turnsMade;
    private int myPoints;
    private int enemyPoints;
    private boolean buttonsEnabled;

    private ClientSideConnection csc;

    public Player(int width, int height) {
        this.width = width;
        this.height = height;
        panel = new JPanel();
        question = new JTextArea();
        b1 = new JButton("48");
        b2 = new JButton("51");
        b3 = new JButton("50");
        b4 = new JButton("52");
        values = new int[4];
        turnsMade = 0;
        myPoints = 0;
        enemyPoints = 0;
    }

    public void setUpGUI() {
        this.setSize(width, height);
        this.setTitle("Player " + playerId);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(panel);
        panel.setLayout(new GridLayout(1, 5));
        panel.add(question);
        question.setWrapStyleWord(true);
        question.setLineWrap(true);
        question.setEditable(false);
        panel.add(b1);
        panel.add(b2);
        panel.add(b3);
        panel.add(b4);

        if (playerId == 1) {
            question.setText("How many states are there in the Usa?");
            otherPlayer = 2;
            buttonsEnabled = true;
        } else {
            question.setText("You are player 2, wait for your turn.");
            otherPlayer = 1;
            buttonsEnabled = false;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    updateTurn();
                }
            });
            t.start();
        }

        toggleButtons();

        this.setVisible(true);
    }

    private void checkWinner() {
        buttonsEnabled = false;
        if (myPoints > enemyPoints) {
            question.setText("YOU WON! \n" + "YOU: " + myPoints + "\n" + "ENEMY: " + enemyPoints);
        } else if (myPoints < enemyPoints) {
            question.setText("YOU LOST! \n" + "YOU: " + myPoints + "\n" + "ENEMY: " + enemyPoints);
        } else {
            question.setText("DRAW!" + "\n" + "YOU: " + myPoints + "\n" + "ENEMY " + enemyPoints);
        }
        csc.closeConnection();
    }

    public void connectToServer() {
        csc = new ClientSideConnection();
    }

    public void setUpButtons() {
        ActionListener actionListener = e -> {
            if (e.getSource() == b1) {
                b1.setText("Wrong answer!");
                b1.setBackground(Color.RED);
                turnsMade++;
                buttonsEnabled = false;
                toggleButtons();
                myPoints += values[0];
                System.out.println("Turns made: " + turnsMade);
                csc.sendButtonNumber(0);
                if (playerId == 2 && turnsMade == maxTurns) {
                    checkWinner();
                } else {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            updateTurn();
                        }
                    });
                    t.start();
                }
            } else if (e.getSource() == b2) {
                b2.setText("Wrong answer!");
                b2.setBackground(Color.RED);
                turnsMade++;
                buttonsEnabled = false;
                toggleButtons();
                myPoints += values[1];
                System.out.println("Turns made: " + turnsMade);
                csc.sendButtonNumber(1);
                if (playerId == 2 && turnsMade == maxTurns) {
                    checkWinner();
                } else {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            updateTurn();
                        }
                    });
                    t.start();
                }
            } else if (e.getSource() == b3) {
                b3.setText("Correct answer!");
                b3.setBackground(Color.GREEN);
                turnsMade++;
                buttonsEnabled = false;
                toggleButtons();
                myPoints += values[2];
                System.out.println("Turns made: " + turnsMade);
                csc.sendButtonNumber(2);
                if (playerId == 2 && turnsMade == maxTurns) {
                    checkWinner();
                } else {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            updateTurn();
                        }
                    });
                    t.start();
                }
            } else if (e.getSource() == b4) {
                b4.setText("Wrong answer!");
                b4.setBackground(Color.RED);
                turnsMade++;
                buttonsEnabled = false;
                toggleButtons();
                myPoints += values[3];
                System.out.println("Turns made: " + turnsMade);
                csc.sendButtonNumber(3);
                if (playerId == 2 && turnsMade == maxTurns) {
                    checkWinner();
                } else {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            updateTurn();
                        }
                    });
                    t.start();
                }
            }
        };
        b1.addActionListener(actionListener);
        b2.addActionListener(actionListener);
        b3.addActionListener(actionListener);
        b4.addActionListener(actionListener);
    }

    public void toggleButtons() {
        b1.setEnabled(buttonsEnabled);
        b2.setEnabled(buttonsEnabled);
        b3.setEnabled(buttonsEnabled);
        b4.setEnabled(buttonsEnabled);
    }

    public void updateTurn() {
        int number = csc.receiveButtonNumber();
        question.setText("How many states are there in the Usa?");
        enemyPoints += values[number];
        if (playerId == 1 && turnsMade == maxTurns) {
            checkWinner();
        } else {
            buttonsEnabled = true;
        }
        toggleButtons();
    }

    private class ClientSideConnection {

        private final int PORT = 7778;
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;

        public ClientSideConnection() {

            System.out.println("----- Client -----");

            try {
                socket = new Socket("localhost", PORT);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                playerId = in.readInt();
                System.out.println("Connected to server as player " + playerId + ".");
                maxTurns = in.readInt() / 2;
                values[0] = in.readInt();
                values[1] = in.readInt();
                values[2] = in.readInt();
                values[3] = in.readInt();
                System.out.println("You have " + maxTurns + " turn.");
            } catch (IOException ex) {
                ex.printStackTrace();
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

        public int receiveButtonNumber() {
            int number = -0;
            try {
                number = in.readInt();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return number;
        }

        public void closeConnection() {
            try {
                socket.close();
                System.out.println("-----CONNECTION CLOSED-----");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Player p = new Player(400, 100);
        p.connectToServer();
        p.setUpGUI();
        p.setUpButtons();
    }
}