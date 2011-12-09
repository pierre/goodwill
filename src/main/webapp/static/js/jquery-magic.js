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

w = {};
r = {};
t = {};

const DEFAULT_DESCRIPTION = "add a description";
const DEFAULT_NAME = "add event type name";

$(document).ready(function()
{
    objects = json_to_objects(json);
    panel_heights();

    build_eventType_table();
    w.events();
    t.events();
});

$(window).resize(function()
{
    var height = $(window).height();
    $("#resultsPane").css("height", (height - 40));
    $("#table").css("height", (height - 40));
});

w.events = function()
{
    $('#header .newET a').click(function()
    {
        eventType = prompt("Event Type:");
        var types = keys(objects);
        var not_repeated = $.grep(types, function(t, i)
        {
            return t == eventType
        }, true);

        if (eventType && (not_repeated.length == types.length)) {
            objects[eventType] = {
                active: "active",
                created_date: new Date(),
                schema: new Array()
            };

            defineObjectAndSchema();

            // rebuilt event type table
            $('#eventTypes tr').remove();
            build_eventType_table();

            // Register events
            t.events();
            var tr = $('#eventTypes tr.' + eventType);
            r.updatePaneOnSelectEvent(tr);
        }
    });

};

r.updatePaneOnSelectEvent = function(tr)
{
    // If fields were added, the schema information was hidden
    $('#schema-information').show();

    // Sroll back to top
    // TODO
    scrollToElement($('#resultsWrapper'));

    // Redraw right pane
    r.actions.wipe_rp();
    r.actions.set_rp_title(tr);
    r.actions.showButtons();
};

r.create_fields = function(fields)
{
    $.each(fields, function(index, field_obj)
    {
        e.create_element(field_obj)
    });

    r.actions.set_rp_schema();
    r.actions.set_rp_sqlSchema();
};

t.events = function()
{
    $("#title #sButtons").hide();

    $('table#eventTypes tbody tr').click(function()
    {
        if ($(this).hasClass("selected")) {
            return;
        }

        eventType = $(this).attr('name');

        defineObjectAndSchema(this);

        r.updatePaneOnSelectEvent(this);
        r.create_fields(schema);
        r.actions.highlightSelectedRow(this);
    });
};

r.events = function()
{
    $("#sButtons li#add").click(function()
    {
        var total_elements = $('#resultsPane .element.field').length + 1; // Thrift fields start at 1, not 0
        element = e.create_element({position:total_elements, status:"new"});

        // Update internal (volatile) database
        attributes = e.get_attributes(element, true);
        schema.push(attributes);

        // Enter edit mode
        e.enter_edit_mode(element, e.get_attributes(element, false), e.status(element));

        // Scroll UI (we need to enter edit mode first as it changes the height of the box)
        scrollToElement(element);
    });

    //    $('#sButtons li#deprecate').toggle(function()
    //    {
    //        // update DOM
    //        $('#resultsPane #title, .element .navBar, .element .details, .element .footer').addClass("deprecated").removeClass("active");
    //
    //        // update JSON
    //        object.active = "deprecated";
    //        $.each(schema, function(index, field)
    //        {
    //            field.active = "deprecated";
    //        })
    //
    //    }, function()
    //    {
    //        // update DOM
    //        $('#title, .element .navBar, .element .details, .element .footer').addClass("active").removeClass("deprecated");
    //
    //        // update JSON
    //        object.active = "active";
    //        $.each(schema, function(index, field)
    //        {
    //            field.active = "active";
    //        })
    //    });
};

r.actions = {
    wipe_rp:function()
    {
        $("#schema")
                .children().remove(".element");
        $("#schema-information")
                .children().remove(".element");
    },

    // Create buttons and events to take action on an event
    set_rp_title:function(tr)
    {
        var rp_title = $(tr);

        // Change color and show buttons
        if ($("#title").length > 0) {
            var prev_key = $("#title").attr("name");
            $("#title ul").remove();
            $("#title td").append($('<ul>').append($('<li>').attr('class', "eventName").attr('id', sanitizeString(prev_key)).text(prev_key)));
            $("#title").attr("id", sanitizeString(prev_key));
            r.actions.highlightSelectedRow(tr);
        }
        rp_title.attr("id", "title");


        //        <ul>
        //            <li id="name"></li>
        //            <li id="sButtons">
        //                <ul>
        //                    <li id="add"></li>
        //                    <li id="deprecate"></li>
        //                </ul>
        //            </li>
        //            <div style="clear:both;"></div>
        //        </ul>
        $("#title ul")
                .append($('<li>').attr("id", "name"))
                .append($('<li>').attr("id", "sButtons")
                .append($('<ul>')
                .append($('<li>').attr("id", "add"))
            //.append($('<li>').attr("id","deprecate"))
                )
                .append($('<div>').attr("style", "clear:both;")));

        if (object.active == "active") {
            rp_title
                    .addClass("active")
                    .removeClass("deprecated");

        }
        else {
            rp_title
                    .addClass("deprecated")
                    .removeClass("active");
        }

        $("#title #name")
                .html($("<a>")
                .attr("target", "_blank")
                .attr("href", (actionCoreURL + eventType))
                .text("HDFS"));

        r.events();
    },

    // Create box with Thrift DSL
    set_rp_schema:function()
    {
        // build string
        var string = "struct " + eventType + " &nbsp; {<br />";
        $.each(schema, function(index, field)
        {
            string += "&nbsp;&nbsp;&nbsp;&nbsp;" + field.position + ":" + fieldTypetoThriftType(field).toLowerCase() + " " + sanitizeString(field.name) + (index == schema.length - 1 ? "" : ",") + "<br />";
        });
        string += "}";

        // build schema div
        var div = $('<div class="element globalElement" id="thriftSchema">')
                .append($('<pre>')
                .html(string)
                );

        $(div).appendTo($("#schema-information"));
    },

    // Create box with the Sink information
    // See GoodwillConfig#getSinkExtraSQL()
    set_rp_sqlSchema:function()
    {
        if (schema.sink_add_info) {
            // Build schema div
            var div = $('<div class="element globalElement" id="sinkInformation">')
                    .append($('<pre>')
                    .html(schema.sink_add_info.split(";").join(";\n")) // Split on ; for pretty printing
                    );
            $(div).appendTo($("#schema-information"));
        }
    },

    showButtons
            :
            function()
            {
                $("#title #sButtons")
                        .show();
            }
    ,

    highlightSelectedRow:function(tr)
    {
        $(tr)
                .addClass("selected")
                .siblings().removeClass("selected");
    }
};

