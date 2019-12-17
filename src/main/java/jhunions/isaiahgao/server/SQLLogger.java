package jhunions.isaiahgao.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class SQLLogger {

	private static Connection con;
	private static String DB_ADDR;
	private static String USER;
	private static String PASS;
	
	public enum PreparedStatements {
		BEGIN_TRANSACTION("INSERT INTO Open_transactions VALUES (?, ?, NOW(), true)"),
		FINISH_TRANSACTION_DELETE_OPEN("DELETE FROM Open_transactions WHERE SerialNo = ?"),
		FINISH_TRANSACTION_INSERT_HISTORY("INSERT INTO History VALUES (?, ?, ?, NOW(), true)"),
		GET_ALL_PRACTICE_ROOMS("SELECT * FROM ("
				+ " (SELECT SerialNo, NULL AS Fname, NULL AS Lname, NULL AS CheckoutTime, NULL AS TimeElapsed"
				+ "	FROM Item"
				+ "	WHERE ItemType = 'Practice Room' AND SerialNo NOT IN (SELECT SerialNo FROM Open_transactions)"
				+ "	UNION ALL"
				+ "	(SELECT o.SerialNo, Fname, Lname, CheckoutTime, TIMESTAMPDIFF(MINUTE, CheckoutTime, NOW()) AS TimeElapsed"
				+ "	FROM Open_transactions o, Users u, Item i"
				+ "	WHERE o.CardID = u.CardID AND o.SerialNo = i.SerialNo AND i.ItemType = 'Practice Room')) AS total"
				+ ") ORDER BY SerialNo ASC;"
				),
		GET_USER_BY_ID("SELECT * FROM Users WHERE CardID = ?"),
		DELETE_USER_BY_ID("REMOVE FROM Users WHERE CardID = ?"),
		REGISTER_USER("INSERT INTO Users VALUES (?, ?, ?, ?, ?)")
		;
		
		private PreparedStatements(String query) {
			this.query = query;
		}
		
		private String query;
		
		public String getQuery() {
			return this.query;
		}
	}
	
    public static void init() {
    	try {
    		File file = new File("sqlserver");
    		if (!file.exists()) {
    			System.err.println("No sqlserver config file.");
    			System.exit(1);
    		}
    		
			try {
				Scanner sc = new Scanner(file);
				while (sc.hasNextLine()) {
					String[] arr = sc.nextLine().split("=");
					switch (arr[0]) {
					case "addr":
						DB_ADDR = arr[1];
						break;
					case "user":
						USER = arr[1];
						break;
					case "pass":
						PASS = arr[1];
						break;
					}
				}
				sc.close();
			} catch (Exception e) {
				System.err.println("Sql config file invalid!");
				System.exit(1);
			}
    		
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://" + DB_ADDR + ":3306/jhunions", USER, PASS);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static ResultSet query(PreparedStatements st, Object... args) {
    	try {
    		System.out.println("Preparing query:");
	    	String q = st.getQuery();
	    	for (int i = 0; i < args.length; i++) {
	    		q = q.replaceFirst("\\?", args[i].toString());
	    	}
	    	System.out.println(q);
    		
    		PreparedStatement query = con.prepareStatement(st.getQuery());
	    	for (int i = 0; i < args.length; i++) {
	    		if (args[i] instanceof Boolean) {
	    			query.setBoolean(i + 1, (boolean) args[i]);
	    		} else if (args[i] instanceof String) {
	    			query.setString(i + 1, (String) args[i]);
	    		} else if (args[i] instanceof Long) {
	    			query.setLong(i + 1, (long) args[i]);
	    		} else {
	    			throw new IllegalArgumentException("Some query thing was not a string or bool - go back and add conditions for: " + args[i].getClass().getName());
	    		}
	    	}
	    	
	    	try {
	    		ResultSet rs = query.executeQuery();
	    		return rs;
	    	} catch (Exception e) {
	    		query.executeUpdate();
	    		return null;
	    	}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }

}
