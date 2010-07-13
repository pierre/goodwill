


$(document).ready(function(){

	// Variables
  var height = $(window).height();
	var objects = {};
	
	// build objects hash
	$.each(json, function(i, val) {
	  objects[val.name] = val.schema; 
	});
			
  // Set the height of the left and right columns
  $("#resultsPane").css("height", (height - 40));
  $("#table").css("height", (height - 40));

	// Build the EventType Table
  $.each(objects, function(eName, schema){
    $('table#eventTypes').append(
      $("<tr>").append(
        $("<td>").text(eName).attr('name', eName))
    );					
  });

	// hover function
  // $('table#eventTypes tbody tr').hover(function(){
  //  $(this).addClass("highlight");
  // }, function(){
  //  $(this).removeClass("highlight");
  // });
	
	
	// click function
	$('table#eventTypes tbody tr').click(function(){
		  
		  
		  // get and set the current resultsPane
			eName   = $('td', this).attr('name');
			schema  = objects[eName];
			pane    = $("#resultsPane");
			
			// clear the current results pane
			pane.children().remove(".element");
			
			// add and remove selected row class
			$(this).parent().children().removeClass("selected");
			$(this).addClass("selected");
			
			// add the eventType to the schema title
			$("#resultsPane #title #name").html("&nbsp;" + eName)
			
			// create each element
			$.each(schema, function(index, element){
			  
        // append element to resultsPane
			  elementDiv(element).appendTo(pane);
			
        // hide parts of the element
        $(".navBar .buttons").hide();
        $(".actions").hide();

        // hide details
        if( $(".description").html() == ""  && $(".sql .dropdown").html() == "" ) {
          $(".details").hide();
        }        
			});
			
	   	
	   // ELEMENT hover
     $("#resultsPane .element").hover(function(){
       $(".navBar .buttons", this).show();
     }, function(){
       $(".navBar .buttons", this).hide();
     });
     
     
     // Element REMOVE button jazz
     $("#resultsPane .remove").click(function(){
       if(confirm("Are you sure you want to depricate this type?")){
         $(this).parent().parent().parent().slideUp(200, function() {$(this).remove();})
       }
     })
     
     // Element BLUE button jazz
     $("#resultsPane .blue").click(function(){
       
       // get values
       var element = $(this).parent().parent().parent();
       var name = $(".name", element).html();
       var description = $(".description", element).html();
       var sql_type = $(".sql .dropdown").html();
       var sql_param = $(".sql .param").html();
       var edit_mode = $(element).attr("_edit") || "";
       
       // show edit DOM stuff
       if(edit_mode == "") {
         $(".details", element).show();
         $(".name", element).addClass("edit").html($("<input>").val(name));
         $(".description", element).addClass("edit").html($("<textarea>").val(description));
         $(".sql .dropdown").html(dropdown(sql_type));
         
         
         // $(".sql .param").html(parameter(sql_type, sql_param));
         
         
         $(".actions", element).show();
         $(element).attr("_edit", "edit");         
       }

     });
     
     // Element SAVE button jazz
     $("#resultsPane .element .actions .save").click(function(){

       // get values 
       var element = $(this).parent().parent().parent();
       var name = $(".name input", element).val();
       var description = $(".description textarea", element).val();
       
       // save values back to objects
       var index  = $("#resultsPane .element").index(element);
       var object = objects[eName][index]
       object["name"] = name;       
       object["eDescription"] = description;
       
       // remove stupid edit things
       $(".name", element).removeClass("edit").html(name);
       $(".description", element).removeClass("edit").html(description);
       $(".actions", element).hide();
       $(element).attr("_edit", "");         

       // hide details if there is nothing to show
       if($(".description").html() == ""  && $(".sql .dropdown").html() == ""){
         $(".details").hide();
       } 
     });
     
     
     // Element CANCEL button jazz
     $("#resultsPane .element .actions .cancel").click(function(){
      
       // retrieve element's original values
       var element = $(this).parent().parent().parent();
       var index  = $("#resultsPane .element").index(element);
       var object = objects[eName][index]

       // insert original values back into the DOM
       var name = object["name"];
       var description = object["eDescription"];


       // insert original values back into the DOM
       $(".name", element).html(name || "");
       $(".description", element).html(description || "")

       
       // remove stupid edit things       
       $(".name", element).removeClass("edit");
       $(".description", element).removeClass("edit");
       $(".actions", element).hide();     
       $(element).attr("_edit", "");         
       
       // hide details if there is nothing to show
       if($(".description").html() == ""  && $(".sql .dropdown").html() == ""){
         $(".details").hide();
       } 
     });
     
     
	});		
});



// RESIZE Windows

$(window).resize(function() {
  var height = $(window).height();	
	$("#resultsPane").css("height", (height - 40));
	$("#table").css("height", (height - 40));
});


function elementDiv(element) {
  return $("<div>").addClass("element").attr("_edit", "")
  
     .append($("<div>").addClass("navBar")
       .append($("<div>").addClass("name").text(element.name || ""))
       .append($("<div>").addClass("buttons")
         .append($("<div>").addClass("blue"))
         .append($("<div>").addClass("remove"))
        )
       .append($("<div style=\"clear:both\"></div>"))
     )
  
     .append($("<div>").addClass("details")
	     .append($("<div>").addClass("description").html(element.eDescription || ""))
       .append($("<div>").addClass("sql")
         .append($("<ul>").addClass("list")
            .append($("<li>").html("sql: "))
            .append($("<li>").addClass("dropdown").html(element.sql_type || "sdf"))
            .append($("<li>").addClass("param").html(element.sql_param || "sf"))
         )
       )
	     .append($("<div><ul>").addClass("actions")
	       .append($("<li>").addClass("save").text("save"))
	       .append($("<li>").addClass("cancel").text("cancel"))
	     )
     )
     
     .append($("<div>").addClass("footer")
       .append($("<div>").addClass("type").text("type: " + (element.type || "")))
       .append($("<div>").addClass("position").text("position: " + (element.position || "")))
     )
     
  .append($("<div style=\"clear:both\"></div>"));
}

function dropdown(sql_type) {
  
  var dropdown = $("<select>");
	var types = ["type", "string", "bool", "byte", "i16", "i32", "i64", "double", "date", "ip"];
  
  
	$.each(types, function(index, type){
		
		var option = $("<option>").val(type).text(type);
		if (type == sql_type)
		  option.attr("selected", "selected");
		$(dropdown).append(option);
		
	});
	
  return dropdown;
}

// function parameter (sql_type, sql_param) {
//   if (sql_type == "string") {
//     var param = $("<input>").val()
//   }
// }

