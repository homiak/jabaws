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

package compbio.data.sequence;

import java.io.File;

public class UnknownFileFormatException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = 196629543695636854L;

    File file;

    public UnknownFileFormatException() {
	super();
    }

    public UnknownFileFormatException(File file, Throwable cause) {
	super("File: " + file.getAbsolutePath(), cause);
    }

    public UnknownFileFormatException(String message, Throwable cause) {
	super(message, cause);
    }

    public UnknownFileFormatException(String message) {
	super(message);
    }

    public UnknownFileFormatException(Throwable cause) {
	super(cause);
    }

}
