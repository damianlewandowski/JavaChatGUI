import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {

    private ServerSocket server;
    private ArrayList<Socket> connections = new ArrayList<>();
    private ArrayList<PrintWriter> outs = new ArrayList<>();
    private ArrayList<String> usernames = new ArrayList<>();
    private boolean listenForConn = true;

    public static void main(String[] args) {
        // Instantiate server and bind it to a given port
        Server server = new Server(9999);
    }

    // Creates server socket
    public Server(int port) {
        System.out.println("Starting server...");

        try {
            server = new ServerSocket(port);
            System.out.println(server.getInetAddress().getLocalHost());
        } catch (IOException e) {
            System.err.println(e);
        }

        // For each connection create another thread
        // which is a mini server.
        // Server Loop
        while(listenForConn) {
            try {
                Socket clientSocket = server.accept();
                connections.add(clientSocket);
                MiniServer mini = new MiniServer(clientSocket);
                mini.start();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    // What is server supposed to do?
    private class MiniServer extends Thread {

        // A client connection
        Socket client;

        // For sending messages
        PrintWriter out;

        // For reading messages
        BufferedReader in;

        // Username
        String username;

        // MiniServer constructor which takes
        // a client socket as a parameter
        public MiniServer(Socket socket) {
            super("MiniServer");
            client = socket;
            out = getOutputStream();
            // Keep all the streams for
            // effective communication.
            // Used for broadcasting messages
            outs.add(out);
            in = getInputStream();
        }

        // Close everything
        public void closeConn() {
            System.out.println("Closing the client...");
            try {
                out.close();
                in.close();
                client.close();
                connections.remove(client);
                outs.remove(out);
                usernames.remove(username);
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        private PrintWriter getOutputStream() {
            try {
                return new PrintWriter(client.getOutputStream(), true);
            } catch (IOException e) {
                System.err.println(e);
            }

            return null;
        }

        private BufferedReader getInputStream() {
            try {
                return new BufferedReader(new InputStreamReader(client.getInputStream()));
            } catch (IOException e) {
                System.err.println(e);
            }

            return null;
        }

        // Send a message to a client
        // for which this thread is made
        private void sendMessage(String msg) {
            out.println(msg);
        }

        private void sendUsernames() {
            for(PrintWriter out : outs) {
                for (String username : usernames) {
                    out.println(username);
                }
            }
        }

        // Template for a thread for receiving messages
        private class Receiver implements Runnable {
            @Override
            public void run() {
                String inputLine;
                try {
                    while ((inputLine = in.readLine()) != null) {
                        if(inputLine.charAt(0) == '[' && inputLine.charAt(inputLine.length() - 1) == ' ') {
                            username = inputLine.substring(0, inputLine.length() - 3);
                            usernames.add(username);
                            broadcastMessage(username + " has joined the chat.");
                            sendUsernames();
                        } else {
                            broadcastMessage(inputLine);
                        }
                    }
                } catch (IOException e) {
                    closeConn();
                }
            }
        }

        private void receiveMessages() {
            new Thread(new Receiver()).start();
        }

        private void broadcastMessage(String msg) {
            for(PrintWriter out : outs) {
                out.println(msg);
            }
        }

        @Override
        public void run() {
            System.out.println("Got another connection!");
            System.out.println("Currently holds : " + connections.size() + " connections.");
            receiveMessages();
        }
    }

}
