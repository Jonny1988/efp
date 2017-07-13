window.addEventListener("load", init);
var file,id,url;
function init(){
	document.getElementById("data_one").addEventListener("change", function chooseFiles(event){
		file = event.target.files[0];
		id= "result_one";
		url = "http://localhost:5000";
		getDataContent();
	});
	document.getElementById("data_two").addEventListener("change", function chooseFiles(event){
		file = event.target.files[0];
		id= "result_two";
		url = "http://localhost:5001";
		getDataContent();
	});
}

function getDataContent(){
	var reader = new FileReader();
	reader.addEventListener("load", readContent);
	reader.addEventListener("error", showErrors);
	reader.readAsText(file, "UTF-8");
}

function readContent(event){
		console.log(event.target.result);
		callbackHandler(event.target.result);
}

function showErrors(event){
	alert("Data could not be loaded");
}

function callbackHandler(text) {
	var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function requestReadyStateHandeler(){
        if(xmlhttp.readyState == 4 && xmlhttp.status == 200){
			var obj = xmlhttp.response;
			var json = JSON.parse(obj)[0];
			document.getElementById(id).innerHTML = 
			document.getElementById(id).innerHTML + "<br>" +
			json.type + "<br>" + json.name + "<br>" + json.description;
        }
    };
	xmlhttp.open("POST", "http://localhost:5000", true);
	xmlhttp.setRequestHeader("Content-type", "text/plain");
    xmlhttp.send(text);
}