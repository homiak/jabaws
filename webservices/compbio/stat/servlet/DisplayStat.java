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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import compbio.stat.servlet.util.StatCollection;
import compbio.stat.servlet.util.Totals;

public class DisplayStat extends HttpServlet {

	private final static Logger log = Logger.getLogger(DisplayStat.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String datetime = req.getParameter("datetime");

		Date fromDate = new Date(Long.parseLong(datetime));
		Calendar toCal = GregorianCalendar.getInstance();
		toCal.setTime(fromDate);
		toCal.add(Calendar.MONTH, 1);

		try {
			StatCollection stats = StatCollection.newStatCollecton(fromDate,
					toCal.getTime());

			log.trace("Stats: " + stats);
			req.setAttribute("stat", stats);
			req.setAttribute("statTotal", Totals.sumStats(stats.getAllStat()));
			req.setAttribute("statTotalCluster",
					Totals.sumStats(stats.getClusterStat()));
			req.setAttribute("statTotalLocal",
					Totals.sumStats(stats.getLocalStat()));

			req.setAttribute("startDate", fromDate);
			req.setAttribute("stopDate", toCal.getTime());
			log.trace("from " + fromDate + "  to " + toCal.getTime());
			RequestDispatcher dispatcher = req
					.getRequestDispatcher("statpages/Statistics.jsp");
			dispatcher.forward(req, resp);

		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new ServletException(e);
		}

	}
}
