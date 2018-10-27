package mud;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import mud.network.client.ClientFrame;
import mud.network.client.GameClient;
import mud.network.server.GameServer;

/**
 * The main plain, brain.
 *
 * @author Japhez
 */
public class Main {

    /**
     * Starts the server on a new thread and listens for client connections.
     *
     * @throws IOException When attempting connection while starting server
     */
    private static void startServer(boolean localOnly) throws IOException {
        GameServer gameServer = new GameServer(GameServer.DEFAULT_PORT, localOnly);
        new Thread(gameServer).start();
    }

    /**
     * Attempts to create and connect the client application, then starts a
     * server and connects it if necessary.
     *
     * @throws UnknownHostException When host cannot be found.
     * @throws IOException When error occurs in connection
     */
    private static void connectClient() throws UnknownHostException, IOException {
        ClientFrame clientFrame = new ClientFrame();
        GameClient gameClient = new GameClient(clientFrame.getjTextArea1(), clientFrame.getjTextField1());
        clientFrame.setVisible(true);
        //Block until the connection choice is determined
        GameClient.ConnectionChoice connectionChoice = gameClient.getConnectionChoice();
        //Check for local only play
        if (connectionChoice == GameClient.ConnectionChoice.LOCAL_SOLO)
            startServer(true);
        //Check for local hosting
        if (connectionChoice == GameClient.ConnectionChoice.LOCAL_CO_OP)
            startServer(false);
    } //Otherwise the connection is remote, no need for server


    public static void main(String[] args) {
        try {
            connectClient();
        } catch (UnknownHostException e) {
            System.out.println(MessageFormat.format("Unknown Host: {0}", e));
        } catch (IOException e) {
            System.out.println(MessageFormat.format("IO Error: {0}", e));
        }
            }
}
