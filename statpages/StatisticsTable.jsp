<%--
Author: Peter Troshin
Date: May 2011
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="dt" %>

    
<table class="its" style="width:600px "> 
<thead>
<tr>
<th rowspan="2">Web Service</th>
<th colspan="4" style="text-align: center">Number of Jobs</th>
</tr>
<tr>
<th title="Total number of jobs received">Total</th>
<th title="The number of jobs with no result">Incomplete</th>
<th title="The number of cancelled jobs">Cancelled</th>
<th title="The number of jobs started but not collected by the user">Abandoned</th>
</tr>
</thead>
<tbody>

 
<c:forEach items="${statistics}" var="ws" varStatus="status">
<c:choose>
	<c:when test="${status.count%2==0}">
		<tr class="even">
	</c:when>
	<c:otherwise>
		<tr class="odd">
	</c:otherwise>
</c:choose>

<td>${ws.key}</td>
<td><a title="Click to view jobs" href="Joblist?ws=${ws.key}&where=${where}&type=all&from=${startDate.time}&to=${stopDate.time}">${ws.value.jobNumber}</a></td>
<td><a title="Click to view jobs" href="Joblist?ws=${ws.key}&where=${where}&type=incomplete&from=${startDate.time}&to=${stopDate.time}">${fn:length(ws.value.incompleteJobs)}</a></td>
<td><a title="Click to view jobs" href="Joblist?ws=${ws.key}&where=${where}&type=cancelled&from=${startDate.time}&to=${stopDate.time}">${fn:length(ws.value.cancelledJobs)}</a></td>
<td><a title="Click to view jobs" href="Joblist?ws=${ws.key}&where=${where}&type=abandoned&from=${startDate.time}&to=${stopDate.time}">${fn:length(ws.value.abandonedJobs)}</a></td>
</tr>
</c:forEach>

<tr style="font-weight: bolder;">
<td>Total:</td>
<td>${totals.total}</td>
<td>${totals.incomplete}</td>
<td>${totals.cancelled}</td>
<td>${totals.abandoned}</td>
</tr>
</tbody>
</table>
<c:remove var="statistics"/>
<c:remove var="totals"/>