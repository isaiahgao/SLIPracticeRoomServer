package jhunions.isaiahgao.server.model;

public class Authenticator {
	
	public Authenticator(String auth) {
		this.auth = auth;
	}
	
	private final String auth;
	
	public boolean authenticate(String key) {
		return auth.equals(key);
	}

}
