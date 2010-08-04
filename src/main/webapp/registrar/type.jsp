<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
	    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
	    <title>thrift - mock</title>
	    <script type="text/javascript">var json = ${it};</script>
			<script type="text/javascript" src="/goodwill/js/jquery-1.3.2.min.js"></script>
			<script type="text/javascript" src="/goodwill/js/jquery-magic.js"></script>
	    <link rel="stylesheet" href="/goodwill/css/global.css" type="text/css">
	</head>
	<body>

		<div id="header">
			<div class="wrapper">
				<h1>Thrift Query Server</h1>
				<div class="newET"><a>Create an Event Type</a></div>
			</div>
		</div>


		<div id="main">
			<div id="table">
				<table id="eventTypes">
					<thead><tr><th>Event Type</th></tr></thead>
				</table>
			</div>

			<div id="resultsWrapper">
				<div id="resultsPane">
					<div id="title" class="active">
						<ul>
							<li>Schema:</li>
							<li id="name"></li>
							<li id="sButtons">
								<ul>
									<li id="show_schema"></li>
									<li id="add"></li>
									<li id="deprecate"></li>
								</ul>
						  </li>
							<div style="clear:both;"></div>

					</div>
				</div>
				<div style="clear:both;"></div>
			</div>

		</div>



	</body>
</html>