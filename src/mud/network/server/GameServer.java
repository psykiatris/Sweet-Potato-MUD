package mud.network.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import mud.GameMaster;
import mud.characters.Player;
import mud.network.server.input.interpreter.LoginInterpreter;
import mud.network.server.log.ConsoleLog;

/**
 * This is the top-level server implementation. Lower level features such as
 * chat should be abstracted to allow for easier debugging and testing. Only top
 * level server interactions should be directly done here. (connecting,
 * disconnecting, etc.)
 *
 * @author Japhez
 */
public class GameServer implements Runnable {

    public static final int DEFAULT_PORT = 5000;
    private final ServerSocket serverSocket;
    private final HashMap<InetAddress, Connection> clientMap; //The master client list that holds connection
    private final GameMaster gameMaster;
    public boolean localOnly;

    /**
     * Creates a new chat server operating at the passed port.
     *
     * @param port the port to operate the server on
     *
     * @throws IOException When it fails to connect
     */

    public GameServer(int port) throws IOException {
        System.out.println(ConsoleLog.log() + "Server starting on port " + port);
        serverSocket = new ServerSocket(port);
        clientMap = new HashMap<>();
        gameMaster = new GameMaster();
    }

    /**
     * Continually checks for new connections. When a client connects, requests
     * their player information.
     */
    public final void setLocalOnly(boolean b) {
        localOnly = b;
    }

    public final boolean isLocalOnly() {
        return localOnly;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Socket newClient = serverSocket.accept();
                Connection connection = connect(newClient);
                //Make sure the connection was correctly established
                if (connection != null) {
                    clientMap.put(newClient.getInetAddress(), connection);
                    System.out.println(ConsoleLog.log() + "Player connected from "
                            + newClient.getInetAddress());
                    //If the server is local only, only accept one connection
                    // Check if local
                    boolean b = isLocalOnly();
                    if (b) {
                        System.out.println(ConsoleLog.log() + "Local-only connection established.");
                        break;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private synchronized Connection connect(Socket socket) {
        //Don't allow duplicate connections
        if (clientMap.get(socket.getInetAddress()) != null) {
            return null;
        }
        Connection connection = null;
        try {
            //Create a temporary (unlisted) character until info is validated
            Player player = new Player("NoOne");
            connection = new Connection(socket, player, new LoginInterpreter(clientMap, gameMaster));
            //The player stores a reference to their current connection for convenience
            player.setConnection(connection);
            connection.sendMessage("Please enter your character name.");
        } catch (IOException ex) {
            Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
            return connection;
        }
        return connection;
    }

    @Override
    public String toString() {
        return "GameServer{" +
                "serverSocket=" + serverSocket +
                ", clientMap=" + clientMap +
                ", gameMaster=" + gameMaster +
                ", localOnly=" + localOnly +
                '}';
    }
}
