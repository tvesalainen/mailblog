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
var firstPage = null;

$(document).ready(function(){

    var search = window.location.search;
    if (!search)
    {
        search = "";
    }
    $("#blog").load("/blog"+search, function(){
        afterLoad();
    });
    
    $("#calendar").load("/blog?calendar=true", function(){
        $(".hidden").hide();
    });
    
    $(".keywordSelect").load("/blog?keywords=true");
    
    $("body").on("click", "img", function(event)
   {        
        window.open($(this).attr("src")+"&original=true");
    });

    $("body").on("click", ".print-all", function(event)
   {        
        var win = window.open("/?all");
        win.print();
    });

    $("body").on("click", ".add-comment", function(event){        
        var target = event.target;
        var prev = $(this).prev();
        var id = prev.attr("id");
        var key=id.substring(13);
        var text = prev.val();
        $.post("/blog", {blog: key, comment: text }, function(data){
            if (data)
            {
                 window.open(data);
            }
            else
            {
                afterLoad();
            }
        });
    });

    $("body").on("click", ".delete-comment", function(event){        
        var id = $(this).attr("id");
        $.post("/blog", {removecomment: id}, function(data){
            afterLoad();
        });
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
                firstPage = id;
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
        firstPage = null; 
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
                if (firstPage)
                {
                  $("#blog").load("/blog?cursor="+firstPage, function(){
                      afterLoad();
                  });
                }
                else
                {
                  $("#blog").load("/blog", function(){
                      afterLoad();
                  });
                }
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
    $(".comments").each(function()
    {
        var id = $(this).attr("id");
        var key=id.substring(9);
        $("#comments-"+key).load("/blog?comments=true&blog="+key, function(){
            $(".hidden").hide();
        });
    });
    var cursor = $("#nextPage").text();
    if (cursor)
    {
        $(".backward").show();
    }
    else
    {
        $(".backward").hide();
    }
    if (pageStack.length > 0)
    {
        $(".forward").show();
    }
    else
    {
        $(".forward").hide();
    }
    $(".hidden").hide();
}
