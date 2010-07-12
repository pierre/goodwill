function dropdown () {
    
    var dropdown = $("<select>");
		var types = ["type", "string", "bool", "byte", "i16", "i32", "i64", "double", "date", "ip"];

		$.each(types, function(index, type){
			$(dropdown).append($("<option>").val(type).text(type));
		});
		
		return dropdown;  
}



$(document).ready(function(){

	// Variables
  var height = $(window).height();
	var objects = new Array();				
	
	
	
  // Set the height of the left and right columns
  $("#resultsPane").css("height", (height - 40));
  $("#table").css("height", (height - 40));



  //////////////////////////
  // TABLE MAGIC ///////////
  //////////////////////////


	
	// Build the EventType Table
  $.each(json, function(index, eventType){

    objects[eventType.name] = eventType.schema;
    
    $('table#eventTypes').append(
      $("<tr>").append(
        $("<td>").text(eventType.name).attr('name', eventType.name))
    );					
  });

	
	// hover function
	$('table#eventTypes tbody tr').hover(function(){
		$(this).addClass("highlight");
	}, function(){
		$(this).removeClass("highlight");
	});
	
	
	// click function
	$('table#eventTypes tbody tr').click(function(){
		
			name    = $('td', this).attr('name');
			schema  = objects[name];
			pane    = $("#resultsPane");
			
			
			$(this).parent().children().removeClass("selected");
			$(this).addClass("selected");
			pane.children().remove(".element");
			
			$("#resultsPane #title .EventType").html("&nbsp;" + name)
			
			$.each(schema, function(index, element){

				var div = 
				  $("<div>").addClass("element")
				  
				     .append($("<div>").addClass("navBar")
				       .append($("<div>").addClass("name").text(element.name || ""))
				       .append($("<div>").addClass("buttons")
				         .append($("<div>").addClass("blue"))
				         .append($("<div>").addClass("remove"))
				        )
				       .append($("<div style=\"clear:both\"></div>"))
				     )
				  
				     
				     .append($("<div>").addClass("details")
  				     .append($("<div>").addClass("description"))
             )
             
             .append($("<div>").addClass("footer")
               .append($("<div>").addClass("type").text("type: " + (element.type || "")))
               .append($("<div>").addClass("position").text("position: " + (element.position || "")))
             )
             
          .append($("<div style=\"clear:both\"></div>"))   
          ;
				     
				     				       
				div.appendTo(pane);
        $(".buttons").hide();
			});
			

      // var buttons = $("<div>").addClass("buttons")
      //             .append($("<div>").addClass("cancel").text("cancel"))
      //             .append($("<div>").addClass("save").text("save"));
      //     
     // buttons.appendTo(pane);
     // $("<div style=\"clear:both\"></div>").appendTo(pane);
	   
	   
     $("#resultsPane .element").hover(function(){
       $(".buttons", this).show();
     }, function(){
       $(".buttons", this).hide();
     });

     $("#resultsPane .remove").click(function(){
       $(this).parent().parent().parent().slideUp(200, function() {$(this).remove();})
     })
     


     // BLUE toggle button
     $("#resultsPane .blue").toggle(function(){

       wrapper = $(this).parent().parent();
       $(".sql", wrapper).children().show();        
       $(".sql", wrapper).addClass("selected");
     }, function(){

       wrapper = $(this).parent().parent();
       $(".sql", wrapper).children().hide();
       $(".sql", wrapper).removeClass("selected");

     });
     
     
     // SQL function

     $(".sql select").change(function(){
       var selected = $("option:selected", this).html();

       if (selected == "string") {
         $(this).parent()
           .append($("<div>").addClass("i parameters")
             .append($("<label>").text("length"))
              .append($("<input>")));


       } else if (selected == "double"){

       } else {
         $(".parameters", $(this).parent()).remove();
         // $(".parameters").remove();

       }
     })


	});		
});


// FIXED effects

$(document).ready($(function () {
  
  var msie6 = $.browser == 'msie' && $.browser.version < 7;
      
      if (!msie6) {

        $(window).scroll(function (event) {
          var y = $(this).scrollTop();
          
          if (y >= 34) {
            $('#resultsWrapper').addClass('fixed');
            // $('#table').addClass('fixed');
          } else {
            $('#resultsWrapper').removeClass('fixed');
            // $('#table').addClass('fixed');            
          }
        });
      }  
}));


// RESIZE Windows

$(window).resize(function() {

  var height = $(window).height();	
	$("#resultsPane").css("height", (height - 40));
	$("#table").css("height", (height - 40));
  // alert($(window).height());
  // $('body').prepend('<div>' + $(window).width() + '</div>');
});