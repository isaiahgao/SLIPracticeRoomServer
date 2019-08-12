window.addEventListener("load", async () => {
	refresh();
});

const refresh = async () => {
    const response = await fetch(`/calendar`, { method: "GET" });
    if (response.status == 404) {
    	window.alert("Error: could not communicate with server.");
    	return;
    }
    
    document.getElementById("friday").innerHTML = 'FRIDAY';
    
    const jresponse = await response.json();
    const entries = jresponse['entries'];
    const table = document.getElementById("calendar-table");
    
    var count = 7;
    entries.forEach(function(str) {
    	table.rows[Math.floor(count / 7)].cells[count % 7].innerHTML = str;
    	count = count + 1;
    });
}