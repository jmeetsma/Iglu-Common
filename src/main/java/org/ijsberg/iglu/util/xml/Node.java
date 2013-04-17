/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.util.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.ijsberg.iglu.util.collection.CollectionSupport;
import org.ijsberg.iglu.util.io.FileSupport;

/**
 * Recursive XML element defined by XML tags
 * A node is capable of reading an XML document from a text
 * It's also capable of reading and converting pseudo-XML (such as HTML)
 */
public class Node extends ElementList
{

	protected String tagname;
	protected boolean isSingleTag;
	protected Node parentNode;

	int startLineNr = -1;
	int endLineNr = -1;

	/**
	 * Constructs an empty node without a name
	 */
	Node()
	{
	}

	//TODO differentiate between HTML parsing, XHTML parsing (& display), XML parsing

	Node(Node parent, Tag tag, boolean isSingleTag, boolean interpreteAsXHTML) throws ParseException
	{
		this.parentNode = parent;
		this.tagname = tag.tagname;
		
		startLineNr = tag.lineNr;
		endLineNr = tag.correspondingTag != null ? tag.correspondingTag.lineNr : startLineNr;
		
		nodeAttributes = tag.attributes;

		this.isSingleTag = isSingleTag;
	}


	Node(Node parent, Tag tag, List xmlElements, boolean interpreteAsXHTML) throws ParseException
	{
		this(parent, tag, false, interpreteAsXHTML);
		build(xmlElements, interpreteAsXHTML);
	}


	public Node(String tagName, Properties properties)
	{
		this(tagName);
		Iterator i = properties.keySet().iterator();
		while (i.hasNext())
		{
			String property = (String) i.next();
	/*		if (property.getValue().getSource() instanceof GenericPropertyBundle)
			{
				addNode(new Node((GenericPropertyBundle) property.getValue().getSource()));
			}
			else*/
			{
				addNode(new Node(property, properties.getProperty(property)));
			}
		}
	}


	/**
	 * Constructs a node with a (tag) name
	 *
	 * @param tagname
	 */
	public Node(String tagname)
	{
		this.tagname = tagname;
	}

	/**
	 * Constructs a node with a (tag) name
	 *
	 * @param tagname
	 */
	public Node(String tagname, boolean interpreteAsXHTML)
	{
		this.tagname = tagname;
		this.interpreteAsXHTML = interpreteAsXHTML;
	}

	/**
	 * Constructs a node under a parent with a (tag) name
	 *
	 * @param parent
	 * @param tagname
	 */
/*	public Node(Node parent, String tagname)
	{
		this.parentNode = parent;
		this.tagname = tagname;
	}
*/
	/**
	 * Constructs a node with a (tag) name and content
	 * Note: value may not contain XML tags itself
	 *
	 * @param name
	 * @param value
	 */
	private Node(String name, String value)
	{
		this(name);
		setValue(value);
	}


	/**
	 * @return the (tag) name
	 */
	public String getName()
	{
		return tagname;
	}

	/**
	 * Sets the (tag) name of this node
	 *
	 * @param name
	 */
	public void setName(String name)
	{
		this.tagname = name;
	}


	/**
	 * @param key
	 * @return attribute value corresponding to the given key
	 */
	public String getAttribute(String key)
	{
		if (nodeAttributes == null)
		{
			return null;
		}
		return nodeAttributes.getProperty(key);
	}

	/**
	 * @return attributes as defined in the tag
	 */
	public Properties getAttributes()
	{
		if (nodeAttributes == null)
		{
			nodeAttributes = new Properties();
		}
		return nodeAttributes;
	}

	/**
	 * Sets a name-value pair that appears as attribute in the opening tag
	 *
	 * @param key
	 * @param value
	 */
	public void setAttribute(String key, String value)
	{
/*		if (!attributes.containsKey(key))
		{
			sortedAttributes.add(key);
		}*/
		if (nodeAttributes == null)
		{
			nodeAttributes = new Properties();
		}
		nodeAttributes.setProperty(key, value);
	}


	/**
	 * Removes an attribute
	 *
	 * @param key
	 */
	public void removeAttribute(String key)
	{
		if (nodeAttributes != null)
		{
			nodeAttributes.remove(key);
		}
	}


	public void parse(String xmlInput) throws ParseException
	{
		parse(xmlInput, false);
	}

	//parses input, adds contents
	public void parse(String xmlInput, boolean strict) throws ParseException
	{
		if (xmlInput == null)
		{
			throw new IllegalArgumentException("input can not be null");
		}
		ArrayList splitContents = split(xmlInput, interpreteAsXHTML, true);
		build(splitContents, interpreteAsXHTML);
	}


	/**
	 * @return true if this node is defined in the middle of some text content of a parent node
	 */
	public boolean isPartOfText()
	{
		return parentNode != null && (parentNode.isPartOfText() || parentNode.containsMarkupText);
	}

	public String toString()
	{
		return toString(EOL + TAB, CONDENSE, -1);
	}


	public String toString(byte formattingStyle)
	{
		return toString(EOL + TAB, formattingStyle, -1);
	}

	public String toString(byte formattingStyle, int minimumLineSize)
	{
		return toString(EOL + TAB, formattingStyle, minimumLineSize);
	}

	/**
	 * @return the XML node as a formatted text
	 */
	public String toString(String lineFeed, byte formattingStyle, int minimumLineSize)
	{
		if (tagname == null)
		{
			return "";
		}

		StringBuffer result = new StringBuffer();
/*		for (int x = 0; x < depth; x++)
		{
			result.append("x\t");
		}*/
		result.append("<" + tagname);

		result.append(contextToNodeAttributes(nodeAttributes));

		if (!contents.isEmpty() || (interpreteAsXHTML && !isSingleTag))
		{
			result.append('>');
			result.append(contentsToString(lineFeed, formattingStyle, minimumLineSize));
			if ((formattingStyle == STRETCH || !(/*containsMarkupText ||*/ isPartOfText())) && result.length() > 0 && result.charAt(result.length() - 1) == TAB && formattingStyle != LEAVE_AS_IS)
			{
				result.deleteCharAt(result.length() - 1);
			}

			result.append("</" + tagname + '>');
		}
		else
		{
			result.append(" />");
		}
		return result.toString();
	}

    /**
     * @return the XML node as a formatted text
     */
    public String toHtmlString(String lineFeed, int minimumLineSize)
    {
        if (tagname == null)
        {
            return "";
        }

        StringBuffer result = new StringBuffer();
/*		for (int x = 0; x < depth; x++)
		{
			result.append("x\t");
		}*/
        result.append("<" + tagname);

        result.append(contextToNodeAttributes(nodeAttributes));

        if (!contents.isEmpty() || (interpreteAsXHTML && !isSingleTag))
        {
            result.append('>');
            result.append(contentsToMaintainableHtml(lineFeed, minimumLineSize));
            if (( !(/*containsMarkupText ||*/ isPartOfText())) && result.length() > 0 && result.charAt(result.length() - 1) == TAB)
            {
                result.deleteCharAt(result.length() - 1);
            }

            result.append("</" + tagname + '>');
        }
        else
        {
            result.append(" />");
        }
        return result.toString();
    }
    //TODO do not format comment! <!--


	/**
	 * @return An empty node cloned from this one
	 */
	protected Node cloneEmpty()
	{
		Node node = new Node(tagname);
		node.interpreteAsXHTML = interpreteAsXHTML;

		node.nodeAttributes = new Properties(nodeAttributes);
		return node;
	}


	/**
	 * @return An empty node cloned from this one
	 */
	public Node cloneFull()
	{
		Node node = new Node(tagname);
		node.interpreteAsXHTML = interpreteAsXHTML;
		if (nodeAttributes != null)
		{
			node.nodeAttributes = new Properties(nodeAttributes);
			for(Object key : nodeAttributes.keySet()) {
				node.nodeAttributes.put(key, nodeAttributes.get(key));
			}
		}
		Iterator i = contents.iterator();
		while (i.hasNext())
		{
			Object o = i.next();
			if (o instanceof Node)
			{
				node.addNode(((Node) o).cloneFull());
			}
			else
			{
				node.contents.add(o);
			}
		}

		return node;
	}


	/**
	 * @return root node of the whole document
	 */
	private Node getRootNode()
	{
		if (parentNode != null)
		{
			return parentNode.getRootNode();
		}
		return this;
	}


	public void setInterpreteAsXHTML(boolean interpreteAsXHTML)
	{
		this.interpreteAsXHTML = interpreteAsXHTML;
	}
	
	public int getStartLineNr() {
		return this.startLineNr;
	}

	public int getNrofLines() {
		return this.endLineNr - this.startLineNr + 1;
	}
	
	
	public static void main(String[] args) {
		List<File> files = FileSupport.getFilesInDirectoryTree("/Users/jmeetsma/development/directory-scan/src/", "*ijsberg*.java");
		System.out.println(files.size());
		for(File file : files) {
			System.out.print(file.getName() + ", ");
		}
	}

}
