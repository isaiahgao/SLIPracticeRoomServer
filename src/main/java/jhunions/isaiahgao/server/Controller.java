package jhunions.isaiahgao.server;

import java.util.Map;

import io.javalin.Context;
import jhunions.isaiahgao.common.Exceptions;
import jhunions.isaiahgao.common.Exceptions.AuthenticationFailedException;
import jhunions.isaiahgao.common.Exceptions.NoSuchRoomException;
import jhunions.isaiahgao.common.PracticeRoom;
import jhunions.isaiahgao.common.User;

public class Controller {
	
	/**
	 * Attempts to check in/out a room. Requires auth.
	 * Body contains user card ID.
	 * Returns 400 if room is occupied, 404 if no user, 204 if successful.
	 * 
	 * Sent request should have the form:
	 * user: [SERIALIZED USER]
	 * roomid: [ROOM NUMBER]
	 * 
	 * @param ctx The context.
	 * @throws NoSuchRoomException If the request is for a room that doesn't exist.
	 * @throws AuthenticationFailedException If auth is invalid.
	 */
    public static void sendCommand(Context ctx) throws NoSuchRoomException, AuthenticationFailedException {
    	if (Main.getInstance().getAuthenticator().getAuthLevel(ctx) < 1) {
    		throw new Exceptions.AuthenticationFailedException();
    	}
    	
    	PracticeRoom room = getRoomObject(ctx);
    	if (room == null) {
    		throw new Exceptions.NoSuchRoomException();
    	}
    	
    	try {
	    	User user = getUser(ctx);
	    	
	    	if (room.isOccupied()) {
	    		if (room.getOccupant().getHopkinsID().equals(user.getHopkinsID())) {
	    			// check back in the room
	    			// TODO
	    			ctx.status(204);
	    			return;
	    		}
	    		
	    		ctx.status(400);
	    		return;
	    	}
	    	
	    	// check out the room
	    	// TOOD
	    	room.setOccupant(user);
	    	ctx.status(204);
    	} catch (Exception e) {
    		ctx.status(404);
    	}
    	
//        JsonNode body;
//        try {
//            body = Main.getJson().readTree(ctx.body());
//        } catch (IOException e) {
//            throw new MalformedJsonException();
//        }
//        
//        // must have exactly 2 parameters, one row, one column with int type values
//        System.out.println("body: " + (body == null ? "null" : body.toString()));
//        if (body == null || body.size() != 2 ||
//                !hasIntKeyOfName(body, "row") ||
//                !hasIntKeyOfName(body, "column")) {
//            throw new MalformedJsonException();
//        }
//
//        // game is already finished
//        if (!game.isActive()) {
//            throw new GameplayException();
//        }
//
//        int row = body.get("row").asInt();
//        int col = body.get("column").asInt();
//        try {
//            game.click(row, col);
//        } catch (CoordinateOutOfBoundsException e) {
//            throw new GameplayException();
//        }

        ctx.status(204);
    }
    
    /**
     * Gets status of a practice room.
     * Returns 200 + info about the room. Time remaining (in minutes), and -1 if unoccupied.
     * format:
     * time-remaining: [TIME IN MINUTES]
     * 
     * @param ctx The context.
     * @throws NoSuchRoomException 
     */
    public static void getStatus(Context ctx) throws NoSuchRoomException {
    	PracticeRoom pr = getRoomObject(ctx);
    	if (!pr.isOccupied()) {
    		ctx.result("-1");
    		ctx.status(200);
    		return;
    	}

    	ctx.result("" + pr.getMinutesUntilAvailable());
    	ctx.status(200);
    }
    
    /**
     * Gets status of all rooms at once.
     * Result 200:
     * rooms:
     *   109:
     *     user:
     *     remaining: -1
     *   110:
     *     user: bob
     *     remaining: 12
     *   ...
     *   119:
     *     user: joe
     *     remaining: 70
     * @param ctx The context. Showing users requires auth level 2.
     */
    public static void getStatuses(Context ctx) {
    	boolean op = Main.getInstance().getAuthenticator().getAuthLevel(ctx) > 1;
    	
    	String json = "{";
    	for (Map.Entry<String, PracticeRoom> entry : Main.getInstance().getRoomHandler()) {
    		json += "\"" + entry.getKey() + "\":{"
    				+ "\"remaining\":" + entry.getValue().getMinutesUntilAvailable()
    				+ (op ? ",\"user\":\"" + entry.getValue().getOccupant().getName().toString() + "\"" : "")
    				+ "},";
    	}
    	
    	if (json.endsWith(","))
    		json = json.substring(0, json.length() - 1);
    	json += "}";
    	
    	ctx.header("Content-Type", "text/json");
    	ctx.result("{\"rooms\":" + json + "}");
    	ctx.status(200);
    }
    
    /**
     * Gets user associated with a card number or email.
     * Body contains user card ID or email, and auth.
     * Returns user object, or 404 if no user exists.
     * 
     * @param ctx The context.
     * @throws Exception If something goes wrong.
     */
    public static User getUser(Context ctx) throws Exception {
    	if (Main.getInstance().getAuthenticator().getAuthLevel(ctx) < 1) {
    		throw new Exceptions.AuthenticationFailedException();
    	}
    	
    	User user = ctx.pathParam("user", User.class).getValue();
    	return user;
    }
    
    /**
     * Registers user using data.
     * Body contains user profile data to be added to database.
     * Returns 200 if successful; 404 if user already exists.
     * 
     * @param ctx The context.
     * @throws Exception If something goes wrong.
     */
    public static void addUser(Context ctx) throws Exception {
    	if (Main.getInstance().getAuthenticator().getAuthLevel(ctx) < 1) {
    		throw new Exceptions.AuthenticationFailedException();
    	}
    	
    	User user = getUser(ctx);
    	if (user == null) {
    		ctx.status(400);
    		return;
    	}
    	
    	Main.getInstance().getUserHandler().registerUser(user);
    	ctx.status(204);
    }
    
    private static PracticeRoom getRoomObject(Context ctx) throws Exceptions.NoSuchRoomException {
        String name = ctx.pathParam("roomid");
        if (name == null)
        	throw new Exceptions.NoSuchRoomException();
        
        return Main.getInstance().getRoomHandler().get(name);
    }

}
