package jhunions.isaiahgao.server.model;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.jetbrains.annotations.Nullable;

import jhunions.isaiahgao.common.User;

public class UserHandler {
	
	private Map<String, User> byHopkinsId;
	
	public UserHandler() {
		this.byHopkinsId = new HashMap<>();
	}
	
	public void load() {
		try {
			File file = new File("users");
			if (!file.exists()) {
				file.createNewFile();
				System.out.println("Created new user database.");
				return;
			}
			
			Scanner sc = new Scanner(file);
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				try {
					User user = new User(line);
					byHopkinsId.put(user.getHopkinsID(), user);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Could not parse value: " + line);
				};
			}
			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not load users.");
			System.exit(1);
		}
	}
	
	public @Nullable User getById(String id) {
		return this.byHopkinsId.get(id);
	}
	
	public boolean unregister(String id) {
		return this.byHopkinsId.remove(id) != null;
	}
	
	public void registerUser(User user) {
		this.byHopkinsId.put(user.getHopkinsID(), user);
		// save
		try {
			File file = new File("users");
			FileWriter writer = new FileWriter(file, true);
			writer.write(user.toString() + System.lineSeparator());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

}
