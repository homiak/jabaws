/* Copyright (c) 2009 Peter Troshin
 *  
 *  JAva Bioinformatics Analysis Web Services (JABAWS) @version: 1.0
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

package compbio.metadata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Exception for generic problems with JobSubmission it is often thrown as a
 * wrapper for the lower level exceptions like IOException or DrmaaException. If
 * this exception is thrown the task has not been calculated
 * 
 * @author pvtroshin
 * 
 *         Date December 2009
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JobSubmissionException extends Exception {

    private static final long serialVersionUID = 607986894357895572L;

    public JobSubmissionException(String message) {
	super(message);
    }

    public JobSubmissionException(Throwable cause) {
	super(cause);
    }

    public JobSubmissionException(String message, Throwable cause) {
	super(message, cause);
    }

}
