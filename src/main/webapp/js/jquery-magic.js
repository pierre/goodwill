w = {};
e = {};
r = {};
t = {};


$(document).ready(function(){	

  objects = json;
  w.panel_heights();
  w.build_eventType_table();
  r.events();
  w.events();
  t.events();
  // $(".element").validate();
  
});

$(window).resize(function() {
  var height = $(window).height();	
	$("#resultsPane").css("height", (height - 40));
	$("#table").css("height", (height - 40));
});


e.events = function(element){
  $(element).hover(function(){
    $(".navBar .buttons", this).show();
  }, function(){
    $(".navBar .buttons", this).hide();
  });

  $(".navBar", element).click(function(){

    var element = $(this).closest(".element");
    var attributes = e.get_attributes(element, false);
    var index  = $("#resultsPane .element").index(element);
    var object = schema[index];
    
    var status = e.status(element);
    e.std_mode(element) ? e.enter_edit_mode(element, attributes, status) :  e.return_to_std_mode(element, object);
    
  });

  $(".navBar .deprecate", element).click(function(event){
    
    // get element and attributes
    var element = $(this).closest(".element");
    var attributes = e.get_attributes(element, false);

    // change the divs' classes
    var divs = $('.navBar, .details, .footer', element);
    (attributes.active == "active") ? divs.removeClass("active").addClass("deprecated") :  divs.removeClass("deprecated").addClass("active");
    
    // reassign the attributes to the schema
    var attributes = e.get_attributes(element, false);
    var index  = $("#resultsPane .element").index(element);
    schema[index] = e.save_to_json(attributes, schema[index]);

    event.stopPropagation();
  });

  $(".details .actions .save", element).click(function(){

    // get element and element attributes
    var element = $(this).closest(".element");
    var status = e.status(element);
    var attributes = e.get_attributes(element, status);
    var index  = $("#resultsPane .element").index(element);
        
    // adjust the schema
    if(schema.length > index) {
      schema[index] = e.save_to_json(attributes, schema[index]);
    } else {
      schema.push(e.save_to_json(attributes, {}));
    }
    
    
    element.attr("_status", "");    
    e.return_to_std_mode(element, attributes);
  });

  $(".details .actions .cancel", element).click(function(){

    // get element
    var element = $(this).closest(".element");
    var index  = $("#resultsPane .element").index(element);
    var object = schema[index];

    e.return_to_std_mode(element, object);

  });

  $(".sql .dropdown", element).change(function(){
    element = $(this).closest(".element");
    var sql_param = $(".details .sql", element);
    e.param(element, "", sql_param);
  });
  
  $(".type .dropdown", element).change(function(){
    element = $(this).closest(".element");
    var type_param = $(".footer .type", element);
    e.param(element, "", type_param);
  });
  
  
}

e.create_element = function(field_obj){

  var elementDiv = function(element) {
    var div = $('<div class="element">')
      .attr("_edit", "")
      .attr("_status", element.status || "")

       .append($('<div class="navBar">')
         .append($('<div class="name">').text(element.name || ""))
         .append($('<div class="buttons">')
          .append($('<div class="deprecate">'))
         )
         .append('<div style="clear:both"></div>')
       )

       .append($('<div class="details">')
  	     .append($('<div class="description">').html(element.description || ""))
         .append($('<div class="sql">')
           .append($('<ul class="list">')
              .append($("<li>").html("sql: "))
              .append($('<li class="dropdown">').html(element.sql_type || ""))
              .append($('<li class="param">').html(element.sql_param || ""))
           )
         )
  	     .append($('<div class="actions"><ul>')
  	       .append($('<li class="save">').text("save"))
  	       .append($('<li class="cancel">').text("cancel"))
  	     )
       )

       .append($('<div class="footer">')
         .append($('<div class="type">')
           .append($('<ul class="list">')
              .append($("<li>").html("type: "))
              .append($('<li class="dropdown">').html(element.eType || ""))
              .append($('<li class="param">').html(element.eParam || ""))
           )         
         )
         .append($('<div class="position">')
           .append($('<div class="text">').text("position: "))
           .append($('<div class="value">').html(element.position + "" || ""))
         )
       )
       

    .append('<div style="clear:both"></div>');



    (element.active == "deprecated") ? $(".navBar, .details, .footer", div).addClass("deprecated") : $(".navBar, .details, .footer", div).addClass("active");

    return div;

  }
  
  var element = elementDiv(field_obj);
  var attributes = e.get_attributes(element, true);

  console.log(attributes);
  
  e.events(element);
  element.appendTo($("#resultsPane"));
  e.return_to_std_mode(element, attributes);
  $(".navBar .buttons", element).hide();
  
  return element;
}

e.get_attributes = function(element, create){
  
  var std_attributes = function(){
    
    var attr = {};
    
    attr.name        = element.find(".name").html();
    attr.description = $(".description", element).html();
    attr.sql_type    = $(".sql .dropdown", element).html();
    attr.sql_param   = $(".sql .param", element).html();
    attr.eType       = $(".type .dropdown", element).html();
    attr.eParam      = $(".type .param", element).html();
    attr.position    = $(".footer .value", element).html();
    attr.active      = $(".navBar", element).hasClass("active") ? "active" : "deprecated";
    attr.edit_mode   = $(element).attr("_edit");
    
    return attr;
  }

  var edit_attributes = function(create){
    
    var attr = {};
    
    attr.name        = $(".name input", element).val();
    attr.description = $(".description textarea", element).val();
    attr.sql_type    = $(".sql .dropdown select option:selected", element).text();
    attr.sql_param   = $(".sql .param input", element).val();
    attr.position    = $(".footer .value", element).html();
    attr.active      = $(".navBar", element).hasClass("active") ? "active" : "deprecated";
    attr.edit_mode   = element.attr("_edit") || "";
    
    if(create){
      console.log("create");
      attr.eType       = $(".type .dropdown select option:selected", element).text();
      attr.eParam      = $(".type .param input", element).val();
    } else {
      console.log("other");
      attr.eType       = $(".type .dropdown", element).html();
      attr.eParam      = $(".type .param", element).html();
    }
    
    return attr
  }
  
  var attr = e.std_mode(element) ? std_attributes() : edit_attributes(create);
  return attr;
}

e.std_mode = function(element){
  return element.attr("_edit") == "";
}

e.status = function(element){
  return element.attr("_status") == "new";
}

e.enter_edit_mode = function(element, attr, create){

  var dropdown = function(sql_type) {

    var dropdown = $("<select>");
  	var types = ["type", "string", "bool", "byte", "i16", "i32", "i64", "double", "date", "ip"];

    var build_dropdown = function(dropdown, types) {
      $.each(types, function(index, type){

    		var option = $("<option>").val(type).text(type);

    		if (type == sql_type){
    		  option.attr("selected", "selected");
    		}

    		$(dropdown).append(option);
    	});

      return dropdown;
    }

    return build_dropdown(dropdown, types);
  }
  

  // FIX element attributes
  $(".navBar .name", element)
    .addClass("edit")
    .html(
      $('<input class="input_name">')
      .val(attr.name)
    );

  $(".details .description", element)
    .addClass("edit")
    .html(
      $("<textarea>")
      .val(attr.description)
    );

  $(".details .sql .dropdown", element)
    .html(
      dropdown(attr.sql_type)
    );
  
  var sql_param = $(".details .sql", element);
  e.param(element, attr.sql_param, sql_param);
  
  
  if (create){
    $('.footer .type .dropdown', element)
      .html(
        dropdown(attr.eType)
      );

    var type_param = $(".footer .type", element);
    e.param(element, attr.sql_param, type_param);
  }

  
  // add event handlers to new elements
  
  $('.navBar input', element).click(function(event){
    event.stopPropagation();
  });
  
  // SHOW actions and details pane
  $(".details", element).show();
  $(".details .actions", element).show();
  $(".navBar .buttons", element).show();
  $(element).attr("_edit", "edit");
  
}

e.return_to_std_mode = function(element, attr){
 

  // FIX element attributes
  $(".name", element)
    .removeClass("edit")
    .html(attr.name || "");
  
  $(".description", element)
    .removeClass("edit")
    .html(attr.description || "");
  
  $(".sql .dropdown", element)
    .html(attr.sql_type || "");
  
  $(".sql .param", element)
    .html(attr.sql_param || "");
  
  $('.type .dropdown', element)
    .html(attr.eType || "");
  
  $(".type .param", element)
    .html(attr.eParam || "");

  // HIDE actions and details pane
  if($(".description", element).html() == ""  && ($(".sql .dropdown", element).text() == "type" || $(".sql .dropdown", element).text() == "")){
    $(".details", element).hide();
  }
  
  $(".details .actions", element).hide();
  $(element).attr("_edit", "");
  
}

e.save_to_json = function(attr, object){

  object.name = attr.name;       
  object.description = attr.description;
  object.sql_type = attr.sql_type;
  object.sql_param = attr.sql_param;
  object.eType = attr.eType;
  object.eParam = attr.eParam;
  object.active = attr.active;
  
  return object;
}

e.param = function(element, param, sql){

  option = $(".dropdown select option:selected", sql).text();

  if(option == "string") {
    $(".param", sql)
      .html(
        $("<input>").val(param)
      );
  } else {
    $(".param", sql)
      .html("");
  }
}

w.panel_heights = function(){
  var height = $(window).height();
  $("#resultsPane").css("height", (height - 40));
  $("#table").css("height", (height - 40));
}

w.build_eventType_table = function() {
  
  sorted_keys = keys(objects).sort();
  
  for (i in sorted_keys) {
    key = sorted_keys[i];
    $('table#eventTypes').append(
      $('<tr>')
        .attr('name', key)
        .append($('<td>').text(key))
    );
  }
}

w.events = function(){
  $('#header .newET a').click(function(){

    eventType = prompt("Event Type:");
    var types = keys(objects);
    var not_repeated = $.grep(types, function(t, i){ return t == eventType }, true)
    
    if( eventType && (not_repeated.length == types.length) ) {
      object = {
        active: "active",
        created_date: new Date(),
        schema: new Array()
      };
      
      objects[eventType] = object;
      
      // rebuilt event type table
      $('#eventTypes tr').remove();
      w.build_eventType_table();
     
      t.events();
      var tr = $('#eventTypes tr.' + eventType)
      r.presentation_stuff(tr);
    }

  });
}

r.presentation_stuff = function(tr){
	r.actions.wipe_rp();
  r.actions.set_rp_title();
	r.actions.showButtons();
	r.actions.highlightSelectedRow(tr);
}

r.create_fields = function(fields){
  $.each(fields, function(index, field_obj){
    e.create_element(field_obj)
	});
}

t.events = function(){
  $("#resultsPane #sButtons").hide();
 
	$('table#eventTypes tbody tr').click(function(){

		  eventType = $(this).attr('name');
		  object = objects[eventType]
		  schema = object.schema;
      
      r.presentation_stuff(this);
      r.create_fields(schema);
      
	});		
  
}

r.events = function(){
 
  $("#resultsPane #title li#show_schema").click(function(){
 
     // build string
     var string = "java_package ning.hadoop.thrift.generated\n\nstruct BasicFlows <br /><br />";
     string += "struct " + eventType + " &nbsp; {<br />";
     $.each(schema, function(index, field) {string += "&nbsp;&nbsp;&nbsp;&nbsp;" + field.position + ":" + field.type + " " + field.name + ",<br />";});
     string += "}";
 
     // build schema div
     var div = $('<div class="element" id="schema">')  
       
       .append($('<div class="navBar active">').text("Schema"))
       .append($('<div class="details active" style="padding:10px;">')
         .html(string)
       );
   
       // append schema to resultsPane
       $(div).appendTo($("#resultsPane"));
       $(this).hide();
 
         
  })
  
  $("#resultsPane #title li#add").click(function(){
    var total_elements = $('#resultsPane .element').length;
    element = e.create_element({position:total_elements, status:"new"});
    attributes = e.get_attributes(element, true);
    schema.push(e.save_to_json(attributes, {}));
  })

  $('#resultsPane #title li#deprecate').toggle(function(){
    
    
    // update DOM
    $('#resultsPane #title, .element .navBar, .element .details, .element .footer').addClass("deprecated").removeClass("active");

    // update JSON
    object.active = "deprecated";
    $.each(schema, function(index, field){
      field.active = "deprecated";
    })
    
  }, function(){
    
    // update DOM
    $('#resultsPane #title, .element .navBar, .element .details, .element .footer').addClass("active").removeClass("deprecated");

    // update JSON
    object.active = "active";
    $.each(schema, function(index, field){
      field.active = "active";
    })
    
    
  });
}


r.actions = {
  
   wipe_rp:function(){
     $("#resultsPane")
   	  .children().remove(".element");     
   },
   
   set_rp_title:function(){
     var rp_title = $("#resultsPane #title");

     if (object.active == "active"){
       rp_title
         .addClass("active")
         .removeClass("deprecated");

     } else {
       rp_title
         .addClass("deprecated")
         .removeClass("active");
     } 

   	 $("#resultsPane #title #name")
   	   .html($("<a>")
   	     .attr("target", "_blank")
   	     .attr("href", ("http://action:8080/rest/1.0/hdfs?dir=/events/ning/" + eventType))
   	     .text(eventType));
     
   },

   showButtons:function(){
     $("#resultsPane #sButtons")
   	  .show();
   },
   
   highlightSelectedRow:function(tr){
     $(tr)
       .addClass("selected")
       .siblings().removeClass("selected");
   }

}

keys = function(obj){
  accumalator = [];
  for (key in obj){
    accumalator.push(key) ;
  }
  return accumalator;
}
