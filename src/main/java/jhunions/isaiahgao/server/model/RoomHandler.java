package jhunions.isaiahgao.server.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import com.amihaiemil.eoyaml.YamlSequence;

import jhunions.isaiahgao.common.PracticeRoom;
import jhunions.isaiahgao.server.Main;

public class RoomHandler implements Iterable<Map.Entry<String, PracticeRoom>> {
	
	public RoomHandler() {
		this.rooms = new HashMap<>();
	}
	
	private Map<String, PracticeRoom> rooms;
	
	public void load() {
		YamlSequence rooms = Main.config.yamlSequence("practice-rooms");
		for (int i = 0; i < rooms.size(); ++i) {
			this.rooms.put(rooms.string(i), new PracticeRoom(rooms.string(i)));
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
