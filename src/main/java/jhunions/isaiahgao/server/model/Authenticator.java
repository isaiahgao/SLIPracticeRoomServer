package jhunions.isaiahgao.server.model;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

import io.javalin.Context;
import jhunions.isaiahgao.server.Main;

public class Authenticator {
	
	public Authenticator(String auth, String admin) {
		this.auth = auth;
		this.admin = admin;
	}
	
	private final String auth, admin;
	
	public int getAuthLevel(String key) {
		if (this.admin.equals(key)) {
			return 2;
		}
		
		if (this.auth.equals(key)) {
			return 1;
		}
		return 0;
	}
	
	public int getAuthLevel(Context ctx) {
        JsonNode body;
        try {
            body = Main.getJson().readTree(ctx.body());
        } catch (IOException e) {
        	e.printStackTrace();
        	return 0;
        }
        return getAuthLevel(body.has("key") ? body.get("key").asText() : null);
	}

}
