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
 * along with this program.  If not, see <http://www.gnu.org/licenses></http:>.
 */

var pageStack = new Array();

$(document).ready(function(){

    $("#blog").load("/blog", function(){
        $(".hidden").hide();
    });
    
    $("#calendar").load("/blog?calendar=true", function(){
        $(".hidden").hide();
    });
    
    $(".keywordSelect").load("/blog?keywords=true");
    
    $("body").on("click", "img", function(event){        
        window.open($(this).attr("src")+"&original=true");
    });

    $("body").on("change", ".search", function(event){        
        var search = $(this).val();
        $.post("/blog", {search: search }, function(data){
            $("#blog").html(data);
            $(".hidden").hide();
        });
    });

    $("body").on("change", ".keywordSelect", function(event){    
        var target = event.delegateTarget;
        var search = $(target).find(".keywordSelect option:selected").val();    
        $.post("/blog", {search: search }, function(data){
            $("#blog").html(data);
            $(".hidden").hide();
        });
    });

    $("body").on("click", ".calendar-menu", function(event){     
        var x = $(this).get(0);
        var id = x.id;
        var cls = "."+id;
        $(cls).toggle();
        $(cls).removeClass("hidden");
        if ("year" != id.substring(0, 4))
        {
            var disp = $(cls).css("display");
            if (disp != "none")
            {
                pageStack.length = 0;
                pageStack.push(id);
                $("#blog").load("/blog?cursor="+id, function(){
                    afterLoad();
                });
            }
        }
    });

    $("body").on("click", ".blog-entry", function(event){        
        var x = $(this).get(0);
        $("#blog").load("/blog?blog="+x.id, function(){
            afterLoad();
        });
    });

    $("body").on("click", ".top", function(event){        
        $("#blog").load("/blog", function(){
            afterLoad();
        });
        pageStack.length = 0;
    });

    $("body").on("click", ".backward", function(event){        
        var cursor = $("#nextPage").text();
        if (cursor) 
        {
            pageStack.push(cursor);
            $("#blog").load("/blog?cursor="+cursor, function(){
                afterLoad();
            });
        }
    });

    $("body").on("click", ".forward", function(event){        
        if (pageStack.length > 0)
        {
            if (pageStack.length == 1)
            {
                $("#blog").load("/blog", function(){
                    afterLoad();
                });
            }
            else
            {
                var cursor = pageStack[pageStack.length-2];
                $("#blog").load("/blog?cursor="+cursor, function(){
                    afterLoad();
                });
            }
            pageStack.pop();
        }
    });

    
});

function afterLoad()
{
    var cursor = $("#nextPage").text();
    if (cursor)
    {
        $(".backward").each(function()
        {
            $(this).attr("disabled", false);
        });
    }
    else
    {
        $(".backward").each(function()
        {
            $(this).attr("disabled", true);
        });
    }
    if (pageStack.length > 0)
    {
        $(".forward").each(function()
        {
            $(this).attr("disabled", false);
        });
    }
    else
    {
        $(".forward").each(function()
        {
            $(this).attr("disabled", true);
        });
    }
    $(".hidden").hide();
}
