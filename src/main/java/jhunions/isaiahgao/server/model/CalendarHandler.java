package jhunions.isaiahgao.server.model;

import java.io.File;
import java.time.Month;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

import jhunions.isaiahgao.server.Main;

public class CalendarHandler {
	
	public CalendarHandler() {
		try {
			load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Map<Integer, String> defaults;
	private Map<SimpleDate, String> singles;
	private List<Recurring> recurrings;
	private String cache;
	private int lastUpdate;
	
	public void invalidateCache() {
		this.cache = null;
	}
	
	public String getCalendarJson() {
		Date today = new Date();
		int todaysdate = today.getDate();
		while (today.getDay() != 0) {
			// skip to most recent Sunday
			today.setDate(today.getDate() - 1);
		}
		if (todaysdate == this.lastUpdate && this.cache != null) {
			return this.cache;
		}
		this.lastUpdate = todaysdate;
		
		this.cache = "{\"entries\":[";
		for (int i = 0; i < 28; i++) {
			// get the next 4 weeks
			String value = this.singles.get(new SimpleDate(today));
			if (value == null) {
				for (Recurring rec : this.recurrings) {
					if (rec.within(today)) {
						value = format(rec.getHours(today.getDay())) + "<br><i>" + rec.getDescription() + "</i>";
						break;
					}
				}
				
				if (value == null) {
					value = format(defaults.get(today.getDay()));
				}
			} else {
				value = "<i>" + format(value) + "</i>";
			}
			
			boolean istoday = today.getDate() == todaysdate;
			this.cache += "\"<strong>" + (istoday ? "<font color=#FF0000>" : "") + Month.of(today.getMonth() + 1).toString().substring(0, 3) + ". " + getDate(today.getDate()) + "</strong><br>" + value + (istoday ? "</font>" : "") + "\",";
			today.setDate(today.getDate() + 1);
		}
		if (this.cache.endsWith(","))
			this.cache = this.cache.substring(0, this.cache.length() - 1);
		this.cache += "]}";
		return this.cache;
	}
	
	private String getDate(int date) {
		int digit = date % 10; // get 1's digit
		if (digit == 1)
			return date + "st";
		if (digit == 2)
			return date + "nd";
		if (digit == 3)
			return date + "rd";
		return date + "th";
	}
	
	public void load() throws Exception {
		File file = new File("availability.json");
		JsonNode root = Main.getJson().readTree(file);
		
		this.defaults = new HashMap<>();
		this.singles = new HashMap<>();
		this.recurrings = new LinkedList<>();
		
		JsonNode ndef = root.get("default");
		for (Iterator<Entry<String, JsonNode>> it = ndef.fields(); it.hasNext();) {
			Entry<String, JsonNode> entry = it.next();
			this.defaults.put(Integer.parseInt(entry.getKey()), format(entry.getValue().asText()));
		}
		
		JsonNode nsing = root.get("single");
		for (Iterator<Entry<String, JsonNode>> it = nsing.fields(); it.hasNext();) {
			Entry<String, JsonNode> entry = it.next();
			this.singles.put(new SimpleDate(entry.getKey()), format(entry.getValue().asText()));
		}
		
		JsonNode nrec = root.get("recurring");
		for (Iterator<JsonNode> it = nrec.iterator(); it.hasNext();) {
			JsonNode arec = it.next();
			this.recurrings.add(new Recurring(arec));
		}
		
		this.invalidateCache();
	}
	
	private String format(String avail) {
		return avail.replace("- ", "-<br>");
	}
	
	public static class Recurring {
		public Recurring(JsonNode node) {
			this.days = new HashMap<>();
			for (int i = 0; i < 7; i++) {
				if (node.has("" + i)) {
					days.put(i, node.get("" + i).asText());
				}
			}
			
			if (node.has("description"))
				this.description = node.get("description").asText();
		
			this.dr = new DateRange(node.get("date-range").asText());
		}
		
		private Map<Integer, String> days;
		private String description;
		private DateRange dr;
		
		public boolean within(Date date) {
			return this.dr.within(date);
		}
		
		public String getDescription() {
			return this.description;
		}
		
		public String getHours(int day) {
			return this.days.get(day);
		}
	}
	
	public static class DateRange {
		public DateRange(String str) {
			String[] arr = str.split("-");
			
			String[] beginstr = arr[0].split("/");
			this.begin = new Date();
			this.begin.setYear(beginstr.length > 2 ? Integer.parseInt(beginstr[2]) - 1900 : new Date().getYear());
			this.begin.setMonth(Integer.parseInt(beginstr[0]) - 1);
			this.begin.setDate(Integer.parseInt(beginstr[1]));
			
			String[] endstr = arr[1].split("/");
			this.end = new Date();
			this.end.setYear(endstr.length > 2 ? Integer.parseInt(endstr[2]) - 1900 : new Date().getYear());
			this.end.setMonth(Integer.parseInt(endstr[0]) - 1);
			this.end.setDate(Integer.parseInt(endstr[1]) + 1);
		}
		
		private Date begin;
		private Date end;
		
		public boolean within(Date date) {
			return date.after(this.begin) && date.before(this.end);
		}
	}
	
	public static class SimpleDate {
		public SimpleDate(String str) {
			String[] arr = str.split("/");
			this.month = Integer.parseInt(arr[0]) - 1;
			this.day = Integer.parseInt(arr[1]);
		}
		
		public SimpleDate(int month, int day) {
			this.month = month;
			this.day = day;
		}
		
		public SimpleDate(Date date) {
			this(date.getMonth(), date.getDate());
		}
		
		private int month, day;
	
		@Override
		public int hashCode() {
			return this.month * 31031 + this.day * 31;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof SimpleDate) {
				SimpleDate sd = (SimpleDate) o;
				return sd.month == this.month && sd.day == this.day;
			}
			
			if (o instanceof Date) {
				Date date = (Date) o;
				return date.getMonth() == this.month && date.getDate() == this.day;
			}
			return false;
		}
	}

}
