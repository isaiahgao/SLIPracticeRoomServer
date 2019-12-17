package jhunions.isaiahgao.server;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import java.io.File;
import java.io.FileWriter;
import java.util.Random;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.Javalin;
import io.javalin.staticfiles.Location;
import jhunions.isaiahgao.common.Exceptions;
import jhunions.isaiahgao.server.model.Authenticator;
import jhunions.isaiahgao.server.model.CalendarHandler;
import jhunions.isaiahgao.server.model.FormatHandler;
import jhunions.isaiahgao.server.model.RoomHandler;
import jhunions.isaiahgao.server.model.UserHandler;

public class Main {
	
	private Main() {
		String auth = checkAuthFile("auth");
		String admin = checkAuthFile("admin");
		
		this.handler = new RoomHandler();
		this.users = new UserHandler();
		this.transactionlogger = new TransactionLogger();
		this.calendar = new CalendarHandler();
		this.auth = new Authenticator(auth, admin);
		this.format = new FormatHandler();
	}
	
	private static String checkAuthFile(String filename) {
		File file = new File(filename);
		String auth = null;
		if (file.exists()) {
			try {
				Scanner sc = new Scanner(file);
				while (sc.hasNextLine()) {
					auth = sc.nextLine();
					break;
				}
				sc.close();
			} catch (Exception e) {
				System.err.println("Auth file invalid!");
				System.exit(1);
			}
		}
		
		if (auth == null) {
			System.err.println("Auth file does not exist or is invalid! Generating...");
			try {
				if (!file.exists())
					file.createNewFile();
				FileWriter writer = new FileWriter(file);
				Random rand = new Random();
				byte[] buf = new byte[32];
				rand.nextBytes(buf);
				auth = new String(buf);
				writer.write(auth);
				writer.close();
			} catch (Exception e) {
				System.err.println("Failed to generate auth file.");
				System.exit(1);
			}
		}
		return auth;
	}

	public static JsonNode config;
	private CalendarHandler calendar;
	private RoomHandler handler;
	private UserHandler users;
	private TransactionLogger transactionlogger;
	private Authenticator auth;
	private FormatHandler format;
	
	public FormatHandler getFormatHandler() {
		return this.format;
	}
	
	public RoomHandler getRoomHandler() {
		return this.handler;
	}
	
	public CalendarHandler getCalendarHandler() {
		return this.calendar;
	}
	
	public UserHandler getUserHandler() {
		return this.users;
	}
	
	public Authenticator getAuthenticator() {
		return this.auth;
	}
	
	public TransactionLogger getTransactionLogger() {
		return this.transactionlogger;
	}
	
    private static ObjectMapper json = new ObjectMapper();
    private static Main instance;
    public static Main getInstance() {
    	return instance;
    }
    
	public static void main(String[] args) {
		instance = new Main();
		int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 7000;

		SQLLogger.init();
		// init
        try {
            IO.refreshService();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
		// load config
		try {
			File file = new File("config.json");
			if (!file.exists())
				file.createNewFile();
			config = json.readTree(file);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to load config.");
			System.exit(1);
		}
		instance.handler.load();
		instance.users.load();
		instance.format.load(config);
		
		//createNewDatabase("users");
        Javalin.create()
        .routes(() -> {
        	path("format", () -> {
        		put(Controller::returnFormat);
        		post(Controller::reloadFormat);
        	});
            path("rooms", () -> {
                path(":roomid", () -> {
                    post(Controller::sendCommand);
                    put(Controller::getStatus);
                });
                put(Controller::getStatuses);
            });
            path("users", () -> {
            	post(Controller::addUser);
            	path(":userid", () -> {
            		delete(Controller::removeUser);
            	});
            	path(":userid", () -> {
                	put(Controller::getUser);
            	});
            });
            path("getcalendar", () -> {
            	get(Controller::getCalendar);
            	post(Controller::reloadCalendar);
            });
        })

        .exception(Exceptions.NoSuchRoomException.class, (e, ctx) -> ctx.status(404))
        .exception(Exceptions.NoSuchUserException.class, (e, ctx) -> ctx.status(404))
        .exception(Exceptions.AuthenticationFailedException.class, (e, ctx) -> ctx.status(403))
//        .exception(JsonProcessingException.class, (e, ctx) -> ctx.status(400))
//        .exception(MalformedJsonException.class, (e, ctx) -> ctx.status(400))

        .enableStaticFiles("/public")
        .enableStaticFiles(System.getProperty("user.dir") + "/src/main/resources/public", Location.EXTERNAL)

        .start(port);
        System.out.println("Starting on port " + port);
	}
	
	public static void reloadConfig() {
		try {
			File file = new File("config.yml");
			if (!file.exists())
				file.createNewFile();
			config = json.readTree(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void reloadCalendar() {
		
	}

    public static ObjectMapper getJson() {
        return json;
    }

}
