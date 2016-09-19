import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.*;

public class Client {

    String hostName;
    int port;
    Socket clientSocket;
    static BufferedReader in;
    static PrintWriter out;

    public static Chat chat;

    static String username;
    static String usersListMsg = "";

    public static void main(String[] args) {
        Client client = new Client("192.168.56.1", 9999);
        chat = new Chat();
    }

    private static void sendMessageToServer(String msg, String username) {
        String msgToBeSent = "";
        msgToBeSent += "[ " + username + " ]   ";
        msgToBeSent += msg;
        out.println(msgToBeSent);
    }

    // Get Input Stream for reading
    // messages from the socket
    private BufferedReader getInputStream() {
        try {
            return new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.err.println(e);
        }

        return null;
    }

    // Get Output Stream for sending
    // messages from the socket to server
    private PrintWriter getOutputStream() {
        try {
            return new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println(e);
        }

        return null;
    }

    public Client(String hostName, int port) {
        System.out.println("Starting client...");

        this.hostName = hostName;
        this.port = port;

        // Create a client socket
        // Connects to the server
        try {
            clientSocket = new Socket(hostName, port);
        } catch (IOException e) {
            System.err.println(e);
        }

        in = getInputStream();
        out = getOutputStream();
    }

    private static void addComp(JPanel thePanel, JComponent comp, int xPos, int yPos, int compWidth, int compHeight, int place,
                         int stretch) {

        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.gridx = xPos;
        gridBagConstraints.gridy = yPos;
        gridBagConstraints.gridwidth = compWidth;
        gridBagConstraints.gridheight = compHeight;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        gridBagConstraints.anchor = place;
        gridBagConstraints.fill = stretch;

        thePanel.add(comp, gridBagConstraints);
    }

    private static void deleteComp(JPanel thePanel, JComponent comp) {
        thePanel.remove(comp);
    }

    // My chat GUI
    public static class Chat extends JFrame {

        JPanel thePanel;
        JTextField messageField;
        JButton sendMessageButton;
        JTextArea chatWindow;
        JTextArea usersList;

        // This will be displayed first
        // nothing else
        JLabel usernameInfo;
        JTextField usernameArea;
        JButton enterButton;

        String chatContent = "";

        public Chat() {
            this.setSize(1000, 800);
            this.setTitle("Chat v2.0");
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create the Panel
            thePanel = new JPanel();

            // Set the layout to GridBagLayout
            thePanel.setLayout(new GridBagLayout());

            // Let the user write his username
            // After he presses the Enter button
            // he enters the chat
            usernameInfo = new JLabel("Username");
            addComp(thePanel, usernameInfo, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
            usernameArea = new JTextField(10);
            addComp(thePanel, usernameArea, 2, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE);
            enterButton = new JButton("Enter");
            addComp(thePanel, enterButton, 3, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE);

            ListenForEnterChat listenForEnterChat = new ListenForEnterChat();
            enterButton.addActionListener(listenForEnterChat);

            this.add(thePanel);
            this.setVisible(true);
        }

        // Clears default message from messageField
        private class ListenForMessageField implements MouseListener {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getSource() == messageField && messageField.getText().equals("Type your message here...")) {
                    messageField.setText("");
                    System.out.println(messageField.getText());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        }

        private class ListenForSendButton implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                String messageToSend = messageField.getText();
                if(e.getSource() == sendMessageButton && (!messageToSend.equals(""))) {
                    sendMessageToServer(messageToSend, username);
                    messageField.setText("");
                }
            }
        }

        // Instantiates another thread and runs it
        // Listens for messages
        void listenForMessages() {
            new Thread(new Receiver()).start();
        }

        // Template for a thread for receiving messages
        private class Receiver implements Runnable {
            @Override
            public void run() {
                String inputLine;
                try {
                    while ((inputLine = in.readLine()) != null) {
                        if(inputLine.charAt(0) == '[' && inputLine.charAt(inputLine.length() - 1) == ']') {
                            String userNameTemp = inputLine.substring(2, inputLine.length() - 2);
                            updateUserWindow(userNameTemp);
                        } else {
                            updateChatWindow(inputLine);
                        }
                    }
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }

        public void updateChatWindow(String msg) {
            chatContent = msg + "\n";
            chatWindow.append(chatContent);
        }

        // Updates window which shows users
        // with a given username
        public void updateUserWindow(String user) {
            if(!usersListMsg.contains(user)) {
                usersListMsg += user + "\n";
                usersList.setText(usersListMsg);
            }
        }


        private class ListenForSendingMessage implements KeyListener {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(!messageField.getText().equals("") && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String messageToSend = messageField.getText();
                    sendMessageToServer(messageToSend, username);
                    messageField.setText("");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        }

        // Create Chat
        private class ListenForEnterChat implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == enterButton) {

                    username = usernameArea.getText();
                    sendMessageToServer("", username);

                    // Remove previous components
                    deleteComp(thePanel, usernameInfo);
                    deleteComp(thePanel, usernameArea);
                    deleteComp(thePanel, enterButton);

                    // Create users list
                    // displays all connected users
                    usersList = new JTextArea(5, 20);
                    addComp(thePanel, usersList, 1, 1, 1, 2, GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL);
                    usersList.setEditable(false);

                    // Create chat window where all
                    // messages are displayed
                    chatWindow = new JTextArea(20, 20);
                    addComp(thePanel, chatWindow, 2, 1, 2, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
                    chatWindow.setEditable(false);

                    // Create the message field where
                    // a user types his message
                    messageField = new JTextField("Type your message here...", 60);
                    addComp(thePanel, messageField, 1, 3, 2, 1, GridBagConstraints.SOUTHWEST, GridBagConstraints.BOTH);

                    // Add a mouse listener to the
                    // message so when user clicks
                    // at it it clears the default msg
                    ListenForMessageField listenForMessageField = new ListenForMessageField();
                    messageField.addMouseListener(listenForMessageField);

                    // Send a message when someone presses Enter
                    ListenForSendingMessage listenForSendingMessage = new ListenForSendingMessage();
                    messageField.addKeyListener(listenForSendingMessage);

                    // Create a send button
                    // on click send message
                    sendMessageButton = new JButton("Send");
                    addComp(thePanel, sendMessageButton, 3, 3, 1, 1, GridBagConstraints.SOUTHWEST, GridBagConstraints.BOTH);
                    ListenForSendButton listenForSendButton = new ListenForSendButton();
                    sendMessageButton.addActionListener(listenForSendButton);

                    // Listen for messages on another thread
                    chat.listenForMessages();
                }
            }
        }
    }
}
