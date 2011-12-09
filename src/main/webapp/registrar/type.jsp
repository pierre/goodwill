<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--
  ~ Copyright 2010 Ning, Inc.
  ~
  ~ Ning licenses this file to you under the Apache License, version 2.0
  ~ (the "License"); you may not use this file except in compliance with the
  ~ License.  You may obtain a copy of the License at:
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  ~ WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  ~ License for the specific language governing permissions and limitations
  ~ under the License.
  --%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <title>Goodwill - an Event Types store</title>
    <jsp:useBean id="it"
                 type="com.ning.metrics.goodwill.modules.ThriftRegistrar"
                 scope="request">
    </jsp:useBean>
    <script type="text/javascript">
        var json = <%= it.getStoreInJSON() %>;
        var actionCoreURL = "<%= it.getActionCoreURL() %>";
    </script>
    <script type="text/javascript" src="/static/js/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="/static/js/layout.js"></script>
    <script type="text/javascript" src="/static/js/server.js"></script>
    <script type="text/javascript" src="/static/js/fields.js"></script>
    <script type="text/javascript" src="/static/js/jquery-magic.js"></script>
    <script type="text/javascript" src="/static/js/jquery.json-2.2.js"></script>
    <link rel="stylesheet" href="/static/css/global.css" type="text/css">
</head>
<body>

<div id="header">
    <div class="wrapper">
        <h1>Schemata registrar</h1>

        <div class="newET"><a>Create an Event Type</a></div>
    </div>
</div>


<div id="main">
    <div id="table">
        <div id="ajax_message"></div>

        <table id="eventTypes">
            <thead>
            <tr>
                <th>Event Types</th>
            </tr>
            </thead>
        </table>
    </div>

    <div id="resultsWrapper">
        <div id="resultsPane">
            <div id="schema-information">
            </div>
            <div id="schema">
            </div>
        </div>
        <div style="clear:both;"></div>
    </div>
</div>
</body>
</html>
