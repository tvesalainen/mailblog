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

$(document).ready(function(){

    $("form").each(function()
    {
        var target = this;
        var loaded = "";
        var action = $(this).attr("action");
        $(this).find(".fields").load(action, function(text, status, req)
        {
            loaded = status;
        });
        $(this).find(".select").load(action+"?select=true", function(text, status, req)
        {
            if (loaded != "success")
            {
                var key = $(target).find(".entitySelect option:selected").val();
                $(target).find(".fields").load(action+"?key="+key);
            }
        });
    });
    
    $("form").on("click", ".new", function(event)
    {        
        var action = $(event.delegateTarget).attr("action");
        var txt = $(this).text();
        var name = prompt(txt, "");
        if (name != null && name != "")
        {
            $(event.delegateTarget).find(".fields").load(action+"?new="+name);
            //$(event.delegateTarget).find(".select").load(action+"?select=true");
        }
    });

    $("form").on("click", ".delete", function(event)
    {        
        var target = event.delegateTarget;
        var deleteTarget = $(target).find(".entityId").text();
        var res = confirm("Delete "+deleteTarget+"?");
        if (res == true)
        {
            var action = $(event.delegateTarget).attr("action");
            var key = $(event.delegateTarget).find("[name='key']").val();
            $.post(action+"?delete="+key, function(data, textStatus, jqXHR)
            {
                if (textStatus != "success")
                {
                    alert(textStatus);
                }
                else
                {
                    $(target).find(".fields").text("");
                    $(target).find(".select").load(action+"?select=true");
                 }
            });
        }
    });

    $("form").on("change", ".entitySelect", function(event)
    {        
        var target = event.delegateTarget;
        var action = $(target).attr("action");
        var key = $(target).find(".entitySelect option:selected").val();
        $(target).find(".fields").load(action+"?key="+key);
    });

    $("form").on("change", ".backupSelect", function(event)
    {        
        var action = $(event.delegateTarget).attr("action");
        var key = $(event.delegateTarget).find(".backupSelect option:selected").val();
        $.get(action+"?backup="+key, function(data)
        {
            $('[name="Page"]').each(function()
            {
                $(this).text(data);
            });
        });
    });

    $("form").on("click", ".submit", function(event)
    {
        var target = event.delegateTarget;
        $(target).find(".mandatory").each(function()
        {
            var val = $(this).val();
            if (val == "")
            {
                $(this).focus();
            }
        });
        var action = $(target).attr("action");
        $.post(action, $(target).serialize(), function(data, textStatus, jqXHR)
        {
            if (textStatus != "success")
            {
                alert(textStatus);
            }
            else
            {
                var key = $(target).find("[name='key']").val();
                $(target).find(".fields").load(action+"?key="+key);
                $(target).find(".select").load(action+"?select=true");
             }
        });
    });

    $("form").submit(function() 
    {
        return false; 
    });
    
});