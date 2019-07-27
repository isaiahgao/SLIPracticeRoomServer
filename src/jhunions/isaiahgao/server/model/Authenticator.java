package jhunions.isaiahgao.server.model;

import io.javalin.Context;

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
		return this.getAuthLevel(ctx.pathParam("key"));
	}

}
