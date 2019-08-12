package jhunions.isaiahgao.server;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;

import io.javalin.Context;
import jhunions.isaiahgao.common.Exceptions;
import jhunions.isaiahgao.common.Exceptions.AuthenticationFailedException;
import jhunions.isaiahgao.common.Exceptions.NoSuchRoomException;
import jhunions.isaiahgao.common.PracticeRoom;
import jhunions.isaiahgao.common.ScanResultPacket;
import jhunions.isaiahgao.common.ScanResultPacket.ScanResult;
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
    		JsonNode node = Main.getJson().readTree(ctx.body());
    		String type = node.get("type").asText();
    		switch (type) {
    		case "scan":
    			scan(ctx, room);
    			break;
    		case "enable":
    			enable(ctx, room);
    			break;
    		case "disable":
    			disable(node, ctx, room);
    			break;
    		}
    	} catch (Exception e) {
    		ctx.status(404);
    	}
    }
    
    private static void enable(Context ctx, PracticeRoom room) {
		Main.getInstance().getTransactionLogger().logout(room.getId());
    	room.enable();
    	ctx.status(204);
    }
    
    private static void disable(JsonNode node, Context ctx, PracticeRoom room) throws Exception {
    	room.disable(node.has("reason") ? node.get("reason").asText() : null);
    	ctx.status(204);
    }
    
    private static void scan(Context ctx, PracticeRoom room) throws Exception {
    	ctx.header("Content-Type", "text/json");
		
		User user = getUserFromCtx(ctx);
    	if (user == null) {
    		ctx.result(new ScanResultPacket(ScanResult.NO_SUCH_USER, null).toString());
    		ctx.status(200);
    		return;
    	}
    	
    	if (room.isOccupied()) {
    		if (room.getOccupant().getHopkinsID().equals(user.getHopkinsID())) {
    			Main.getInstance().getTransactionLogger().logout(room.getId());
    			room.setOccupant(null);
	    		ctx.result(new ScanResultPacket(ScanResult.CHECKED_IN, room.getId()).toString());
	    		ctx.status(200);
    			return;
    		}
    		
    		ctx.status(400);
    		return;
    	}
    	
    	room.setOccupant(user);
		Main.getInstance().getTransactionLogger().login(room.getId());
		ctx.result(new ScanResultPacket(ScanResult.CHECKED_OUT, room.getId()).toString());
		ctx.status(200);
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
     *     user: {json}
     *     remaining: 70
     * @param ctx The context. Showing users requires auth level 2.
     */
    public static void getStatuses(Context ctx) {
//    	System.out.println("received get request");
    	boolean op = Main.getInstance().getAuthenticator().getAuthLevel(ctx) > 1;
    	
    	String json = "{";
    	for (Map.Entry<String, PracticeRoom> entry : Main.getInstance().getRoomHandler()) {
    		PracticeRoom room = entry.getValue();
    		User occupant = room.getOccupant();
    		json += "\"" + entry.getKey() + "\":{"
    				+ "\"remaining\":" + (room.isEnabled() ? room.getMinutesUntilAvailable() : 9999)
    				+ (op ? ",\"user\":\"" + (occupant == null ? "\"null\"" : occupant.toString()) + "\"" : "")
    				+ (room.isEnabled() && room.getDisabledReason() != null ? "" : ",\"reason\":\"" + room.getDisabledReason() + "\"") + "},";
    	}
    	
    	if (json.endsWith(","))
    		json = json.substring(0, json.length() - 1);
    	json += "}";
    	
    	ctx.header("Content-Type", "text/json");
    	ctx.result("{\"rooms\":" + json + "}");
    	ctx.status(200);
    }
    
    public static void getUser(Context ctx) throws Exception {
    	if (Main.getInstance().getAuthenticator().getAuthLevel(ctx) < 1) {
    		throw new Exceptions.AuthenticationFailedException();
    	}
    	
        String id = ctx.pathParam("userid");
        if (id == null)
        	throw new Exceptions.InvalidSyntaxException();
    	
        User user = Main.getInstance().getUserHandler().getById(id);
        if (user == null)
        	throw new Exceptions.NoSuchUserException();
        
    	ctx.header("Content-Type", "text/json");
    	ctx.result(user.toString());
    	ctx.status(200);
    }
    
    public static void removeUser(Context ctx) throws Exception {
    	if (Main.getInstance().getAuthenticator().getAuthLevel(ctx) < 1) {
    		throw new Exceptions.AuthenticationFailedException();
    	}
    	
        String id = ctx.pathParam("userid");
        if (id == null)
        	throw new Exceptions.InvalidSyntaxException();
    	
        boolean successful = Main.getInstance().getUserHandler().unregister(id);
        if (!successful)
        	throw new Exceptions.NoSuchUserException();
        
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
    private static User getUserFromCtx(Context ctx) throws Exception {
    	if (Main.getInstance().getAuthenticator().getAuthLevel(ctx) < 1) {
    		throw new Exceptions.AuthenticationFailedException();
    	}
    	
    	JsonNode body;
        try {
            body = Main.getJson().readTree(ctx.body());
        } catch (IOException e) {
        	e.printStackTrace();
        	throw new Exceptions.NoSuchUserException();
        }
        
        try {
        	// has user as node
        	if (body.has("user"))
        		return new User(body.get("user").asText());
        
        	// has card number for some reason
        	if (body.has("cardnumber")) {
        		String num = body.get("cardnumber").asText();
        		return Main.getInstance().getUserHandler().getById(num);
        	}
        	
        	// thing is a user
        	return new User(ctx.body());
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new Exceptions.NoSuchUserException();
        }
    }
    
    public static void getCalendar(Context ctx) throws Exception {
    	String json = Main.getInstance().getCalendarHandler().getCalendarJson();
    	ctx.result(json);
    	ctx.status(200);
    }
    
    public static void reloadCalendar(Context ctx) throws Exception {
    	if (Main.getInstance().getAuthenticator().getAuthLevel(ctx) < 2) {
    		throw new Exceptions.AuthenticationFailedException();
    	}
    	
    	Main.reloadCalendar();
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
    	
    	User user = getUserFromCtx(ctx);
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
    
    public static void disableRoom(Context ctx) throws Exception {
    	
    }

}
