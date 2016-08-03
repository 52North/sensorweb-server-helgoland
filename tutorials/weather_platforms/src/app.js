//variable for leaflet map
var myMap;

//executed once DOM is loaded
$( document ).ready(function() {

     // initialize map.
     myMap = L.map('mapid').setView([51.946, 7.635], 13);

     // define Open Street Map layer
     L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
          attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
     }).addTo(myMap);

    // retrieve all phenomena from SOS
    // for select element
    getAllPhenomena();
});


// BASE URL OF API INSTANCE
var baseUrl = "http://ows.dev.52north.org:8080/ckan-sos-webapp/api/v1";


// RETRIEVE ALL STATIONS

// function to retrieve all stations
var getAllStations = function(){

    // REST URL to  retrieve all available stations
    var allStationsRequestUrl = baseUrl + "/stations";

    // use jquery.ajax() to execute request
    jQuery.ajax( allStationsRequestUrl, {

        dataType : "json",
        success : allStationsCallback
    });
};

// stationsis an array of GeoJSON items!
var allStationsCallback = function(stations) {

    if(stations.length === 0){
        alert("Currently there are no weather stations to display!");
    }
    else{
        // visualize all stations on map
        visualizeStations(stations);
    }
};

// function to retrieve one stations
var setPopupContent = function(station, popup) {
    var content = $("<div />", {
        class : "auto"
    });
    
    var url = baseUrl + "/timeseries?station=" + station.properties.id;
    var selection = $("#selectPhenomenon :selected");
    if (selection.index() > 1) {
        url = url + "&phenomenon=" + selection.val();
    }
    
    jQuery.ajax(url, {
        dataType : "json",
        success : function(data) {
            
            var title = $("<b></b>")
                    .text("Last Values @ " + station.properties.label);
                        
            var lastValues = [];
            for (var idx in data) {
                var timeseries = data[idx];
                var url = baseUrl + "/timeseries/" + timeseries.id;
                jQuery.ajax(url, {
                    dataType : "json",
                    success : function(data) {
                        var uom = data.uom;
                        var value = data.lastValue.value;
                        var date = new Date(data.lastValue.timestamp);
                        var atZulu = date.toISOString().slice(0,19) + " (UTC)";
                        var phenomenon = data.parameters.phenomenon.label;
                        lastValues.push($("<div />").text(phenomenon + ": " + value + " " + uom + ", " + atZulu));
                        content.append(title, lastValues);
                        popup.update();
                    }
                });
            }
        }
    });
    
    // set as native DOM node
    popup.setContent(content[0]);
};

var geoJSONlayer;

// stations are GeoJSON features
var visualizeStations = function(stations){

    for (var index=0; index< stations.length; index++){
        var station = stations[index];

        // check if feature is missing geoJSON properties
        if(!(station.type && station.geometry)){

            // remove feature from array
            stations.splice(index, 1);
        }
    }

    //remove old layer
    if(myMap.hasLayer(geoJSONlayer))
        myMap.removeLayer(geoJSONlayer);

    var myPointToLayer = function(feature, latlon) {
        var marker = new L.marker(latlon);
        marker.on('popupopen', function(e) {
            var popup = e.target.getPopup();
            var url = baseUrl + "/stations/" + feature.id;
            $.get(url).done(function(data) {
                setPopupContent(data, popup);
            });
        });
        return marker;
    };

    // add remaining geoJSON features to map
    geoJSONlayer = L.geoJson(stations, {
        pointToLayer : myPointToLayer,
        onEachFeature: function(feature, layer) {
            var stationId = feature.id;
            layer.bindPopup("Loading station '" + stationId + "' ...");
        }
    }).addTo(myMap);
    
    //zoom to extent of added features
    zoomToAllFeatures();
};

// ZOOM TO ALL AVAILABLE FEATURES

var zoomToAllFeatures = function(){
    var bounds = L.latLngBounds([]);


    var layerBounds = geoJSONlayer.getBounds();
    // extend the bounds of the collection
    // to fit the bounds of all features
    bounds.extend(layerBounds);

     // apply bounds to map variable
     myMap.fitBounds(bounds);
};


// RETRIEVE ALL PHENOMENA

// function to retrieve all phenomena
var getAllPhenomena = function(){

    // REST URL to  retrieve all available phenomena
    var allPhenomenaRequestUrl = baseUrl + "/phenomena?platformtypes=all&measurementtypes=all";

    // use jquery.ajax() to execute request
    jQuery.ajax( allPhenomenaRequestUrl, {

        dataType : "json",
        success : allPhenomenaCallback
    });
};

// fill select element with options for each phenomena
var allPhenomenaCallback = function(phenomena) {

    //first clear select and add placeholder
    $("#selectPhenomenon").empty().append('<option value="">select phenomenon to filter stations ...</option>');
    // or $('selectPhenomenon option').remove();

    //Create and append the select options using JQuery
    $("#selectPhenomenon").append(new Option("Select All", ""));
    for (var i = 0; i < phenomena.length; i++) {
        var phenomenon = phenomena[i];

        var option = new Option(phenomenon.label, phenomenon.id);
        $(option).html(phenomenon.label);
        $("#selectPhenomenon").append(option);
    }

};


// FILTER STATIONS

var filterStations = function(selectedPhenomenonOption){
    var phenomenonId = selectedPhenomenonOption.value;

    getFilteredStations(phenomenonId);
};


// RETRIEVE FILTERED STATIONS

// function to retrieve all stations
var getFilteredStations = function(phenomenonId){

    // REST URL to  retrieve all available stations
    var query = phenomenonId && "?phenomenon=" + phenomenonId;
    var getFilteredStationsRequestUrl = baseUrl + "/stations" + query;

    // use jquery.ajax() to execute request
    jQuery.ajax( getFilteredStationsRequestUrl, {

        dataType : "json",
        success : getFilteredStationsCallback
    });
};

// stations is an array of GeoJSON items!
var getFilteredStationsCallback = function(stations) {

    if(stations.length === 0){
        alert("Currently there are no weather stations to display!");
    }
    else{
        // visualize stations on map
        visualizeStations(stations);
    }
};

