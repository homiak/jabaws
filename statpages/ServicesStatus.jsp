<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%--
Author: Peter Troshin
Date: May 2011
This is a JSP fragment to be inserted into document, cannot be used alone
TODO refactor
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="dt" %>

<c:import url="header.jsp" >
	<c:param name="title">JABAWS Services Status</c:param>
</c:import>  
<div style="margin: 20px ">
<h2 style="text-align: center;">JABAWS Services Status</h2>
<h2 style="text-align: center;">Server: <span style="color: green">${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}</span> </h2>
<br/>
<p>All the services were tested while this page was loading. If you want to test the services again, just reload this page. Click on the service status to see the results of the testing.</p>
<table class="its" >
<thead>
<tr>
<th title="The name of the service" width="100px">Service</th>
<th title="Service status">Status</th>
</tr>
</thead>
<c:forEach items="${results}" var="res" varStatus="status">
  <c:choose>
	<c:when test="${status.count%2==0}">
		<tr class="even">
	</c:when>
	<c:otherwise>
		<tr class="odd">
	</c:otherwise>
</c:choose>
  <td width="100px">${res.service}</td>
  
  <td>
  <c:choose>
	<c:when test="${res.status}">
		
		<div class="source">
		<div class="header collapsed" onclick=
		"$(this).toggleClassName('collapsed'); $(this).next('.body').toggleClassName('collapsed');"
 		title="Click to open/close"><span style="color: green">OK</span></div>
		<div class="body collapsed">
		<pre>${res.details}</pre>
		</div>
		</div>
	</c:when>
	<c:otherwise>
	<div class="source">
		<div class="header collapsed" onclick=
		"$(this).toggleClassName('collapsed'); $(this).next('.body').toggleClassName('collapsed');"
 		title="Click to open/close"><span style="color: red">Fail</span></div>
		<div class="body collapsed">
		<pre>${res.details}</pre>
		</div>
	</div>

	</c:otherwise>
	</c:choose>
  </td>
  
</tr>

</c:forEach>
</table>
<p>If you would like to integrate JABAWS with automated health check system you may want to use 
the HTTP code response service checker. It responds with HTTP status code depending on the status 
of the web service.
For more information please refer to <a href="man_serverwar.html#usingWsTester">Testing JABAWS server</a> help page.</p>
</div><!-- margin div -->	
<jsp:include page="footer.jsp" />