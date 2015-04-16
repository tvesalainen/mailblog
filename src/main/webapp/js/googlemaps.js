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
    $(".map-canvas").each(function()
    {
        var win = this;
        $.getJSON("/geojson?height="+$(win).height(), function(data)
        {
            var mapOptions = {
                center: {lat: data['latitude'], lng: data['longitude']},
                zoom: data['zoom']
            };
            var map = new google.maps.Map(win, mapOptions);
            google.maps.event.addListener(map, 'bounds_changed', function()
            {
                var href = window.location.href;
                var bounds = map.getBounds();
                var zoom = map.getZoom();
                $.getJSON("/geojson?bbox="+bounds.toUrlValue()+"&zoom="+zoom, function(data)
                {
                    var arr = data['keys'];
                    for (i in arr)
                    {
                        var key = arr[i];
                        if (!map.data.getFeatureById(key))
                        {
                            map.data.loadGeoJson(href+"/geojson?key="+key, function(array)
                            {
                                var arr = array;
                            });
                        }
                    }
                });
            });
            google.maps.event.addListener(map, 'zoom_changed', function()
            {
                var p = pixelsPerMiles(win, map);
                map.data.forEach(function(feature)
                {
                    var visible = isVisible(p, feature);
                    map.data.overrideStyle(feature, { visible : visible });
                });
            });
            map.data.setStyle(function(feature)
            {
                var p = pixelsPerMiles(win, map);
                var visible = isVisible(p, feature);
                var color;
                var opacity;
                var icon;
                color = feature.getProperty('color');
                opacity = feature.getProperty('opacity');
                icon = feature.getProperty('icon');
                return ({
                    visible : visible,
                    icon : icon,
                    strokeColor : color,
                    strokeOpacity: opacity,
                    strokeWeight : 1
                });
            });
        });
    });

    function isVisible(pmm, feature)
    {
        var minPmm = feature.getProperty('pmm');
        return (!minPmm || minPmm < pmm);
    }
    function pixelsPerMiles(win, map)
    {
        var height = $(win).height();
        var bounds = map.getBounds();
        var ne = bounds.getNorthEast();
        var sw = bounds.getSouthWest();
        var dLat = ne.lat() - sw.lat();
        return (height / dLat) / 60;
    }
});



