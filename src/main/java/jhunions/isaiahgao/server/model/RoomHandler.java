package jhunions.isaiahgao.server.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.node.ArrayNode;

import jhunions.isaiahgao.common.PracticeRoom;
import jhunions.isaiahgao.common.UserInstance;
import jhunions.isaiahgao.server.Main;

public class RoomHandler implements Iterable<Map.Entry<String, PracticeRoom>> {
	
	public RoomHandler() {
		this.rooms = new HashMap<>();
	}
	
	private Map<String, PracticeRoom> rooms;
	
	public UserInstance getPersonInRoom(String id) {
		for (PracticeRoom room : this.rooms.values()) {
			if (room.getOccupantInstance() != null && room.getOccupantInstance().getUser().getHopkinsID().equals(id))
				return room.getOccupantInstance();
		}
		return null;
	}
	
	public void load() {
		ArrayNode rooms = (ArrayNode) Main.config.get("practice-rooms");
		for (int i = 0; i < rooms.size(); ++i) {
			String name = rooms.get(i).asText();
			this.rooms.put(name, new PracticeRoom(name));
		}
	}
	
	public @Nullable PracticeRoom get(String name) {
		return rooms.get(name);
	}
	
	/**
	 * @param name Name of the room.
	 * @return Whether or not the room is occupied, or true if the room doesn't exist.
	 */
	public boolean isOccupied(String name) {
		PracticeRoom pr = this.get(name);
		if (pr == null)
			return true;
		
		return pr.isOccupied();
	}

	@Override
	public Iterator<Entry<String, PracticeRoom>> iterator() {
		return this.rooms.entrySet().iterator();
	}

}
