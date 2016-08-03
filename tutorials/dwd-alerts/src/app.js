//variable for leaflet map
var myMap;

$( document ).ready(function() {
	// initialize map.

	myMap = L.map('mapid').setView([51.946, 7.635], 13);

	L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
		attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
	}).addTo(myMap);

});

// BASE URL OF SOS INSTANCE
var baseUrl = "http://ows.dev.52north.org:8080/dwd-harvester-webapp/api/v1";



// RETRIEVE ALL GEOMETRIES OF DWD ALERTS	

/*
 * function to retrieve all geometries
 * 
 * note that if there currently are no DWD alerts, then the request 
 * will result in an empty set of returned items.
 *
 */  
var getAllGeometries = function(){

	// REST URL to retrieve all currently available DWD geometries
	var allGeometriesRequestUrl = baseUrl + "/geometries?expanded=true";

	// use jquery.ajax() to execute request 
	jQuery.ajax( allGeometriesRequestUrl, {

		dataType : "json",
		success : allGeometriesCallback
	})
}


var allGeometriesCallback = function(geoJSONfeatures) {

	if(geoJSONfeatures.length === 0){
		alert("Currently there are no weather alers produced by the DWD (Deutscher Wetter Dienst - German Weather Service! Consequentially, no geometries can be displayed)");
	}
	else{
		// visualize all geometries on map
	visualizeGeometriesOnMap(geoJSONfeatures);
	}
}

// VISUALIZE GEOMETRIES

// geoJSON layer for leaflet map; used to add retrieved geometries
var geoJSONlayer;

var visualizeGeometriesOnMap = function(geoJSONfeatures){
	
	for (var index=0; index< geoJSONfeatures.length; index++){
		var geoJSONfeature = geoJSONfeatures[index];
		
		// check if feature is missing geoJSON properties
		if(!(geoJSONfeature.type && geoJSONfeature.geometry)){
			
			// remove feature from array
			geoJSONfeatures.splice(index, 1);

		}
	}
	
	//remove old layer
	if(myMap.hasLayer(geoJSONlayer))
		myMap.removeLayer(geoJSONlayer);
	
	// add remaining geoJSON features to map
	geoJSONlayer = L.geoJson(geoJSONfeatures, {
			onEachFeature: onEachFeature
		}).addTo(myMap);

	//zoom to extent of added features
	zoomToAllFeatures();
}


// ZOOM TO ALL AVAILABLE FEATURES

var zoomToAllFeatures = function(){
	var bounds = L.latLngBounds([]);
	
		
	var layerBounds = geoJSONlayer.getBounds();
	// extend the bounds of the collection 
	// to fit the bounds of all features 
	bounds.extend(layerBounds);
	  
	 // apply bounds to map variable
	 myMap.fitBounds(bounds);
}

//ON EACH FEATURE

// helper variable to store the clicked latLong position
var clickedLatLng;

function onEachFeature(feature, layer) {
	
	//bind click on feature
    layer.on({
        click: fetchAndShowAlertMessage
    });
}

function fetchAndShowAlertMessage(event) {

	// set clicked position
	clickedLatLng = event.latlng;

	// get feature from event
	var feature = event.target.feature;
	
	// extract platform ID from feature
	var platformId = feature.properties.platform.id;
  
	// retrieve platform from SOS instance
	// and show popup
	var platform = getPlatform(platformId);
}


// RETRIEVE PLATFORM	

var getPlatform = function(platformId){

	// REST URL to retrieve all currently available DWD platforms
	var getPlatformRequestUrl = baseUrl + "/platforms/" + platformId;

	// use jquery.ajax() to execute request 
	jQuery.ajax( getPlatformRequestUrl, {

		dataType : "json",
		success : getPlatformCallback
	})
}

// show popup
var getPlatformCallback = function(platformData) {

	// extract first alert message
	var alertLabel = platformData.series[0].label;
	
	// show popup on clicked position
	myMap.openPopup( alertLabel, clickedLatLng); 
}