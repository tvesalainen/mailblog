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


$(document).ready(function(){

    var search = window.location.search;
    if (!search)
    {
        search = "";
    }
    $.get("/opengraph"+search, function(data, status)
    {
        $("head").append(data);
    });
    
    $("#blog").load("/blog"+search, function(){
        afterLoad();
    });

    var pending = false;
    $(window).scroll(function()
    {
        if (!pending)
        {
            var top = $(window).scrollTop();
            var dh = $(document).height();
            var wh = $(window).height();
            if(top > 0 && top >= dh - wh)
            {
                var href = $("#blog").find(".lasthref").last().attr("href");
                if (href)
                {
                    $("#blog").append("<div class='appendhere'></div>");
                    pending = true;
                    $("#blog").find(".appendhere").last().load(href, function()
                    {
                        pending = false;
                    });
                }
            }
        }
    });
    
    $("#calendar").load("/blog?calendar=true", function()
    {
        $(".hidden").hide();
    });
    
    $(".keywordSelect").load("/blog?keywords=true");
    
    $(".lastPosition").load("/lastPosition");

    $("#blog").on("click", "img", function(event)
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
        $("#blog").html("");
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
        $("#blog").load("/blog", function(){
            afterLoad();
        });
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
    $(".hidden").hide();
}
