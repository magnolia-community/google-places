[#assign places = model.items!]
[#if places?has_content]
<style>
    #map-canvas {
        height: ${content.height!"600"}px;
        width: 100%;
        margin: 0px;
        padding: 0px
    }
</style>
<script type="text/javascript"
        src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=places"></script>
<script type="text/javascript">
    // more details at https://developers.google.com/maps/documentation/javascript/reference
    var map;
    var service;
    var infowindow;
    var bounds = new google.maps.LatLngBounds();
    var mapItems;

    function initialize() {

        var center = new google.maps.LatLng(1, 1);


        map = new google.maps.Map(document.getElementById('map-canvas'), {
            center: center,
            // 0 == full map
            zoom: ${content.zoomLevel!"0"},
            mapTypeId: ${content.mapType!"google.maps.MapTypeId.SATELLITE"}
        });

        service = new google.maps.places.PlacesService(map);

        mapItems = [];
            [#list places as place]
                [#assign placeContentMap = cmsfn.asContentMap(place)]
                [#assign name = (placeContentMap.place!"")?js_string]
                [#assign street = (placeContentMap.street!"")?js_string]
                [#assign city = (placeContentMap.city!"")?js_string]
                [#assign country = (placeContentMap.country!"")?js_string]
                [#assign latitude = (placeContentMap.latitude!"")?js_string]
                [#assign longitude = (placeContentMap.longitude!"")?js_string]
                [#assign icon = (model.getIconForPlace(place)!"")?js_string]
                [#if latitude?has_content && longitude?has_content]
                    createMarker('${latitude}', '${longitude}', null, '${name}[#if street?has_content], [/#if]${street}, ${city}[#if country?has_content], [/#if]${country}', '${icon}');
                [#else]
                   mapItems.push({
                        query: '${street} [#if street?has_content], [/#if]${city}[#if country?has_content], [/#if]${country}',
                       address: '${street} [#if street?has_content], [/#if]${city}',
                      icon: '${icon}',
                      name: '${name}'
                  });
                [/#if]
            [/#list]


        initiateSearch(mapItems, 0);
        setTimeout(function() { document.getElementById("message").style.display = "none" }, mapItems.length * 50);
    }

    var runningCount = 0

    function initiateSearch(mapItems, index) {
        for (var i = index; i < mapItems.length; i++) {
            // in theory google allows up to 10 requests in a second, in practice ...
            if (runningCount > 3) {
                setTimeout(function() {initiateSearch(mapItems, i)}, 1000);
                break;
            } else {
                runningCount++;
                service.textSearch(mapItems[i], callback);
            }
        }
    }

    function callback(results, status) {
        runningCount--;
        if (status == google.maps.places.PlacesServiceStatus.OK) {
            for (var i = 0; i < results.length; i++) {
                var place = results[i];

            var magnoliaPlace = findPlace(place.formatted_address);
            var title = place.formatted_address;
                createMarker(place.geometry.location.lat(), place.geometry.location.lng(), magnoliaPlace, title);
                // interested in first result only
                break;
            }
        } else if (status != google.maps.places.PlacesServiceStatus.ZERO_RESULTS) {
          alert(" It would seem we are loading too many requests too fast. You might want to buy access from google to display so many places ... or set Lat/Lon position manually. Status was: " + status);
        }
    }

    function createMarker(lat, lng, magnoliaPlace, title, icon) {
        var location = new google.maps.LatLng(lat, lng);
        bounds.extend(location);
        if (icon == null) {
            icon = "";
        }
        if (magnoliaPlace != null) {
          title = magnoliaPlace.name + ", " + title;
          icon = magnoliaPlace.icon;
        }
        var marker = new google.maps.Marker({
            position: location,
            map: map,
            title: title,
            icon: icon
        });
        centerMap();
    }

    function centerMap() {
        map.setCenter(bounds.getCenter());
        map.fitBounds(bounds);
    }

    function findPlace(address) {
        var match = 20000;
        var hit = null;
        for (var i = 0; i < mapItems.length; i++) {
            mapItem = mapItems[i];
            distance = getEditDistance(address, mapItem.address);
            if (distance < match) {
              match = distance;
              hit = mapItem;
            }
        }
        return hit;
    }

    google.maps.event.addDomListener(window, 'load', initialize);

// getEditDistance function is originally from http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#JavaScript
// ... some rights might be reserved, check source page to be sure.
// ... but edits are all mine :)
function getEditDistance(a, b) {
  if(a.length === 0) return b.length;
  if(b.length === 0) return a.length;
  a = a.trim();
  b = b.trim();

  var matrix = [];

  // increment along the first column of each row
  var i;
  for(i = 0; i <= b.length; i++){
    matrix[i] = [i];
  }

  // increment each column in the first row
  var j;
  for(j = 0; j <= a.length; j++){
    matrix[0][j] = j;
  }

  // Fill in the rest of the matrix
  for(i = 1; i <= b.length; i++){
    for(j = 1; j <= a.length; j++){
      if(b.charAt(i-1) == a.charAt(j-1)){
        matrix[i][j] = matrix[i-1][j-1];
      } else {
        matrix[i][j] = Math.min(matrix[i-1][j-1] + 1, // substitution
                                Math.min(matrix[i][j-1] + 1, // insertion
                                         matrix[i-1][j] + 1)); // deletion
      }
    }
  }

  var  discount = 0;
  if (a.indexOf(b) > -1 || b.indexOf(a) > -1) {
    discount = 5;
  }
  //  alert("a:"+a + ", b:"+b + ", \ndist:" + (matrix[b.length][a.length]) + "\n disc:" + discount);
  return matrix[b.length][a.length] - discount;
};
</script>
<div id="message"><span>Please be patient, in order to not overload free API usage, loading of map results might take a while depending on number of results ...</span></div>
<div style="width: 100%;"><div id="map-canvas"></div>
</div>
[/#if]