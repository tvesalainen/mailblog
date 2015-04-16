/* 
 * Copyright (C) 2015 tkv
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

$(document).ready(function()
{
    var map;
    
    google.maps.event.addDomListener(window, 'load', googlemaps);
    
    function googlemaps()
    {
        $.getJSON("/lastPosition?json=true", function(data)
        {
            var mapOptions = {
                center: {lat: data['latitude'], lng: data['longitude']},
                zoom: 8
            };
            map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
            google.maps.event.addListener(map, 'bounds_changed', boundsChanged);
            map.data.setStyle(function(feature)
            {
                var color;
                var opaque;
                var icon;
                color = feature.getProperty('color');
                opaque = feature.getProperty('opaque');
                icon = feature.getProperty('icon');
                return ({
                    icon : icon,
                    strokeColor : color,
                    strokeOpacity: opaque,
                    strokeWeight : 1
                });
            });
        });
    }

    function boundsChanged()
    {
        var href = window.location.href;
        var bounds = map.getBounds();
        var zoom = map.getZoom();
        $.getJSON("/geojson?bbox="+bounds.toUrlValue()+"&zoom="+zoom, function(data)
        {
            var i;
            var arr = data['keys'];
            for (key in arr)
            {
                map.data.loadGeoJson(href+"/geojson?key="+arr[key], function(array)
                {
                    var arr = array;
                });
            }
        });
    }
    
});



