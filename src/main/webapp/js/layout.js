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
 * Utilities
 */

function scrollToElement(el)
{
    el[0].scrollIntoView(true);
}

keys = function(obj)
{
    accumalator = [];
    for (key in obj) {
        accumalator.push(key);
    }
    return accumalator;
};

/**
 * Convert a field type, as defined in SchemaFieldType, to a Thrift type.
 *
 * @param field String representation of a SchemaFieldType
 * @See SchemaFieldType in com.ning:metrics.serialization
 */
function fieldTypetoThriftType(field)
{
    if (field.field_type.toLowerCase() == "date") {
        return "i64";
    }
    else {
        if (field.field_type.toLowerCase() == "ip") {
            return "string";
        }
        else {
            return field.field_type;
        }
    }
}

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

function json_to_objects(json)
{
    var objects = {};
    $.each(json.types, function(index, eventType)
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
}

function defineObjectAndSchema()
{
    object = objects[eventType];
    schema = object.schema;
}

/*
 * Generic layout controls
 */

function panel_heights()
{
    var height = $(window).height();
    $("#resultsPane").css("height", (height - 40));
    $("#table").css("height", (height - 40));
}

/*
 * Create the left (static) table
 */
function build_eventType_table()
{
    sorted_keys = keys(objects).sort();

    for (var i in sorted_keys) {
        key = sorted_keys[i];
        $('table#eventTypes').append(
                $('<tr style="cursor:pointer">')
                        .attr('name', key)
                        .append($('<td>').append($('<ul>').append($('<li>').attr('class', "eventName").attr('id', sanitizeString(key)).text(key))))
                );
    }
}