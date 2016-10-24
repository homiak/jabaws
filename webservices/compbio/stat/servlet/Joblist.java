/* Copyright (c) 2011 Peter Troshin
 *  
 *  JAva Bioinformatics Analysis Web Services (JABAWS) @version: 2.0     
 * 
 *  This library is free software; you can redistribute it and/or modify it under the terms of the
 *  Apache License version 2 as published by the Apache Software Foundation
 * 
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Apache 
 *  License for more details.
 * 
 *  A copy of the license is in apache_license.txt. It is also available here:
 * @see: http://www.apache.org/licenses/LICENSE-2.0.txt
 * 
 * Any republication or derived work distributed in source code form
 * must include this copyright and license notice.
 */
package compbio.stat.servlet;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import compbio.engine.client.PathValidator;
import compbio.stat.collector.StatDB;
import compbio.stat.collector.StatProcessor;
import compbio.util.Util;
import compbio.ws.client.Services;

public class Joblist extends HttpServlet {

	static final String JT_FAILED = "failed";
	static final String JT_ABANDONED = "abandoned";
	static final String JT_CANCELLED = "cancelled";
	static final String JT_ALL = "all";
	static final String JT_INCOMPLETE = "incomplete";
	/**
	 * Input:
	 * 
	 * ws=${ws.key}
	 * 
	 * where=everywhere cluster local
	 * 
	 * type=cancelled all incomplete
	 * 
	 * from=${startDate}
	 * 
	 * to=${stopDate}
	 * 
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		/*
		 * Values for this servlet are not user supplied, so do not bother with
		 * nice error messages just throw the exception is something is wrong!
		 */
		String wsname = req.getParameter("ws");
		Services wservice = Services.getService(wsname);
		if (wservice == null) {
			throw new ServletException(
					"Webservice name 'ws' is not specified or is incorrect. Given value:"
							+ wsname);
		}
		String executor = req.getParameter("where");
		if (Util.isEmpty(executor)) {
			throw new ServletException("'Where' is not specified!");
		}
		if (!(executor.equalsIgnoreCase("everywhere")
				|| executor.equalsIgnoreCase("local") || executor
				.equalsIgnoreCase("cluster"))) {
			throw new ServletException("Invalid 'where' value '" + executor
					+ "' can be one of 'everywhere', 'local', 'cluster'!");
		}
		Boolean where = null;
		if (executor.equalsIgnoreCase("local")) {
			where = false;
		} else if (executor.equalsIgnoreCase("cluster")) {
			where = true;
		}

		String jobtype = req.getParameter("type");
		if (Util.isEmpty(executor)) {
			throw new ServletException("'type' is not specified!");
		}
		if (!(jobtype.equalsIgnoreCase(JT_CANCELLED)
				|| jobtype.equalsIgnoreCase(JT_ALL)
				|| jobtype.equalsIgnoreCase(JT_INCOMPLETE)
				|| jobtype.equalsIgnoreCase(JT_ABANDONED) || jobtype
				.equalsIgnoreCase(JT_FAILED))) {
			throw new ServletException("Invalid 'jobtype' value '" + jobtype
					+ "' can be one of 'cancelled', 'all', 'incomplete', "
					+ "'failed', 'abandoned'!");
		}
		String fromDate = req.getParameter("from");
		if (Util.isEmpty(fromDate)) {
			throw new ServletException("'fromDate' is not specified!");
		}
		String toDate = req.getParameter("to");
		if (Util.isEmpty(toDate)) {
			throw new ServletException("'toDate' is not specified!");
		}

		// Just extract the last name from the path
		String clusterTempDir = StatisticCollector.getClusterJobDir();
		if (!Util.isEmpty(clusterTempDir)) {
			clusterTempDir = new File(clusterTempDir).getName();
		} else {
			clusterTempDir = "";
		}
		// Just extract the last name from the path
		String localTempDir = StatisticCollector.getLocalJobDir();
		if (!Util.isEmpty(localTempDir)) {
			if (PathValidator.isAbsolutePath(localTempDir)) {
				localTempDir = new File(localTempDir).getName();
			}
		} else {
			localTempDir = "";
		}

		Timestamp startDate = new Timestamp(Long.parseLong(fromDate));
		Timestamp stopDate = new Timestamp(Long.parseLong(toDate));
		StatDB statdb = null;
		try {
			statdb = new StatDB();
			StatProcessor stat = new StatProcessor(statdb.readData(startDate,
					stopDate, wservice, where));

			HttpSession session = req.getSession();
			if (jobtype.equalsIgnoreCase(JT_CANCELLED)) {
				session.setAttribute("stat",
						new StatProcessor(stat.getCancelledJobs()));
			} else if (jobtype.equalsIgnoreCase(JT_INCOMPLETE)) {
				session.setAttribute("stat",
						new StatProcessor(stat.getIncompleteJobs()));
			} else if (jobtype.equalsIgnoreCase(JT_ALL)) {
				session.setAttribute("stat", stat);
			} else if (jobtype.equalsIgnoreCase(JT_FAILED)) {
				session.setAttribute("stat",
						new StatProcessor(stat.getFailedJobs()));
			} else if (jobtype.equalsIgnoreCase(JT_ABANDONED)) {
				session.setAttribute("stat",
						new StatProcessor(stat.getAbandonedJobs()));
			} else {
				throw new AssertionError("Unrecognised job type: " + jobtype);
			}
			session.setAttribute("clusterTemp", clusterTempDir);
			session.setAttribute("localTemp", localTempDir);
			req.setAttribute("startDate", startDate.getTime());
			req.setAttribute("stopDate", stopDate.getTime());

			RequestDispatcher dispatcher = req
					.getRequestDispatcher("statpages/Joblist.jsp");
			dispatcher.forward(req, resp);

		} catch (SQLException e) {
			e.printStackTrace();
			throw new ServletException("SQLException : "
					+ e.getLocalizedMessage(), e);
		}

	}
}
