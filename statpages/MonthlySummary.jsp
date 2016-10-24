<%--
Author: Peter Troshin
Date: May 2011
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="dt" %>

<c:import url="header.jsp" >
	<c:param name="title">JABAWS Usage Statistics</c:param>
</c:import>  

<div style="margin: 20px ">  

<h1>JABAWS Usage Statistics</h1>
<table class="center its" style="width: 600px "> 
<thead>
<tr>
<th rowspan="2">Month</th>
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

<c:forEach items="${stat}" var="monthTotal" varStatus="status">
<c:choose>
	<c:when test="${status.count%2==0}">
		<tr class="even">
	</c:when>
	<c:otherwise>
		<tr class="odd">
	</c:otherwise>
</c:choose>

<td>
<c:choose>
<c:when test="${isAdmin}">
  <a title="Click to view the detailed statistics for the period" href="DisplayStat?datetime=${monthTotal.key.time}"><fmt:formatDate value="${monthTotal.key}" type="date" pattern="MMM yyyy"/></a>
</c:when>
<c:otherwise>
   <fmt:formatDate value="${monthTotal.key}" type="date" pattern="MMM yyyy"/>
</c:otherwise>
</c:choose>

</td>
<td>${monthTotal.value.total}</td>
<td>${monthTotal.value.incomplete}</td>
<td>${monthTotal.value.cancelled}</td>
<td>${monthTotal.value.abandoned}</td>
</tr>
</c:forEach>
<tr style="font-weight: bolder;">
<td>Total:</td>
<td>${total.total}</td>
<td>${total.incomplete}</td>
<td>${total.cancelled}</td>
<td>${total.abandoned}</td>
</tr>
</tbody>
</table>

<div style="width: 600px">
<h3>Help</h3>
<p>
The table contains the number of jobs processed by JABAWS per month, for the whole 
period when the statistics was collected</p> 
For each month the table contains the following information.  
<ul>
	<li>The period of time for which statistics is displayed. For example Jan 2011 means period of time from the first of 
	January to the 31 of January.</li>
	<li>Total - the total number of jobs accepted by JABAWS</li>
	<li>Incomplete - the number of jobs for which the result file was not found or was empty excluding cancelled</li>
	<li>Cancelled - the number of jobs cancelled by the user</li>
	<li>Abandoned - the number of jobs which result(s) were not collected</li>
</ul>
The summary for each column is displayed in the last row of the table.
</div> <!-- Help text enclosing dev end -->
<c:if test="${!isAdmin}">
<br/><p><a href="AnnualStat">Please login as administrator</a> to view the detailed statistics (<a href=man_stats.html#helpUsageStats>help</a>)</p>
</c:if>

</div> <!-- page enclosing div end -->
<jsp:include page="footer.jsp" />