window.addEventListener("load", async () => {
    const response = await fetch(`/rooms`, { method: "PUT" });
    if (response.status == 404) {
    	window.alert("Error: could not communicate with server.");
    	return;
    }
    
    const roominfotop = await response.json();
    const roominfo = roominfotop['rooms'];
    var text = "";
    Object.keys(roominfo).forEach(function(key) {
    	const avil = getAvailability(roominfo[key]);
    	text = text + key + ": " + avil + "<br>";
	});

    document.getElementById("p1").innerHTML = text;
});

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