<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
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

<dt:table class="its" id="job" name="${stat.jobs}" export="true" sort="list"  pagesize="100" 
defaultsort="0" defaultorder="descending">
	<dt:caption>JABAWS Jobs List (<a href="#dcolm">detailed column description</a>) </dt:caption>	
	<dt:column title="JobID" sortable="true">
		<c:choose>
		<c:when test="${fn:startsWith(job.jobname,'@')}">
			<a href="${pageContext.request.contextPath}/${clusterTemp}/${job.escJobname}">${job.jobname}</a>
		</c:when>
		<c:otherwise>
			<a href="${pageContext.request.contextPath}/${localTemp}/${job.escJobname}">${job.jobname}</a>
		</c:otherwise>
		</c:choose>
	</dt:column>
	<dt:column property="clusterJobId" title="Cluster JobID" sortable="false"></dt:column>
	<dt:column property="inputSize" title="Input Size (b)" sortable="true"></dt:column>
	<dt:column property="resultSize" title="Result Size (b)" sortable="true"></dt:column>
	<dt:column title="Runtime (s)" sortable="true">
		<c:choose>
			<c:when test="${job.runtime==-1}">
				?
			</c:when>
			<c:when test="${job.runtime==0}">
				&lt;1
			</c:when>
			<c:otherwise>
				${job.runtime}
			</c:otherwise>
		</c:choose>
	</dt:column>
	<dt:column property="start" title="Start time" sortable="true"></dt:column>
	<dt:column property="finish" title="Finish time" sortable="true"></dt:column>	
	
	<dt:column title="isCancelled" sortable="true">
		${job.isCancelled}
	</dt:column>	
	<dt:column title="isCollected" sortable="true">
		${job.isCollected} 
	</dt:column>
	<dt:column property="isFinished" title="isFinished?" sortable="true"></dt:column>	

	
	<dt:setProperty name="export.pdf" value="true"/>
	<dt:setProperty name="paging.banner.group_size" value="15" />
	<dt:setProperty name="export.decorated" value="false" />
	<dt:setProperty name="export.pdf.include_header" value="true" />
	<dt:setProperty name="export.excel.include_header" value="true" />
	<dt:setProperty name="export.csv.include_header" value="true" />
	<dt:setProperty name="export.amount" value="list" /> <!-- set list if want to export all -->
	<dt:setProperty name="paging.banner.item_name" value="Job" />
    <dt:setProperty name="paging.banner.items_name" value="Jobs" />
 	<dt:setProperty name="sort.amount" value="list" /> <!-- set page if want to sort only first page before viewing -->

</dt:table>

<c:if test="${!empty stat.jobs}">

<a name="dcolm"></a>Columns
<ul>
	<li>JobID - the JABAWS job id, unique for every job</li>
	<li>Cluster JobID - cluster job id</li>
	<li>InputSize - input size in bytes</li>
	<li>ResultSize - result size in bytes</li>
	<li>Runtime (s) - job's runtime in seconds</li>
	<li>Start time (s)- job's start time and date</li>
	<li>Finish time (s)- job's finish time and date</li>
	<li>isCancelled - whether the job was cancelled</li>
	<li>isCollected - whether the job was collected. False for the jobs that has been initiated but which results has never been retrieved</li>
	<li>isFinished - whether the job has finished. This does not necessarily mean that the job has produced the result. 
	The job can sometime finish in failure.</li>
</ul>
</c:if>
	
<jsp:include page="footer.jsp" />
