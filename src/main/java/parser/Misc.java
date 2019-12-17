package parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import com.google.common.collect.Lists;

public class Misc {
	public static void mai3n(String[] args) {
		for (int i = 109; i < 120; i++) {
			if (i == 113)
				continue;

			String result = String.format("INSERT INTO Item VALUES (\"PR_%d\", \"%s\", \"%s\", \"%s\", \"%s\");",
					i, "Practice Room " + i, "Practice Room", "Mattin Center", "GOOD");
			System.out.println(result);
		}
	}
	
	public static void main(String[] args) {
		String[] conds = {
				"NEW",	
				"NEW",	
				"NEW",	
				"NEW",	
				"NEW",	
				"GOOD",	
				"GOOD",	
				"GOOD",
				"GOOD",	
				"GOOD",
				"GOOD",	
				"GOOD",
				"GOOD",	
				"GOOD",
				"GOOD",	
				"GOOD",
				"GOOD",	
				"GOOD",
				"GOOD",	
				"GOOD",
				"GOOD",	
				"GOOD",
				"FAIR",	
				"FAIR",	
				"FAIR",	
				"FAIR",	
				"POOR"
		};

		List<String> mattin = new ArrayList<>();
		List<String> mattinvg = new ArrayList<>();
		List<String> levering = new ArrayList<>();
		List<String> leveringvg = new ArrayList<>();
		List<String> lab = new ArrayList<>();
		List<String> labvg = new ArrayList<>();
		List<String> typesvg = new ArrayList<>();
		List<String> typesg = new ArrayList<>();
		
		Set<String> types = new HashSet<>();
		
		Map<String, List<List<String>>> chad = new HashMap<>();
		chad.put("Mattin Center", Lists.newArrayList(mattin, mattinvg));
		chad.put("Levering Hall", Lists.newArrayList(mattin, mattinvg));
		chad.put("The LaB", Lists.newArrayList(mattin, mattinvg));
		
		List<String> f = f("bg");
		int phase = 0;
		String location = null;
		
		for (String s : f) {
			Random rand = new Random(s.hashCode());
			
			if (phase == 0) {
				if (s.trim().isEmpty()) {
					phase = 1;
					continue;
				}
				
				String sub = s.split("\"")[1];
				types.add(sub);
			} else if (phase == 1) {
				if (s.trim().isEmpty()) continue;
				
				if (s.startsWith("LOCATION")) {
					location = s.split(" ", 2)[1];
					continue;
				}
				
				String[] arr = s.split("\t");

				if (s.startsWith("CONSOLE")) {
					for (int i = 0; i < Integer.parseInt(arr[2]); i++) {
						int serial = rand.nextInt(8999) + 1000;
						String result = String.format("INSERT INTO Item VALUES (\"CON_%d\", \"%s\", \"%s\", \"%s\", \"%s\");",
							serial, arr[1], arr[1], location, conds[rand.nextInt(conds.length)]);
						chad.get(location).get(0).add(result);
					}
					continue;
				}
				
				if (arr.length == 3) {
					// videogame
					for (int i = 0; i < Integer.parseInt(arr[2]); i++) {
						int serial = rand.nextInt(89999) + 10000;
						String result = String.format("INSERT INTO Videogame VALUES (\"VG_%d\", \"%s\", \"%s\", \"%s\", \"%s\");",
							serial, arr[1], arr[1], location, conds[rand.nextInt(conds.length)]);
						chad.get(location).get(1).add(result);
					}
					
					if (types.add(arr[1])) {
						String result = String.format("INSERT INTO Videogame_Type VALUES (\"%s\", %d, \"%s\");",
							arr[1], (rand.nextInt(4) + 4) * 10, arr[0]);
						typesvg.add(result);
					}
				} else {
					// board game
					for (int i = 0; i < Integer.parseInt(arr[1]); i++) {
						int serial = rand.nextInt(89999) + 10000;
						String result = String.format("INSERT INTO Item VALUES (\"B_%d\", \"%s\", \"%s\", \"%s\", \"%s\");",
							serial, arr[0], arr[0], location, conds[rand.nextInt(conds.length)]);
						chad.get(location).get(0).add(result);
					}
					
					if (types.add(arr[0].trim())) {
						String result = String.format("INSERT INTO Item_Type VALUES (\"%s\", \"%s\", %d, \"%s\", %d);",
							arr[0].trim(), "NULL", (rand.nextInt(4) + 2) * 5, "Board Game", 0);
						typesg.add(result);
					}
				}
			}
		}

		typesg.forEach(System.out::println);
		System.out.println();
		typesvg.forEach(System.out::println);
		System.out.println();
		chad.entrySet().forEach(e -> {
			e.getValue().forEach(l -> {
				l.forEach(System.out::println);
				System.out.println();
			});
			System.out.println();
		});
	}
	
	public static void main2(String[] args) {
		List<String> f = f("data");
		
		List<String> consoles = new ArrayList<>();
		List<String> games = new ArrayList<>();
		Set<String> pcon = new HashSet<>();
		
		String[] conds = {
				"NEW",	
				"NEW",	
				"NEW",	
				"NEW",	
				"NEW",	
				"NEW",	
				"NEW",	
				"NEW",	
				"NEW",	
				"NEW",	
				"NEW",	
				"NEW",	
				"NEW",	
				"GOOD",	
				"GOOD",	
				"GOOD",
				"GOOD",	
				"GOOD",
				"GOOD",	
				"GOOD",
				"FAIR",	
				"FAIR",	
				"POOR",	
				"BROKEN"
		};
		
		for (String s : f) {
			if (s.isEmpty())
				continue;
			
			String[] arr = s.split("\t");
			String name = arr[0];
			int amt = Integer.parseInt(arr[1]);
			String console = arr[2];
			
			Random rand = new Random(name.hashCode());
			for (int i = 0; i < amt; i++)
				games.add(String.format("INSERT INTO Videogame VALUES (\"%d\", \"%s\", \"%s\", \"%s\", \"%s\");",
						Math.abs(rand.nextInt(89999) + 10000),
						name,
						name,
						"The LaB",
						conds[rand.nextInt(conds.length)]));
			
			if (pcon.add(name)) {
				consoles.add(String.format("INSERT INTO Videogame_Type VALUES (\"%s\", %d, \"%s\", \"%s\");",
						name,
						(rand.nextInt(5) + 3) * 10,
						console,
						120));
			}
		}
		
		consoles.forEach(System.out::println);
		games.forEach(System.out::println);
	}
	
	private static List<String> f(String str) {
		List<String> list = new ArrayList<>();
		try {
			File file = new File("sql/" + str);
			Scanner sc = new Scanner(file);
			while (sc.hasNextLine()) {
				list.add(sc.nextLine());
			}
		} catch (Exception e) {
			
		}
		return list;
	}

}
