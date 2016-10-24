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

package compbio.engine.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import compbio.util.SysPrefs;

public class RunnerConfigMarshaller<T> {

	private static final Logger log = Logger
			.getLogger(RunnerConfigMarshaller.class);

	private final JAXBContext ctx;

	@SuppressWarnings("all")
	public RunnerConfigMarshaller(Class<?> rootClass) throws JAXBException {
		this(rootClass, null);
	}

	public RunnerConfigMarshaller(Class<?> rootClass, Class<?>... classes)
			throws JAXBException {

		if (classes != null) {
			List<Class<?>> classesList = new ArrayList<Class<?>>(
					Arrays.asList(classes));
			classesList.add(rootClass);
			ctx = JAXBContext.newInstance(classesList.toArray(new Class<?>[0]));
		} else {
			ctx = JAXBContext.newInstance(rootClass);
		}
	}

	public void write(Object xmlRootElement, OutputStream out)
			throws JAXBException, IOException {
		Marshaller marsh = ctx.createMarshaller();
		marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		// disable validation
		marsh.setSchema(null);
		marsh.marshal(xmlRootElement, out);
	}

	public void writeAndValidate(Object xmlRootElement, String schemafile,
			OutputStream out) throws JAXBException, IOException, SAXException {
		Marshaller marsh = ctx.createMarshaller();
		marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marsh.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION,
				schemafile);
		marsh.setSchema(getSchema(schemafile));
		marsh.marshal(xmlRootElement, out);
	}

	void generateSchema(String directoryForSchema, String schemaName)
			throws JAXBException, IOException {

		Marshaller marsh = ctx.createMarshaller();
		marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		ctx.generateSchema(new MySchemaOutputResolver(directoryForSchema,
				schemaName));
	}

	public static Schema getSchema(String schemafile) throws SAXException {
		// define the type of schema - we use W3C:
		String schemaLang = "http://www.w3.org/2001/XMLSchema";
		// get validation driver:
		SchemaFactory factory = SchemaFactory.newInstance(schemaLang);
		// create schema by reading it from an XSD file:
		Schema schema = factory.newSchema(new StreamSource(schemafile));
		return schema;
	}

	/**
	 * 
	 * @return validator instance
	 * @throws SAXException
	 */
	public static Validator getValidator(String schemafile) throws SAXException {
		Schema schema = getSchema(schemafile);
		Validator validator = schema.newValidator();
		return validator;
	}

	public static Validator getValidator(Schema schema) throws SAXException {
		Validator validator = schema.newValidator();
		return validator;
	}

	public static boolean validate(Validator validator, String document)
			throws IOException, SAXException {
		try {
			// at last perform validation:
			validator.validate(new StreamSource(document));
		} catch (SAXException ex) {
			ex.printStackTrace();
			log.warn("SAXException validating xml" + ex.getMessage());
			// we are here if the document is not valid:
			// ... process validation error...
			return false;
		}
		return true;
	}

	public <V> V readAndValidate(InputStream document, Class<V> resultElemType)
			throws JAXBException, IOException, SAXException {

		String schemaFile = Long.toHexString(System.nanoTime());
		generateSchema(SysPrefs.getSystemTmpDir(), schemaFile);

		Unmarshaller um = ctx.createUnmarshaller();
		um.setSchema(getSchema(SysPrefs.getSystemTmpDir() + File.separator
				+ schemaFile));

		JAXBElement<V> rconfig = um.unmarshal(new StreamSource(document),
				resultElemType);
		return rconfig.getValue();
	}

	static class MySchemaOutputResolver extends SchemaOutputResolver {
		final String dir;
		final String sname;

		public MySchemaOutputResolver(String directory, String suggestedFileName) {
			this.dir = directory;
			this.sname = suggestedFileName;
		}

		@Override
		public Result createOutput(String namespaceUri, String suggestedFileName)
				throws IOException {
			return new StreamResult(new File(dir, this.sname));
		}
	}

	@SuppressWarnings("all")
	public <V> V read(InputStream instream, Class<V> resultElemType)
			throws JAXBException {
		return read(instream, resultElemType, null);
	}

	public <V> V read(InputStream instream, Class<V> resultElemType,
			Class<?>... classes) throws JAXBException {
		if (instream == null) {
			throw new NullPointerException("Input stream must be provided!");
		}
		JAXBContext ctx = null;
		if (classes != null) {
			List<Class<?>> classesList = new ArrayList<Class<?>>(
					Arrays.asList(classes));
			classesList.add(resultElemType);
			ctx = JAXBContext.newInstance(classesList.toArray(new Class<?>[0]));
		} else {
			ctx = JAXBContext.newInstance(resultElemType);
		}

		Unmarshaller um = ctx.createUnmarshaller();
		JAXBElement<V> rconfig = um.unmarshal(new StreamSource(instream),
				resultElemType);

		return rconfig.getValue();
	}

}
