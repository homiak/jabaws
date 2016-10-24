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

<h2>JABAWS Usage Statistics for the Period: <fmt:formatDate value="${startDate}" /> to <fmt:formatDate value="${stopDate}"/></h2>
<h2>All Jobs</h2>
<c:set var="where"  value="everywhere" scope="request"/>
<c:set var="statistics"  value="${stat.allStat}" scope="request"/>
<c:set var="totals" value="${statTotal}" scope="request"/>
<c:import url="StatisticsTable.jsp"/> 

<h2>Local Jobs</h2>
<c:set var="where"  value="local" scope="request"/>
<c:set var="statistics"  value="${stat.localStat}" scope="request"/>
<c:set var="totals" value="${statTotalLocal}" scope="request"/>
<c:import url="StatisticsTable.jsp"/>
	
<h2>Cluster Jobs</h2>
<c:set var="where"  value="cluster" scope="request"/>
<c:set var="statistics"  value="${stat.clusterStat}" scope="request"/>
<c:set var="totals" value="${statTotalCluster}" scope="request"/>
<c:import url="StatisticsTable.jsp"/>

<div style="width: 600px">
<h3>Help</h3>
<p>
Each table contains the number of jobs processed by JABAWS during the period of 
time specified in the title.</p> 
<ul>
<li>The "All Jobs" table contains the summary of all jobs.</li> 
<li>"Local Jobs" table - contains the summary of the jobs calculated by the local engine.</li> 
<li>"Cluster Jobs" table - contains the summary of the jobs calculated by the cluster.</li>
</ul>
Each table contains the following information for each web service  
<ul>
	<li>Total - the total number of jobs accepted by a particular JABA service</li>
	<li>Incomplete - the number of jobs for which the result file was not found or was empty excluding cancelled</li>
	<li>Cancelled - the number of jobs cancelled by the user</li>
	<li>Abandoned - the number of jobs which result(s) were not collected</li>
</ul>
</div> <!-- Help text enclosing dev end -->
</div> <!-- page enclosing div ends -->

<jsp:include page="footer.jsp" />
