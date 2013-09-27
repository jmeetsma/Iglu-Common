/*
 * Copyright 2011-2013 Jeroen Meetsma - IJsberg
 *
 * This file is part of Iglu.
 *
 * Iglu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Iglu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ijsberg.iglu.util.xml;


import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.util.io.StreamSupport;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * A Document contains a well-formed XML document or is empty
 * The Application Server has its own XML library / Document for the following reasons:
 * <ul>
 * <li>to be independent of 3rd party XML libraries</li>
 * <li>to be able to deal with inconsequent use of XML</li>
 * <li>to avoid unwanted behavior such as dtd-retrieval over the network</li>
 * <li>to format XML in an nice way as text</li>
 * </ul>
 */
public class Document extends ElementList {
	//storage of XML Document header stuff
	private String encoding = "utf-8";
	private String name;

	private File file;

/*	private String doctype;
	private String xmlVersion;
	private String encoding;
	private Collection docTypeAttributes;
	private String dtd;*/

	/**
	 *
	 */
	public Document() {
	}

	/**
	 * @param name name of the top node
	 */
	public Document(String name) {
		this.name = name;
	}

	public Document(File file) throws IOException, ParseException {
		this.file = file;
		load(file);
	}

	public File getFile() {
		return file;
	}


	public String getEncoding() {
		return encoding;
	}


	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}


	/**
	 * Turns a node into a document
	 *
	 * @param n
	 */
	public Document(Node n) {
		super();
		this.name = n.getName();
//		this.nodeAttributes = new GenericPropertyBundle(n.nodeAttributes);
		this.addNode(n);
	}

	/**
	 *
	 */
	public Document(boolean interpreteAsXHTML) {
		this.interpreteAsXHTML = interpreteAsXHTML;
	}


	public Document load(String input) throws ParseException {
		BufferedReader reader = new BufferedReader(new StringReader(input));
		try {
			load(reader);
			parse(input);
			reader.close();
		} catch (IOException e) {
			throw new ConfigurationException("String reading is not supposed to fail", e);
		}
		return this;
	}


	public void load(File file) throws IOException, ParseException {
		FileInputStream stream = new FileInputStream(file);

		FileReader fileReader = new FileReader(file);
		encoding = fileReader.getEncoding();

		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		load(reader);

		stream.close();
		stream = new FileInputStream(file);

		parse(stream, encoding);

		reader.close();
		stream.close();
	}


	private void load(BufferedReader reader) throws IOException, ParseException {
		String xmlDef = "";
		int lineNr = 1;
		LOOP:
		while (reader.ready()) {
			xmlDef += reader.readLine();
			lineNr++;
			int startPos = xmlDef.indexOf('<');
			if (startPos != -1) {
				if (xmlDef.indexOf("<?xml") != startPos) {
					//no usable xml def found
					break LOOP;
				}
				int endPos = xmlDef.indexOf("?>");
				if (endPos != -1) {
					Tag xmlDefTag = new Tag(lineNr, xmlDef.substring(startPos, endPos + 2));
					encoding = xmlDefTag.getAttributes().getProperty("encoding");
					break LOOP;
				}

			}
		}
	}

	public void save(File file) throws IOException {
		save(file, STRETCH, 50);
	}


	public void save(File file, byte formattingStyle) throws IOException {
		save(file, formattingStyle, 50);
	}

	public void save(File file, byte formattingStyle, int minimumLineLength) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		if (encoding != null) {
			out.write(toString(formattingStyle, minimumLineLength).getBytes(encoding));
		} else {
			out.write(toString(formattingStyle, minimumLineLength).getBytes());
		}
		out.close();
	}

	/**
	 * @param name name of the top node
	 */
	public Document(String name, boolean interpreteAsXHTML) {
		this.name = name;
		this.interpreteAsXHTML = interpreteAsXHTML;
	}

	/**
	 * Turms a node into a document.
	 *
	 * @param n
	 */
	public Document(Node n, boolean interpreteAsXHTML) {
		super();
		this.interpreteAsXHTML = interpreteAsXHTML;
		this.name = n.getName();
//		this.nodeAttributes = new GenericPropertyBundle(n.nodeAttributes);
//		this.setContents(n.getContentsList());
		this.addNode(n);
	}


	/**
	 * Loads the contents from a (byte) input stream.
	 * Encoding is ignored.
	 *
	 * @param in
	 * @throws IOException    if reading the input stream fails
	 * @throws ParseException if the XML document can not be parsed
	 */
	public void parse(InputStream in) throws IOException, ParseException {
		String input = new String(StreamSupport.absorbInputStream(in));
		parse(input);
	}

	/**
	 * Loads the contents from a (byte) input stream
	 *
	 * @param in
	 * @param encoding encoding to use when read the input stream
	 * @throws IOException    if reading the input stream fails
	 * @throws ParseException if the XML document can not be parsed
	 */
	public void parse(InputStream in, String encoding) throws IOException, ParseException {
//		String input = StringSupport.absorbInputStream(in, encoding);
		String input = new String(StreamSupport.absorbInputStream(in));

		parse(input);
	}

	// FIXME </endtag > does not parse because of the space


	public void parse(String xmlInput) throws ParseException {
		parse(xmlInput, false);
	}

	/**
	 * Loads the contents from an XML text
	 * A sanity check is performed, which is not too strict:
	 * A valid document must contain one node exactly
	 * and may furthermore contain instruction- and comment-tags.
	 *
	 * @param xmlInput
	 * @throws ParseException if the XML document can not be parsed
	 */
	public void parse(String xmlInput, boolean strict) throws ParseException {

		if (xmlInput == null) {
			throw new IllegalArgumentException("input can not be null");
		}
		ArrayList splitContents = split(xmlInput, interpreteAsXHTML, true);
		build(splitContents, interpreteAsXHTML);


		int nodeCount = 0;
		ArrayList contentsToRemove = new ArrayList();
		Iterator i = contents.iterator();
		while (i.hasNext()) {
			Object element = i.next();
			if (element instanceof Node) {
				nodeCount++;
			} else if (element instanceof Tag) {
				Tag tag = (Tag) element;
				if (!(tag.getType() == Tag.INSTRUCTION_TAG || tag.getType() == Tag.COMMENT_TAG)) {
					throw new ParseException("document contains invalid element: " + StringSupport.condenseWhitespace(StringSupport.trim(element.toString(), 20, "...")));
				}
			} else {
				if (element.toString().trim().length() > 0) {
					//TODO if strict throw new ParseException("document contains invalid element: " + StringSupport.condenseWhitespace(StringSupport.trim(element.toString(), 20, "...")));
					contentsToRemove.add(element);
				}
			}
			if (nodeCount > 1) {
				throw new ParseException("document contains more than 1 node: " + StringSupport.condenseWhitespace(StringSupport.trim(element.toString(), 20, "...")));
			}
		}
		i = contentsToRemove.iterator();
		while (i.hasNext()) {
			contents.remove(i.next());
		}
	}


	/**
	 * @return the XML document as a formatted text
	 */

	public String toString(byte formattingStyle) {
		return contentsToString(formattingStyle).trim();
	}

	public String toString(byte formattingStyle, int minimumLineLength) {
		return contentsToString(formattingStyle, minimumLineLength).trim();
	}

	public String toString() {
		if (1 == 1) {
			return contentsToString().trim();
		}

		StringBuffer result = new StringBuffer();
//		if (contents.size() > 0 || (interpreteAsXHTML && !isSingleTag))
		{
			result.append(contentsToString());
		}
		return result.toString();
	}


	public boolean isPartOfText() {
		return false;
	}

	public String getName() {
		return name;
	}
}



















