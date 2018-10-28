package mud.characters;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import mud.geography.Direction;
import mud.geography.Room;
import mud.network.server.Connection;

/**
 * A MUD player.
 *
 * @author Japhez
 */
public class Player extends NPC {

    private static final long serialVersionUID = -1239901229610596639L;
    private Connection connection;
    private char[] password;
    boolean needsSaving;
    private String message;

    public Player(String name) {
        super(name);
        needsSaving = false;
    }

    public final char[] getPassword() {
        return password.clone();
    }

    public final void setPassword(char[] p) {
        password = p.clone();
    }

    public final boolean isSaved() {
        return needsSaving;
    }

    public final void hasBeenSaved() {
        needsSaving = false;
    }

    public final void setConnection(Connection conn) {
        connection = conn;
    }

    public final Connection getConnection() {
        return connection;
    }

    /**
     * Removes the player from an existing old room, sets their current room to
     * the new room, and adds them to that room's occupant list. Similar to the
     * move method, but more explicit
     *
     * @param r the room the player is in
     */
    @Override
    public final void setCurrentRoom(Room r) {
        //If the player is currently in a room
        if (currentRoom != null) {
            currentRoom.removePlayer(this); //Remove player from old room
        }
        currentRoom = r; //Set current room
        currentRoom.addPlayer(this); //Add player to current room
        needsSaving = true;
    }

    /**
     * Sends a message to this via the player's established connection.
     *
     * @param msg the message to send
     */
    public final void sendMessage(String msg) {
        message = msg;
        connection.sendMessage(message);
    }

    /**
     * Gets and sends the description of the room and contents to the player.
     */
    public final void look() {
        if (currentRoom == null) {
            sendMessage("You seem to be in limbo.  How odd.");
            return;
        }
        String description = "\n";
        //Get room title;
        description += currentRoom.getName();
        //Get room description;
        description += MessageFormat.format("\n{0}", currentRoom.getDescription());
        //Get room exits
        description += "\n" + currentRoom.getFormattedExits();
        //Get NPCs in room
        AtomicReference<ArrayList<NPC>> NPClist = new AtomicReference<>(currentRoom.getNPCs());
        if (!NPClist.get().isEmpty()) {
            for (NPC n : NPClist.get()) {
                description = MessageFormat.format("\n{0} is here.", n.getName());
            }
        }
        //Get players in room
        AtomicReference<ArrayList<Player>> playersList = new AtomicReference<>(currentRoom.getPlayers());
        if (!playersList.get().isEmpty()) {
            for (Player p : playersList.get()) {
                //List the player if this player isn't that player...what?
                if (!p.equals(this)) {
                    description = MessageFormat.format("\n{0} is here.", p.getName());
                }
            }
        }
        sendMessage(description);
    }

    /**
     * Attempts to move this player in the given direction, and then gives them
     * feedback about whether it was successful or not.
     *
     * @param dir the direction the player is trying to move
     */
    @Override
    public final void move(Direction dir) {
        Room roomInDirection = currentRoom.getRoomInDirection(dir);
        if (roomInDirection != null) {
            //Remove the player from the player list in the current room
            currentRoom.removePlayer(this);
            //Send departure message to the room
            currentRoom.sendMessageToRoom(MessageFormat.format("{0} leaves {1}.", name, dir));
            //Send moving message to the player
            sendMessage(MessageFormat.format("You move {0}.", dir));

            //Move the player in the given direction
            setCurrentRoom(roomInDirection);
            // Announce arrival
            currentRoom.sendMessageToRoom(MessageFormat.format("{0} arrives " +
                    "from the {1}.", name, Direction.getOppositeDirection(dir)));
            //Force the player to look
            look();
        } else {
            sendMessage("You cannot move in that direction.");
        }
    }

    @Override
    public final String toString() {
        return MessageFormat.format("Player'{'connection={0}, name=''{1}'', respawnRoom={2}'}'", connection, name, respawnRoom);
    }
}
