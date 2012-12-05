/* 
 * Copyright (C) 2012 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

$(function() {
  var blogservice = $("meta[name='blog']").attr("content");

    $.getJSON(blogservice , function(data) {
        if (data) {
            var content = document.getElementById("blog");
            content.innerHTML = data.blog;
                }
        $("img").click(function(){
            $(this).load($(this).attr("src")+"&original=true");
        });
    
    });
    
  var calendarservice = $("meta[name='calendar']").attr("content");

    $.getJSON(calendarservice , function(data) {
        if (data) {
            var content = document.getElementById("calendar");
            content.innerHTML = data.calendar;
                }
        $("li").click(function(){
            var x = $(this).get();
        });
    
    });
    
});


