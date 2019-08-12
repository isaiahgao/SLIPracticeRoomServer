window.addEventListener("load", async () => {
	refresh();
});

const refresh = async () => {
    const response = await fetch(`/rooms`, { method: "PUT" });
    if (response.status == 404) {
    	window.alert("Error: could not communicate with server.");
    	return;
    }
    
    const roominfotop = await response.json();
    const roominfo = roominfotop['rooms'];
    Object.keys(roominfo).forEach(function(key) {
    	const avil = getAvailability(roominfo[key]);
    	console.log("status-" + key);
   	 	document.getElementById("status-" + key).innerHTML = avil;
	});

    // set time
    const now = new Date();
    var display = "";
    var pm = false;
    if (now.getHours() > 12) {
    	pm = true;
    	display += now.getHours() - 12;
    } else {
    	display += now.getHours() == 0 ? "12" : now.getHours();
    }
    display += ":" + ("0" + now.getMinutes()).slice(-2) + " " + (pm ? "PM" : "AM");
    document.getElementById("time").innerHTML = "<strong>Last Updated:</strong> " + display;
}

const getAvailability = (data) => {
	const remaining = data['remaining'];
	if (remaining === 9999) {
		if (data.hasOwnProperty('reason') && data['reason'] !== 'null') {
			return "UNAVAILABLE: [" + data['reason'] + "]";
		}
		return "UNAVAILABLE";
	}

	if (remaining === -1) {
		return "Available";
	}
	
	return "Checked out [" + remaining + " minutes remaining]";
}