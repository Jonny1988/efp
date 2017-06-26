var responseText = "Failure with Transmission";

function readFile() {
    //window.alert("es geht");
    var fileOne = document.forms[0].files;
    //window.alert("FileOnw" + fileOne);
    var results = document.getElementById("results");
    results.innerHTML = results.innerHTML + " " + callbackHandler();
}


function callbackHandler() {
	console.log("handler");
    var xmlhttp = new XMLHttpRequest();
    var url = "http://localhost:5000";
    xmlhttp.onreadystatechange = function requestReadyStateHandeler(){
          if(xmlhttp.readyState == 4 && xmlhttp.status == 200){
           console.log(xmlhttp.responseText);   
          }
        };
	xmlhttp.open("POST", url, true);
	xmlhttp.withCredentials = true;
	xmlhttp.setRequestHeader("Content-type", "application/json");
    xmlhttp.send(null);
	console.log("finish");
}


var json = {
    "filename": "FileServer",
    "type": "java",
    "content": "package und so n shit"
}
