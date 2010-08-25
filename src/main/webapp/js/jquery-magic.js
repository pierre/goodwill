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
e = {};
r = {};
t = {};

const DEFAULT_DESCRIPTION = "add a description";
const DEFAULT_NAME = "add event type name"

$(document).ready(function()
{
    console.log(json);
    objects = w.json_to_objects(json);
    w.panel_heights();
    w.build_eventType_table();
    r.events();
    w.events();
    t.events();
    // $(".element").validate();

});

$(window).resize(function()
{
    var height = $(window).height();
    $("#resultsPane").css("height", (height - 40));
    $("#table").css("height", (height - 40));
});

function scrollToElement(el)
{
    el[0].scrollIntoView(true);
}

w.json_to_objects = function(json)
{
    var objects = {};
    $.each(json, function(index, eventType)
    {
        var name = eventType.name;
        var schema = $.map(eventType.schema, function(s, index)
        {
            return {
                active: 'active',
                description: s.description,
                field_type: s.type,
                name: s.name,
                position: s.position,
                sql_length: s.sql == null ? null : s.sql.length,
                sql_scale: s.sql == null ? null : s.sql.scale,
                sql_precision: s.sql == null ? null : s.sql.precision,
                sql_type: s.sql == null ? null : s.sql.type
            };
        });

        schema['sink_add_info'] = eventType.sinkAddInfo;

        objects[name] =
        {
            schema: schema,
            active: 'active'
        }
    });

    return objects;
};

e.events = function(element)
{
    $(element).hover(function()
    {
        $(".navBar .buttons", this).show();
    }, function()
    {
        $(".navBar .buttons", this).hide();
    });

    $(".navBar", element).click(function()
    {
        var element = $(this).closest(".element");
        var attributes = e.get_attributes(element, false);
        var index = $("#resultsPane .element").index(element);
        var object = schema[index];

        var status = e.status(element);
        e.std_mode(element) ? e.enter_edit_mode(element, attributes, status) : e.return_to_std_mode(element, object);
    });

    $(".navBar .deprecate", element).click(function(event)
    {
        // Get element and attributes
        var element = $(this).closest(".element");
        var attributes = e.get_attributes(element, false);

        // Change the divs' classes
        var divs = $('.navBar, .details, .footer', element);
        (attributes.active == "active") ? divs.removeClass("active").addClass("deprecated") : divs.removeClass("deprecated").addClass("active");

        // Reassign the attributes to the schema
        attributes = e.get_attributes(element, false);
        var index = $("#resultsPane .element").index(element);
        schema[index] = attributes;

        event.stopPropagation();
    });

    $(".details .actions .save", element).click(function()
    {
        // get element and element attributes
        var element = $(this).closest(".element");
        var new_element = e.status(element);
        var attributes = e.get_attributes(element, new_element);
        var index = $("#resultsPane .element").index(element);

        var necessary_fields_missed = ((attributes.name == "") || (attributes.description == "") || (attributes.field_type == ""));

        if (necessary_fields_missed) {
            alert("The name, description, and field_type fields need to be filled out!");
            console.log("attributes missing");
            console.log(attributes);
        }
        else {
            // adjust the schema
            if (schema.length > index) {
                schema[index] = attributes;
            }
            else {
                schema.push(attributes);
            }

            w.request(new_element);
            element.attr("_status", "");
            e.return_to_std_mode(element, attributes);

            // Hide the schema information as they need to be updated on the server side
            $('#schema-information').hide();
        }
    });

    $(".details .actions .cancel", element).click(function()
    {
        // get element
        var element = $(this).closest(".element");
        var index = $("#resultsPane .element").index(element);
        var object = schema[index];

        e.return_to_std_mode(element, object);

    });

    $(".type .dropdown", element).change(function()
    {
        var element = $(this).closest(".element");
        var attributes = e.get_attributes(element, true);

        console.log("field type changed");
        console.log(attributes);

        // Change sql dropdown and add parameter fields
        var dropdown = $(".sql .dropdown", element)
                .html(e.dropdown(attributes));

        var sql = $(".details .sql", element);
        e.param(attributes, "sql", sql);

        // e.param("", "field", footer);
    });


};

e.create_element = function(field_obj)
{
    var elementDiv = function(element)
    {
        var div = $('<div class="element field">')
                .attr("_edit", "")
                .attr("_status", element.status || "")

                .append($('<div class="navBar" style="cursor:pointer">')
                .append($('<div class="name">').text(element.name || ""))
                .append($('<div class="buttons">')
                .append($('<div class="deprecate">'))
                )
                .append('<div style="clear:both"></div>')
                )

                .append($('<div class="details">')
                .append($('<div class="description">').html(element.description || ""))

                .append($('<div class="type">')
                .append($('<ul class="list">')
                .append($("<li>").html("Thrift type: "))
                .append($('<li class="dropdown">').html(element.field_type || ""))
                )
                )

                .append($('<div class="sql">')
                .append($('<ul class="list">')
                .append($("<li>").html("SQL type: "))
                .append($('<li class="dropdown">').html(element.sql_type || ""))
                .append($('<li class="primary_parameter">').html(element.sql_precision || ""))
                .append($('<li class="secondary_parameter">').html(element.sql_scale || ""))
                )
                )
                .append($('<div class="actions">')
                .append($('<button class="cancel">').text("cancel"))
                .append($('<button class="save">').text("save"))
                )
                .append('<div style="clear:both"></div>')
                )

                .append($('<div class="footer">')
                .append($('<div class="type">')
                .append($('<ul class="list">')
                .append($("<li>").html("Thrift type: "))
                .append($('<li class="dropdown">')
                .html(element.field_type || "")
                )
                )

                )
                .append($('<div class="position">')
                .append($('<div class="text">').text("Thrift position: "))
                .append($('<div class="value">').html(element.position + "" || ""))
                )
                )

                .append('<div style="clear:both"></div>');

        (element.active == "deprecated") ? $(".navBar, .details, .footer", div).addClass("deprecated") : $(".navBar, .details, .footer", div).addClass("active");

        return div;
    };

    var element = elementDiv(field_obj);
    var attributes = e.get_attributes(element, true);

    e.events(element);
    element.appendTo($("#schema"));
    e.return_to_std_mode(element, attributes);
    $(".navBar .buttons", element).hide();

    return element;
};

e.get_attributes = function(element, create)
{
    var std_attributes = function()
    {
        var attr = {};

        attr.name = element.find(".name").html();
        attr.description = $(".description", element).html();
        attr.sql_type = $(".sql .dropdown", element).html();
        attr.field_type = $(".type .dropdown", element).html();
        attr.position = $(".footer .value", element).html();
        attr.active = $(".navBar", element).hasClass("active") ? "active" : "deprecated";
        attr.edit_mode = $(element).attr("_edit");

        if (attr.field_type == "string") {
            attr.sql_length = $(".sql .primary_parameter", element).html() || "";
            attr.sql_scale = "";
            attr.sql_precision = "";
        }
        else {
            if (attr.field_type == "decimal" || attr.field_type == "numeric") {
                attr.sql_length = "";
                attr.sql_precision = $(".sql .primary_parameter", element).html();
                attr.sql_scale = $(".sql .secondary_parameter", element).html();
            }
            else {
                attr.sql_length = "";
                attr.sql_scale = "";
                attr.sql_precision = "";
            }
        }

        return attr;
    };

    var edit_attributes = function(create)
    {
        var attr = {};
        console.log("edit mode");

        attr.name = $(".name input", element).val();
        attr.description = $(".description textarea", element).val();
        attr.field_type = create ? $(".type .dropdown select option:selected", element).text() : $(".type .dropdown", element).html();
        attr.sql_type = $(".sql .dropdown select option:selected", element).text();
        attr.position = $(".footer .value", element).html();
        attr.active = $(".navBar", element).hasClass("active") ? "active" : "deprecated";
        attr.edit_mode = element.attr("_edit") || "";

        // remove default values for name and description
        attr.name = (attr.name != DEFAULT_NAME) ? attr.name : "";
        attr.description = (attr.description != DEFAULT_DESCRIPTION) ? attr.description : "";

        // get sql length, scale, and precision
        console.log("this is a " + attr.field_type);
        if (attr.field_type == "string") {
            attr.sql_length = $(".sql .primary_parameter input", element).val() || "";
            attr.sql_scale = "";
            attr.sql_precision = "";
        }
        else {
            if (attr.field_type == "double") {
                attr.sql_length = "";
                attr.sql_scale = $(".sql .primary_parameter input", element).val();
                attr.sql_precision = $(".sql .secondary_parameter input", element).val();
            }
            else {
                attr.sql_length = "";
                attr.sql_scale = "";
                attr.sql_precision = "";
            }
        }

        console.log("fetched attribute values for " + attr.name);
        console.log(attr);

        return attr
    };

    return e.std_mode(element) ? std_attributes() : edit_attributes(create);
};

e.std_mode = function(element)
{
    return element.attr("_edit") == "";
};

e.status = function(element)
{
    return element.attr("_status") == "new";
};

e.enter_edit_mode = function(element, attr, create)
{
    console.log("enter edit mode");

    // FIX element attributes
    $(".navBar .name", element)
            .addClass("edit")
            .html(
            $('<input class="input_name">')
                    .val(attr.name || DEFAULT_NAME)
                    .focus(function()
            {
                if ($(this).val() == DEFAULT_NAME) {
                    $(this).val("");
                }
            })
            );

    $(".details .description", element)
            .addClass("edit")
            .html(
            $("<textarea>")
                    .val(attr.description || DEFAULT_DESCRIPTION)
                    .focus(function()
            {
                if ($(this).val() == DEFAULT_DESCRIPTION) {
                    $(this).val("");
                }
            }));

    $(".details .sql .dropdown", element)
            .html(
            e.dropdown(attr)
            );

    var sql = $(".details .sql", element);
    e.param(attr, "sql", sql);

    if (create) {
        $('.details .type', element)
                .show();
        $('.details .type .dropdown', element)
                .html(
                e.footer_dropdown(attr)
                );
    }

    // add event handlers to new elements
    $('.navBar input', element).click(function(event)
    {
        event.stopPropagation();
    });

    // SHOW actions and details pane
    $(".details", element).show();
    $(".details .actions", element).show();
    $(".navBar .buttons", element).show();
    $(element).attr("_edit", "edit");
};

e.return_to_std_mode = function(element, attr)
{
    // FIX element attributes
    $(".name", element)
            .removeClass("edit")
            .html(attr.name || "");

    $(".details .description", element)
            .removeClass("edit")
            .html(attr.description || "");

    $(".details .type", element).hide();

    $(".sql .dropdown", element)
            .html(attr.sql_type || "");

    $(".sql .primary_parameter", element)
            .html(attr.sql_length || attr.sql_scale || "");

    $(".sql .secondary_parameter", element)
            .html(attr.sql_precision || "");

    $('.type .dropdown', element)
            .html(attr.field_type || "");

    // HIDE actions and details pane
    if ($(".description", element).html() == "" && ($(".sql .dropdown", element).text() == "type" || $(".sql .dropdown", element).text() == "")) {
        $(".details", element).hide();
    }

    $(".details .actions", element).hide();
    $(element).attr("_edit", "");

};


e.dropdown = function(attr)
{
    var dropdown = $("<select>");

    // Netezza specific!
    var type_map = {
        ""  : [],
        "string" : ["nvarchar", "varchar"],
        "bool" : ["boolean"],
        "byte" : ["byteint"],
        "i16" : ["smallint"],
        "i32" : ["integer"],
        "i64" : ["bigint", "date", "datetime", "timestamp"],
        "double" : ["numeric", "decimal"]
    };


    var build_dropdown = function(dropdown, types, sql_type)
    {
        $.each(types, function(index, type)
        {
            var option = $("<option>").val(type).text(type);

            if (type == sql_type) {
                option.attr("selected", "selected");
            }

            $(dropdown).append(option);
        });

        return dropdown;
    };

    var possible_types = type_map[attr.field_type];
    var sql_type = attr.sql_type;

    return build_dropdown(dropdown, possible_types, sql_type);
}

e.footer_dropdown = function()
{
    var dropdown = $("<select>");
    var options = ["", "string", "bool", "byte", "i16", "i32", "i64", "double"];

    $.each(options, function(index, option)
    {
        var row = $("<option>").val(option).text(option);
        $(dropdown).append(row);
    });

    return dropdown;
};

e.param = function(attributes, type, form)
{

    // var option = $(".dropdown select option:selected", form).text();

    var field_type = attributes.field_type;
    var length = eval("attributes." + type + "_length") || "";
    var scale = eval("attributes." + type + "_scale") || "";
    var precision = eval("attributes." + type + "_precision") || "";

    console.log("length: " + length);

    if (field_type == "string") {
        $(".primary_parameter", form)
                .html($("<input>").val(length));
        $(".secondary_parameter", form)
                .html("");

    }
    else {
        if (field_type == "double" || field_type == "decimal") {
            $(".primary_parameter", form)
                    .html($("<input>").val(scale));
            $(".secondary_parameter", form)
                    .html($("<input>").val(precision));

        }
        else {
            $(".primary_parameter", form)
                    .html("");
            $(".secondary_parameter", form)
                    .html("");
        }
    }
};

w.panel_heights = function()
{
    var height = $(window).height();
    $("#resultsPane").css("height", (height - 40));
    $("#table").css("height", (height - 40));
};

w.build_eventType_table = function()
{
    sorted_keys = keys(objects).sort();

    for (var i in sorted_keys) {
        key = sorted_keys[i];
        $('table#eventTypes').append(
                $('<tr style="cursor:pointer">')
                        .attr('name', key)
                        .append($('<td>').text(key))
                );
    }
};

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
            w.build_eventType_table();

            // Register events
            t.events();
            var tr = $('#eventTypes tr.' + eventType);
            r.updatePaneOnSelectEvent(tr);
        }
    });

};

r.updatePaneOnSelectEvent = function(tr)
{
    r.actions.wipe_rp();
    r.actions.set_rp_title();
    r.actions.showButtons();
    r.actions.highlightSelectedRow(tr);
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

function defineObjectAndSchema()
{
    object = objects[eventType];
    schema = object.schema;
}

t.events = function()
{
    $("#resultsPane #sButtons").hide();

    $('table#eventTypes tbody tr').click(function()
    {
        eventType = $(this).attr('name');

        defineObjectAndSchema(this);

        r.updatePaneOnSelectEvent(this);
        r.create_fields(schema);
    });
};

r.events = function()
{
    $("#resultsPane #title li#add").click(function()
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

    $('#resultsPane #title li#deprecate').toggle(function()
    {
        // update DOM
        $('#resultsPane #title, .element .navBar, .element .details, .element .footer').addClass("deprecated").removeClass("active");

        // update JSON
        object.active = "deprecated";
        $.each(schema, function(index, field)
        {
            field.active = "deprecated";
        })

    }, function()
    {
        // update DOM
        $('#resultsPane #title, .element .navBar, .element .details, .element .footer').addClass("active").removeClass("deprecated");

        // update JSON
        object.active = "active";
        $.each(schema, function(index, field)
        {
            field.active = "active";
        })
    });
};

r.actions = {
    wipe_rp:function()
    {
        $("#schema")
                .children().remove(".element");
        $("#schema-information")
                .children().remove(".element");
    },

    set_rp_title:function()
    {
        var rp_title = $("#resultsPane #title");
        // Remove the help text
        $("#resultsPane #title #schema_title").remove();

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

        $("#resultsPane #title #name")
                .html($("<a>")
                .attr("target", "_blank")
                .attr("href", ("http://action.ninginc.com:8080/rest/1.0/hdfs?dir=/events/ning/" + eventType))
                .text(eventType));
    },

    // Create box with Thrift DSL
    set_rp_schema:function()
    {
        // build string
        var string = "struct " + camelizeString(sanitizeString(eventType)) + " &nbsp; {<br />";
        $.each(schema, function(index, field)
        {
            string += "&nbsp;&nbsp;&nbsp;&nbsp;" + field.position + ": " + field.field_type + " " + sanitizeString(field.name) + (index == schema.length - 1 ? "" : ",") + "<br />";
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
                $("#resultsPane #sButtons")
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

keys = function(obj)
{
    accumalator = [];
    for (key in obj) {
        accumalator.push(key);
    }
    return accumalator;
};

function camelizeString(string)
{
    var a = string.split('_'), i;
    var s = [];
    for (i = 0; i < a.length; i++) {
        s.push(a[i].charAt(0).toUpperCase() + a[i].substring(1));
    }
    s = s.join('');
    return s;
}

function sanitizeString(stringToSanitize)
{
    return stringToSanitize.toLowerCase().replace(/ /g, "_");
}

w.request = function(new_element)
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
        console.log("create eventType");
        $.ajax({
            type: 'PUT',
            url: '/registrar',
            data: $.toJSON({name: eventType, schema: transformed_schema}),
            success: function()
            {
                console.log("successfully posted eventType")
            },
            error: function(XMLHttpRequest, textStatus, errorThrown)
            {
                console.log("failed");
                console.log(textStatus);
            },
            dataType: "json",
            contentType: "application/json"
        });

    }
    else {
        console.log("updating eventType");
        $.ajax({
            type: 'PUT',
            url: '/registrar',
            data: $.toJSON({name: eventType, schema: transformed_schema}),
            success: function()
            {
                console.log("successfully put eventType")
            },
            error: function(XMLHttpRequest, textStatus, errorThrown)
            {
                console.log(textStatus);
                console.log(XMLHttpRequest);
                console.log(errorThrown);
            },
            dataType: "json",
            contentType: "application/json"
        });
    }
};