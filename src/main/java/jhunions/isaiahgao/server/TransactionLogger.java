package jhunions.isaiahgao.server;

import java.io.IOException;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.api.services.sheets.v4.Sheets.Spreadsheets;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.Lists;

import jhunions.isaiahgao.common.UserInstance;
import jhunions.isaiahgao.common.Utils;

public class TransactionLogger {
	
    private int logsize;
    private Month month;
	
	public TransactionLogger() {
	}
	
    // log in
    public void login(String room) {
        this.push(room);
    }
    
    // log out
    public void logout(String room) {
        this.poll(room);
    }
    
    // fill in Time In and Monitor Initials in database
    private void poll(String room) {
    	UserInstance inst = Main.getInstance().getRoomHandler().get(room).getOccupantInstance();
    	if (inst == null)
    		return;
    	
        Spreadsheets accessor = IO.getService().spreadsheets();
        String range = inst.getSheetName() + "!H" + inst.getLine() + ":I" + inst.getLine();
        
        List<List<Object>> values = Arrays.asList(Arrays.asList(
                Utils.getTime(new Date()), "AUTO LOG"
                ));
        ValueRange vr = new ValueRange().setValues(values);

        try {
			accessor.values().update(IO.getLogURL(), range, vr)
			.setValueInputOption("RAW")
			.execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    // handle IO for logging in
    private void push(String room) {
    	try {
	        this.checkMonth();
	        this.logUser(room);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    // fill in user data
    private void logUser(String room) {
    	try {
        	UserInstance inst = Main.getInstance().getRoomHandler().get(room).getOccupantInstance();
	        Spreadsheets accessor = IO.getService().spreadsheets();
	        
	        if (this.logsize == 0) {
	            try {
	                ValueRange vr = accessor.values().get(IO.getLogURL(), "A:A").execute();
	                this.logsize = vr.getValues() == null ? 0 : vr.getValues().size();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	        this.logsize++;
	        // save line that we're putting data on
	        inst.setLine(this.logsize);
	        
	        // save to db
	        List<List<Object>> values = Arrays.asList(
	                inst.toObjectList()
	        );
	        String range = "A" + this.logsize + ":J" + this.logsize;
	        ValueRange body = new ValueRange().setValues(values);
	
	        accessor.values()
	            .update(IO.getLogURL(), range, body)
	            .setValueInputOption("RAW")
	            .execute();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    // check if a new month is needed
    private void checkMonth() throws Exception {
    	Spreadsheets accessor = IO.getService().spreadsheets();
        if (this.month == null || Month.of(Calendar.getInstance().get(Calendar.MONTH) + 1) != this.month) {
            // see if we need to create a new spreadsheet
            this.month = Month.of(Calendar.getInstance().get(Calendar.MONTH) + 1);
            String matching = Utils.capitalizeFirst(this.month.toString()) + " " + Calendar.getInstance().get(Calendar.YEAR);
            
            Spreadsheet ss = accessor.get(IO.getLogURL()).execute();
            for (Sheet s : ss.getSheets()) {
                if (s.getProperties().getTitle().equals(matching)) {
                    // we have a sheet for the current month; proceed as usual
                    return;
                }
            }
            
            //otherwise create the new sheet
            
            // generate properties
            SheetProperties prop = new SheetProperties();
            prop.setTitle(matching);
            prop.setIndex(0);
            
            int id = this.month.getValue() << 24 | Calendar.getInstance().get(Calendar.YEAR);
            prop.setSheetId(id);
            
            List<Request> req = new ArrayList<>();
            req.add(new Request().setAddSheet(new AddSheetRequest().setProperties(prop)));
            
            // write the header data
            List<List<Object>> values = new ArrayList<>();
            values.add(Lists.newArrayList(
                    "Timestamp",
                    "Name",
                    "JHED E-mail",
                    "Phone Number",
                    "Room",
                    "Current Time",
                    "Agreement",
                    "Time Returned",
                    "Monitor Name Upon Return",
                    "Comments"
                    ));
            String range = matching + "!A1:J1";
            ValueRange body = new ValueRange().setValues(values);
            
            // create the action
            accessor.batchUpdate(IO.getLogURL(), new BatchUpdateSpreadsheetRequest().setRequests(req)).execute();
            accessor.values()
                .update(IO.getLogURL(), range, body)
                .setValueInputOption("RAW")
                .execute();
            // reset log size
            this.logsize = 0;
        }
    }
}
