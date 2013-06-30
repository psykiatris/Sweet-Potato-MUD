package mud.network.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import mud.Player;
import mud.network.server.input.interpreter.MasterInterpreter;
import mud.network.server.log.ConsoleLog;

/**
 * A client that that allows for reading and writing from the client socket.
 * This class and subclasses should be used for server/client interaction.
 */
public class Client {

    private Player player;
    private InetAddress address;
    private Socket client;
    private boolean online;
    private ClientWriter writer;
    private ClientReader reader;
    private Thread writerThread;
    private Thread readerThread;
    private MasterInterpreter interpreter;

    /**
     * Creates a new Client which contains connection information for the
     * server.
     *
     * @param client
     * @throws IOException
     */
    public Client(Socket client, InetAddress address, Player player, MasterInterpreter interpreter) throws IOException {
        this.online = true;
        this.client = client;
        this.address = address;
        this.player = player;
        this.interpreter = interpreter;
        this.writer = new ClientWriter();
        this.reader = new ClientReader();
        startThreads();
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Sends the passed string to this client to be printed.
     *
     * @param message
     */
    public void sendMessage(String message) {
        writer.queue.offer(message);
    }

    /**
     * Cleanly closes the connection of the passed client.
     *
     * @throws IOException
     */
    public void cleanUpConnection() {
        //Verify that the client is online before cleaning up (this prevents
        //duplicate clean ups)
        if (online) {
            try {
                disconnectClient();
                reader.fromClient.close();
                writer.toClient.close();
                System.out.println(ConsoleLog.log() + player.getName() + " has fallen into a trance.");
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Retrieves the desired Client using the given name.
     *
     * @param name the name to search for
     * @return the Client with the given name if they exist. Otherwise null.
     */
    public static Client getClient(String name, HashMap<InetAddress, Client> clientList) {
        Set<InetAddress> keySet = clientList.keySet();
        for (InetAddress i : keySet) {
            Client client = clientList.get(i);
            if (client.getPlayer().getName().equalsIgnoreCase(name)) {
                return client;
            }
        }
        return null;
    }

    /**
     * @return this client's Internet address
     */
    public InetAddress getClientAddress() {
        return address;
    }

    /**
     * Reconnect this client using the given socket.
     *
     * @param socket the new connection the client established
     */
    public void reconnect(Socket socket) throws IOException {
        online = true;
        client = socket;
        writer = new ClientWriter();
        reader = new ClientReader();
        startThreads();
    }

    /**
     * @return true if the client is currently online
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Starts the reading and writing threads for this client.
     */
    private void startThreads() {
        writerThread = new Thread(writer);
        writerThread.start();
        readerThread = new Thread(reader);
        readerThread.start();
    }

    /**
     * Interrupts the client's threads, cutting off communication to and from
     * this client. Also marks this client as being offline.
     */
    public void disconnectClient() {
        readerThread.interrupt();
        writerThread.interrupt();
        online = false;
    }

    /**
     * Reads input from this client and acts upon it.
     */
    private class ClientReader extends Thread {

        private ObjectInputStream fromClient;

        /**
         * Creates a new ChatReader to receive input from the client.
         *
         * @throws IOException
         */
        public ClientReader() throws IOException {
            fromClient = new ObjectInputStream(client.getInputStream());
        }

        /**
         * Continually waits for input (Packet objects) from the client, and
         * interprets them when they arrive.
         */
        @Override
        public synchronized void run() {
            while (!Thread.interrupted()) {
                Packet packet;
                try {
                    while ((packet = (Packet) fromClient.readObject()) != null) {
                        interpreter.interpret(address, packet);
                    }
                    Thread.sleep(100);
                } catch (NoSuchElementException | IllegalStateException | IOException | InterruptedException | ClassNotFoundException e) {
                    System.out.println(ConsoleLog.log() + " " + player.getName() + " reader crashed.");
                    break;
                }
            }
            cleanUpConnection();
        }
    }

    /**
     * Writes messages to this client.
     */
    private class ClientWriter implements Runnable {

        private LinkedBlockingQueue queue;
        private PrintWriter toClient;

        /**
         * Creates a new ChatWriter that allows the server to write to a client.
         *
         * @throws IOException
         */
        public ClientWriter() throws IOException {
            queue = new LinkedBlockingQueue();
            toClient = new PrintWriter(client.getOutputStream(), true);
        }

        public LinkedBlockingQueue getQueue() {
            return queue;
        }

        /**
         * Continually waits for a queued message to send to the client, and
         * sends them in the order they are received.
         */
        @Override
        public synchronized void run() {
            while (!Thread.interrupted()) {
                //If there's something in the queue, retrieve it and send it
                if (!queue.isEmpty()) {
                    String poll = (String) queue.poll();
                    //System.out.println(log + "Sending " + poll + " to '" + name + "'");
                    toClient.println(poll);
                }
                //Sleep to prevent rediculous CPU usage costs, haha
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    //In case the client disconnects
                    cleanUpConnection();
                    return;
                }
            }
            cleanUpConnection();
        }
    }
}