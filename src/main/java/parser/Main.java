package parser;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import jhunions.isaiahgao.common.DateTime;
import jhunions.isaiahgao.common.User;

public class Main {
	
	private static final boolean ANONYMIZE = false;
	private static final long ID_RAND_MAGIC = 534948573l;
	private static List<String> randFirstNames = new ArrayList<>();
	private static List<String> randLastNames = new ArrayList<>();
	
	public static void main(String[] args) throws Exception {
		writeTransactions();
		writeOpenTransactions();
		writeUsers();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public static void writeOpenTransactions() throws Exception {
		// the old storage format did not store primary key of Users,
		// so we must make do with looking up Email from list of users
		// sort of like doing a hashed theta join on email = email.
		
		// map<email, hopkinsid>
		Map<String, String> map = new HashMap<>();
		
		{
			// populate map from user data
			File file = new File("sql/users");
			Scanner sc = new Scanner(file);
			while (sc.hasNextLine()) {
				User user = new User(sc.nextLine());
				map.put(user.getJhed(), user.getHopkinsID());
			}
			sc.close();
		}
		
		{
			// parse transactions
			File file = new File("sql/opentransactions");
			Scanner sc = new Scanner(file);
			FileWriter writer = new FileWriter("sql/opentransactions.sql");
			while (sc.hasNextLine()) {
				String str = sc.nextLine();
				if (str.trim().isEmpty())
					continue;
				
				String[] arr = str.split("\t");

				String strtimeout = arr[0];
				String name = arr[1];
				String email = arr[2].split("@")[0];
				String phone = arr[3];
				String item = arr[4];
				
				String hopid = map.get(email.toLowerCase());
				if (hopid == null) {
					continue;
				}
				
				DateTime timeout = DateTime.fromString(strtimeout, 0);
				if (timeout == null) {
					System.err.println("Bad line: " + strtimeout);
					continue;
				}
				
				
				String checkouttime = timeout.toString();
				String serialno = "PR_" + Integer.parseInt(item.split(" ")[1]);
				
				if (ANONYMIZE) {
					hopid = "" + Math.abs(Long.parseLong(hopid) - ID_RAND_MAGIC);
				}
				
				String statement = String.format("INSERT INTO Open_transactions VALUES (\"%s\", \"%s\", '%s', \"%s\");\n", hopid, serialno, checkouttime, "true");
				writer.write(statement);
			}
			sc.close();
			writer.close();
		}
	}
	
	
	public static void writeTransactions() throws Exception {
		// the old storage format did not store primary key of Users,
		// so we must make do with Email
		// map<email, hopkinsid>
		Map<String, String> map = new HashMap<>();
		
		{
			// populate map from user data
			File file = new File("sql/users");
			Scanner sc = new Scanner(file);
			while (sc.hasNextLine()) {
				User user = new User(sc.nextLine());
				map.put(user.getJhed(), user.getHopkinsID());
			}
			sc.close();
		}
		
		{
			// parse transactions
			File file = new File("sql/transactions");
			Scanner sc = new Scanner(file);
			FileWriter writer = new FileWriter("sql/transactions.sql");
			while (sc.hasNextLine()) {
				String str = sc.nextLine();
				if (str.trim().isEmpty())
					continue;
				
				String[] arr = str.split("\t");
				if (arr.length < 9)
					continue;
				
				String strtimeout = arr[0];
				String name = arr[1];
				String email = arr[2].split("@")[0];
				String phone = arr[3];
				String item = arr[4];
				String strtimein = arr[7];
				boolean unregistered = false;
				boolean finished = arr.length > 9 && !arr[9].trim().isEmpty();
				
				String hopid = map.get(email.toLowerCase());
				if (hopid == null) {
					continue;
				}
				
				DateTime timeout = DateTime.fromString(strtimeout, 0);
				if (timeout == null) {
					System.err.println("Bad line: " + strtimeout);
					continue;
				}
				
				DateTime timein = timeout.clone();
				if (!strtimein.isEmpty())
					timein.setHMS(strtimein, timeout.getH() > 12 ? 12 : 0);
				else {
					timein.setHMS((timeout.getH() + 1) + ":" + timeout.getM() + ":" + timeout.getS(), 0);
					System.out.println("strtimein is empty: " + str);
				}
				
				if (timein == null || timeout == null) {
					continue;
				}
				
				String checkouttime = timeout.toString();
				String checkintime = timein.toString();
				String serialno = "PR_" + Integer.parseInt(item.split(" ")[1]);
				
				if (ANONYMIZE) {
					hopid = "" + Math.abs(Long.parseLong(hopid) - ID_RAND_MAGIC);
				}
				
				String statement = String.format("INSERT INTO History VALUES (\"%s\", \"%s\", '%s', '%s', \"%s\");\n", hopid, serialno, checkouttime, checkintime, "true");
				writer.write(statement);
			}
			sc.close();
			writer.close();
		}
	}
	
	public static void writeUsers() throws Exception {
		loadNames();
		
		// convert JSON to SQL insert statements
		File file = new File("sql/users");
		Scanner sc = new Scanner(file);
		FileWriter writer = new FileWriter("sql/users.sql");
		while (sc.hasNextLine()) {
			User user = new User(sc.nextLine());
			String fname = user.getName().getFirstName();
			String lname = user.getName().getLastName();
			String jhed = user.getJhed();
			String id = user.getHopkinsID();
			long phone = user.getPhone();
			
			if (ANONYMIZE) {
				// seed a rand for consistency
				Random rand = new Random(user.getHopkinsID().hashCode());
				
				id = "" + Math.abs(Long.parseLong(id) - ID_RAND_MAGIC);
				fname = randFirstNames.get(rand.nextInt(randFirstNames.size()));
				lname = randLastNames.get(rand.nextInt(randLastNames.size()));
				jhed = Character.toLowerCase(fname.charAt(0)) + lname.substring(0, Math.min(5, lname.length())).toLowerCase() + rand.nextInt(50);
				phone = 1000000000l + (long) (rand.nextDouble() * 8999999999l);
			}
			
			String statement = String.format("INSERT INTO Users VALUES (\"%s\", \"%s\", \"%s\", \"%s\", %d);\n", id, jhed, lname, fname, phone);
			writer.write(statement);
		}
		sc.close();
		writer.close();
	}
	
	private static void loadNames() throws Exception {
		File file = new File("sql/randomnames");
		Scanner sc = new Scanner(file);
		while (sc.hasNextLine()) {
			String[] arr = sc.nextLine().split(" ");
			randFirstNames.add(arr[0]);
			randLastNames.add(arr[1]);
		}
		sc.close();
	}
	
}
