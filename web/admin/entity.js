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
       var action = $(this).attr("action");
       $(this).contents(".fields").load(action);
       $(this).contents(".select").load(action+"?select=true");
    });
    
    $("form").on("click", ".new", function(event)
    {        
       var action = $(event.delegateTarget).attr("action");
       var txt = $(this).text();
       var name = prompt(txt, "");
       if (name != null && name != "")
        {
           $(event.delegateTarget).contents(".fields").load(action+"?new="+name);
        }
    });

    $("form").on("change", ".entitySelect", function(event)
    {        
       var action = $(event.delegateTarget).attr("action");
       var key = $(".entitySelect option:selected").val();
        $(event.delegateTarget).contents(".fields").load(action+"?key="+key);
    });

    $("form").on("change", ".backupSelect", function(event)
    {        
       var action = $(event.delegateTarget).attr("action");
       var key = $(".backupSelect option:selected").val();
       $.get(action+"?backup="+key, function(data)
       {
          $('[name="Page"]').each(function()
            {
              $(this).text(data);
            });
       });
    });

    $("form").submit(function() 
    {
       $(this).contents(".mandatory").each(function()
       {
          var val = $(this).val();
          if (val == "")
          {
              $(this).focus();
          }
       });
       var action = $(this).attr("action");
       $.post(action, $(this).serialize(), function(data, textStatus, jqXHR)
        {
            if (textStatus != "success")
            {
                alert(textStatus);
            }
        });
       return false; 
    });

});