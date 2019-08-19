package jhunions.isaiahgao.server.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

import jhunions.isaiahgao.common.IDFormat;

public class FormatHandler {
	
	public FormatHandler() {
		this.map = new HashMap<>();
	}
	
	private Map<String, IDFormat> map;
	
	public void load(JsonNode config) {
		JsonNode node = config.get("id-formats");
		for (Iterator<Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
			Entry<String, JsonNode> entry = it.next();
			JsonNode value = entry.getValue();
			String key = entry.getKey();
			
			map.put(key, new IDFormat(
					value.get("length").asInt(),
					value.get("ignore-first").asInt()
					));
		}
	}
	
	public String getFormat() {
		String str = "";
		for (Map.Entry<String, IDFormat> entry : this.map.entrySet()) {
			str += "\"" + entry.getKey() + "\":" + entry.getValue().toString() + ",";
		}
		str = str.substring(0, str.length() - 1);
		return str + "}";
	}
	
}
