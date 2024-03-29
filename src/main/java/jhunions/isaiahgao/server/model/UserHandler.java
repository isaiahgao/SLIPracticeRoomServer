package jhunions.isaiahgao.server.model;

import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.jetbrains.annotations.Nullable;

import jhunions.isaiahgao.common.FullName;
import jhunions.isaiahgao.common.User;
import jhunions.isaiahgao.server.SQLLogger;
import jhunions.isaiahgao.server.SQLLogger.PreparedStatements;

public class UserHandler {
	
	private Map<String, User> byHopkinsId;
	
	public UserHandler() {
		this.byHopkinsId = new HashMap<>();
	}

	@Deprecated
	public void load() {
		boolean shouldSaveAll = false;
		// check for legacy
		try {
			File file = new File("oldusers");
			if (!file.exists()) {
				throw new InterruptedException();
			}
			
			Scanner sc = new Scanner(file);
			while (sc.hasNextLine()) {
				String str = sc.nextLine();
				String[] arr = str.split("\t");
				
				User user = new User(arr[0], new FullName(arr[1], arr[2]), arr[3], Long.parseLong(arr[4]));
				if (user.checkForErrors() == null)
					byHopkinsId.put(user.getHopkinsID(), user);
			}
			sc.close();
			
			shouldSaveAll = true;
			file.renameTo(new File("oldusers.processed"));
		} catch (InterruptedException e) {
			
		} catch (Exception e) {
			System.err.println("Failed to load old users.");
			e.printStackTrace();
		}
		
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
 		
 		if (shouldSaveAll) {
 			this.saveAll();
 		}
	}

	@Deprecated
	public @Nullable User getById(String id) {
		try {
			return User.fromSQL(SQLLogger.query(PreparedStatements.GET_USER_BY_ID, id));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean unregister(String id) {
		return SQLLogger.query(PreparedStatements.DELETE_USER_BY_ID, id) != null;
	}
	
	public void registerUser(User user) {
		SQLLogger.query(PreparedStatements.REGISTER_USER, user.getHopkinsID(), user.getJhed(), user.getName().getLastName(), user.getName().getFirstName(), user.getPhone());
	}
	
	@Deprecated
	public void saveAll() {
		try {
			File file = new File("users");
			FileWriter writer = new FileWriter(file);
			for (User user : this.byHopkinsId.values()) {
				writer.write(user.toString() + System.lineSeparator());
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public void append(User user) {
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
