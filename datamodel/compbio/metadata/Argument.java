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

import java.net.URL;
import java.util.List;

/**
 * An unmodifiable view for the options and parameters, with one exception - it
 * allows to set a value
 * 
 * @see Parameter
 * @see Option
 * 
 * @author pvtroshin
 * 
 *         Date December 2009
 * @param <T>
 *            executable type
 */
public interface Argument<T> {

    URL getFurtherDetails();

    String getDefaultValue();

    String getDescription();

    String getName();

    /**
     * 
     * @return List of values allowed for an Argument
     */
    List<String> getPossibleValues();

    /**
     * Set default values for the parameter or an option
     * 
     * @param defaultValue
     *            the value to be set
     * @throws WrongParameterException
     *             - when the value to be set is illegal. Wrong value for
     *             numeric parameter is the value defined outside it , for
     *             string type parameter, wrong value is the one which is not
     *             listed in possible values list
     * @see ValueConstrain
     */
    void setValue(String defaultValue) throws WrongParameterException;

}
