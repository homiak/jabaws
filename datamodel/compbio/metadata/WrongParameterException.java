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

/**
 * WrongParameterException is thrown wherever the {@link RunnerConfig} object
 * does not match the actual runnable or then attempting to set the value of
 * {@link Argument} to invalid value.
 * 
 * 
 * @author pvtroshin
 * 
 * @version 1.0 October 2009
 */
public class WrongParameterException extends Exception {

	/**
	 * Default stable serial for serialization
	 */
	private static final long serialVersionUID = -547775417557345769L;

	public WrongParameterException(Option<?> option) {
		this("Wrong option is: " + option.toString());
	}

	public WrongParameterException(String message) {
		super(message);
	}

	public WrongParameterException(Throwable cause) {
		super(cause);
	}

	public WrongParameterException(String message, Throwable cause) {
		super(message, cause);
	}

}
