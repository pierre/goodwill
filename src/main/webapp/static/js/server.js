/*
 * Copyright 2010 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/*
 * AJAX functions
 */

server = {};

server.request = function(new_element)
{
    transformed_schema = $.map(schema, function(s, index)
    {
        return {
            name: s.name,
            type: s.field_type,
            position: parseInt(s.position),
            description: s.description,
            sql: {
                scale: parseInt(s.sql_scale),
                precision: parseInt(s.sql_precision),
                length: parseInt(s.sql_length),
                type: s.sql_type
            }
        }
    });

    if (new_element) {
        $.ajax({
            type: 'PUT',
            url: '/registrar',
            data: $.toJSON({name: eventType, schema: transformed_schema}),
            success: function()
            {
                $("#ajax_message p").remove();
                $("#ajax_message").append($('<p>').attr("id", "ajax_success").text("Data saved!"));
                $("#ajax_success").fadeOut(2000);
            },
            error: function(XMLHttpRequest, textStatus, errorThrown)
            {
                console.log(XMLHttpRequest.statusText);
                alert("There was a problem saving data: " + textStatus + " (" + XMLHttpRequest.statusText + ")");
            },
            dataType: "text", // No data is sent back
            contentType: "application/json"
        });
    }
    else {
        $.ajax({
            type: 'PUT',
            url: '/registrar',
            data: $.toJSON({name: eventType, schema: transformed_schema}),
            success: function()
            {
                $("#ajax_message p").remove();
                $("#ajax_message").append($('<p>').attr("id", "ajax_success").text("Data saved!"));
                $("#ajax_success").fadeOut(2000);
            },
            error: function(XMLHttpRequest, textStatus, errorThrown)
            {
                alert("There was a problem saving data: " + textStatus + " (" + XMLHttpRequest + ")");
            },
            dataType: "text", // No data is sent back
            contentType: "application/json"
        });
    }
};
