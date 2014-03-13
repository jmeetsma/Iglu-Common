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

import org.ijsberg.iglu.util.misc.StringSupport;

import java.io.Serializable;
import java.util.*;

//TODO create tests, clean up and move to iglu-common

/**
 */
public abstract class ElementList implements Serializable {
	//the next flag enables load of HTML, conversion to XHTML and display of XHTML
	protected boolean interpreteAsXHTML;

	protected final static char TAB = '\t';
	protected final static String EOL = System.getProperty("line.separator");
	protected final static char NEWLINE = '\n';

	//attributes are contained in a context
	protected Properties nodeAttributes;// = new PropertyBundle();

	//contents can be XMLTexts, special tags, and subNodes
	protected ArrayList contents = new ArrayList(50);

	//formatting styles
	public static final byte LEAVE_AS_IS = 0;
	public static final byte CONDENSE = 1;
	public static final byte STRETCH = 2;

	//contains (HTML) elements that are used to mark up texts such as <b>
	protected boolean containsMarkupText;
	protected boolean containsSingleString;

	//adds contents
	protected void build(List xmlElements, boolean interpreteAsXHTML) throws ParseException {
		this.interpreteAsXHTML = interpreteAsXHTML;

		int nrofElements = xmlElements.size();
		for (int i = 0; i < nrofElements; i++) {
			Object element = xmlElements.get(i);

			if (element instanceof Node) {
				this.addNode((Node) element);
			} else if (element instanceof Tag) {
				Tag subtag = (Tag) element;

				if (subtag.type == Tag.START_TAG) {
/*					if (tagname == null)
					{
						tagname = subtag.tagname;
					}*/
					if (subtag.correspondingTag != null) {
						//collect elements inbetween
						ArrayList elementsInbetween = new ArrayList();
						int endTagPos = xmlElements.indexOf(subtag.correspondingTag);
						for (i++; i < endTagPos; i++) {
							elementsInbetween.add(xmlElements.get(i));
						}
						//skip empty markup tags in case interpreted as XHTML
						Node newNode = new Node(this instanceof Node ? (Node) this : null, subtag, elementsInbetween, interpreteAsXHTML);
						if (!newNode.isEmptyXHTMLMarkupNode())//skip empty markup
						{
							contents.add(newNode);
//							if (interpreteAsXHTML && isHTMLMarkupTag(subtag.tagname))
							if (interpreteAsXHTML && isHTMLMarkupTag(getName())) {
								containsMarkupText = true;
							}
						}
					} else {
						if (interpreteAsXHTML) {
							//add unresolved start tag as singletag node (<img src=".." />
							Node newNode = new Node(this instanceof Node ? (Node) this : null, subtag, true, interpreteAsXHTML);
							if (!newNode.isEmptyXHTMLMarkupNode())//skip empty markup
							{
								contents.add(newNode);
							}
						} else {
							throw new ParseException("starttag (" + subtag.tagname + ") without endtag found");
						}
					}
				} else if (subtag.type == Tag.SINGLE_TAG) {
					Node newNode = new Node(this instanceof Node ? (Node) this : null, subtag, true, interpreteAsXHTML);
					if (!newNode.isEmptyXHTMLMarkupNode())//skip empty markup
					{
						contents.add(newNode);
					}
				} else if (subtag.type == Tag.END_TAG) {
					//ignore
				} else {
					//comment- and instruction tags
					contents.add(element);
				}
			} else if (element instanceof /*XMLTextElement*/ String) {
				contents.add(element);
				if (element.toString().trim().length() > 0) {
					if (contents.size() == 1) //TODO what about comment tags?
					{
						containsSingleString = true;
					} else {
						containsSingleString = false;
						containsMarkupText = true;
					}
				}
			} else {
				throw new IllegalStateException("unexpected element of type " + element.getClass().getName());
			}
		}
		if (interpreteAsXHTML && contents.size() == 1) {
			Object o = (contents.get(0));
			if (o instanceof Node) {
				Node node = (Node) o;
				if (node.getName().equals(getName()) && isHTMLMarkupTag(getName())) {
					//subNode is obsolete
					if (nodeAttributes == null) {
						nodeAttributes = node.nodeAttributes;
					} else {
						nodeAttributes.putAll(node.nodeAttributes);
					}
					contents = node.contents;
				}
			}
		}
		//TODO cleanup unnecessary nested markup nodes
	}


	boolean isEmptyXHTMLMarkupNode() {
		return interpreteAsXHTML && contents.isEmpty() && isHTMLMarkupTag(getName());
	}

	public abstract String getName();

/*	public GenericPropertyBundle toProperties()
	{
		GenericPropertyBundle bundle = new GenericPropertyBundle(this.getName());
		Iterator i = this.getNodesInTree().iterator();
		while (i.hasNext())
		{
			Node node = (Node) i.next();
			bundle.setProperty(node.getName(), node.getContentsWithoutTags());
		}
		return bundle;
	}
*/

	/**
	 * @return a list of all subnodes recursively
	 */
	public List<Node> getNodesInTree() {
		return getNodes(true, null);
	}

	/**
	 * @return a list of subnodes of which this node is parent
	 */
	public List<Node> getNodesFromRoot() {
		return getNodes(false, null);
	}


	/**
	 * @return the first subnode found or null in case it doesn't exist
	 */
	public Node getFirstNodeInRoot() {
		List<Node> nodes = getNodesFromRoot();
		if (nodes.isEmpty()) {
			return null;
		} else {
			return (Node) getNodesFromRoot().get(0);
		}
	}

	/**
	 * @param name
	 * @return the first subnode with a certain name
	 */
	public Node getFirstNodeByNameInTree(String name) {
		return getFirstNodeByName(name, true);
	}

	/**
	 * @param name
	 * @param recurse indicates whether to regard all subnodes or just the nodes this node is parent of
	 * @return the first node with a certain name encountered
	 */
	public Node getFirstNodeByName(String name, boolean recurse)//TODO make all recursive methods like this
	{
		Iterator i = contents.iterator();
		Node node = null;
		while (i.hasNext()) {
			Object o = i.next();
			if (o instanceof Node) {
				node = (Node) o;
				if (node.getName().equals(name)) {
					return node;
				}
				if (recurse) {
					node = node.getFirstNodeByNameInTree(name);
					if (node != null) {
						return node;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param tagname
	 * @return a list of subnodes directly under this node, that have a certain name
	 */
	public List<Node> getNodesFromRootByName(String tagname) {
		return getNodes(false, tagname);
	}


	/**
	 * @param tagname
	 * @return a list of all subnodes with a certain name
	 */
	//TODO tagname?
	public List<Node> getNodesFromTreeByName(String tagname) {
		return getNodes(true, tagname);
	}


	/**
	 * @param name
	 * @return all subnodes that possess an attribute with a certain name
	 */
	public List<Node> getNodesFromTreeByAttributeName(String name) {
		return getNodesByAttributeName(true, name);
	}

	/**
	 * Renames all subnodes with a certain name
	 *
	 * @param oldName
	 * @param newName
	 */
	public void renameNodesInTree(String oldName, String newName) {
		List<Node> nodes = getNodesFromTreeByName(oldName);
		Iterator i = nodes.iterator();
		while (i.hasNext()) {
			Node n = (Node) i.next();
			n.setName(newName);
		}
	}

	//TODO better distinction between recursive and non-recursive methods

	/**
	 * Renames all subnodes with a certain name
	 *
	 * @param oldName
	 * @param newContents
	 */
	public void replaceNodesInTree(String oldName, String newContents) {
		//TODO parse new contents
		ArrayList replacedContents = new ArrayList();
		Iterator i = contents.iterator();
		while (i.hasNext()) {
			Object o = i.next();

			if (o instanceof Node) {
				Node node = (Node) o;
				if (node.getName().equals(oldName)) {
					replacedContents.add(newContents);
				} else {
					node.replaceNodesInTree(oldName, newContents);
					replacedContents.add(node);
				}
			} else {
				replacedContents.add(o);
			}
		}
		contents = replacedContents;
	}

	public void renderNodesInTree(String oldName, String startContents, String endContents) {
		ArrayList replacedContents = new ArrayList();
		Iterator i = contents.iterator();
		while (i.hasNext()) {
			Object o = i.next();

			if (o instanceof Node) {
				Node node = (Node) o;
				if (node.getName().equals(oldName)) {
					replacedContents.add(startContents + node.getContentsWithoutTags() + endContents);
				} else {
					node.renderNodesInTree(oldName, startContents, endContents);
					replacedContents.add(node);
				}
			} else {
				replacedContents.add(o);
			}
		}
		contents = replacedContents;
	}

	public void renderNodesMoveAttribute(String oldName, String attributeName, String startContents, String endContents) {
		ArrayList replacedContents = new ArrayList();
		Iterator i = contents.iterator();
		while (i.hasNext()) {
			Object o = i.next();

			if (o instanceof Node) {
				Node node = (Node) o;
				if (node.getName().equals(oldName)) {
					replacedContents.add(node);
					replacedContents.add(startContents + node.getAttribute(attributeName) + endContents);
				} else {
					node.renderNodesMoveAttribute(oldName, attributeName, startContents, endContents);
					replacedContents.add(node);
				}
			} else {
				replacedContents.add(o);
			}
		}
		contents = replacedContents;
	}

	public void removeNodesInTreeByName(String oldName) {
		ArrayList replacedContents = new ArrayList();
		Iterator i = contents.iterator();
		while (i.hasNext()) {
			Object o = i.next();

			if (o instanceof Node) {
				Node node = (Node) o;
				if (!node.getName().equals(oldName)) {
					node.removeNodesInTreeByName(oldName);
					replacedContents.add(node);
				} else {
//					replacedContents.add(node);
				}
			} else {
				replacedContents.add(o);
			}
		}
		contents = replacedContents;
	}


	/**
	 * Rename all nodes with a certain name and a certain attribute name-value pair
	 *
	 * @param oldName
	 * @param newName
	 * @param attributeName
	 * @param attributeValue
	 */
	public void renameNodesWithAttributeValue(String oldName, String newName, String attributeName, String attributeValue) {
		List<Node> nodes = getNodesFromTreeByName(oldName);
		Iterator i = nodes.iterator();
		while (i.hasNext()) {
			Node n = (Node) i.next();
			n.setName(newName);
		}
	}

	/**
	 * @param all  indicates if all subnodes must be retrieved or only the nodes this node is parent of
	 * @param name
	 * @return all subnodes that possess an attribute with a certain name
	 */
	protected List<Node> getNodesByAttributeName(boolean all, String name) {
		ArrayList<Node> result = new ArrayList<Node>();
		Iterator i = contents.iterator();

		while (i.hasNext()) {
			Object o = i.next();
			if (o instanceof Node) {
				Node subNode = (Node) o;
				if ((name == null) || (subNode.nodeAttributes != null && subNode.nodeAttributes.containsKey(name))) {
					result.add(subNode);
				}
				if (all) {
					result.addAll(subNode.getNodesFromTreeByAttributeName(name));
				}
			}
		}
		return result;
	}

	//fromTree / fromRoot <-> recursive

	/**
	 * @param recursive  indicates if all subnodes must be retrieved or only the nodes this node is parent of
	 * @param name name of nodes that must be returned (exludes nodes with other names)
	 * @return a list of subnodes
	 */
	protected List<Node> getNodes(boolean recursive, String name) {
		ArrayList<Node> result = new ArrayList<Node>();
		Iterator i = contents.iterator();

		while (i.hasNext()) {
			Object o = i.next();
			if (o instanceof Node) {
				Node subNode = (Node) o;
				if ((name == null) || subNode.getName().equals(name)) {
					result.add(subNode);
				}
				if (recursive) {
					result.addAll(subNode.getNodes(recursive, name));
				}
			}
		}
		return result;
	}

	/**
	 * @return a list of strings and / or subnodes contained in this node, in order of appearance
	 */
	public ArrayList getContentsList() {
		return contents;
	}

	/**
	 * @return the contents of this node and subnodes in text form, without any tags
	 */
	public String getContentsWithoutTags() {
		StringBuffer result = new StringBuffer("");
		Iterator i = contents.iterator();

		while (i.hasNext()) {
			Object o = i.next();
			if (o instanceof Node) {
				Node subNode = (Node) o;
				result.append(subNode.getContentsWithoutTags());
			} else if (o instanceof String) {
				result.append(o.toString());
			} else if (o instanceof Tag) {
				//skip comment etc.
			} else {
				throw new IllegalStateException("Node " + getName() + " contains illegal element " + o.getClass().getName() + " -> " + o);
			}
		}
		return result.toString().trim();
	}

	/**
	 * Replaces the contents of this node with a single text
	 *
	 * @param value
	 */
	public void setValue(String value) {
		contents = new ArrayList();
		contents.add(value);
		if (value.trim().length() > 0) {
			containsSingleString = true;
		}
	}

	/**
	 * Appends a piece of text to the contents of this node
	 *
	 * @param value
	 */
	public void addValue(String value) {
		contents.add(value);
		if (value.trim().length() > 0) {
			if (contents.size() == 1) {
				containsSingleString = true;
			} else {
				containsSingleString = false;
				containsMarkupText = true;
			}
		}
	}

	/**
	 * Empties this node
	 */
	public void deleteContents() {
		contents = new ArrayList();
		containsMarkupText = false;
		containsSingleString = false;
	}

	/**
	 * Replaces the contents (subnodes and text) of this node by new contents
	 *
	 * @param contents
	 */
	public void setContents(ArrayList contents) {
		this.contents = contents;
		containsMarkupText = false;
		containsSingleString = false;
		Iterator i = contents.iterator();

		while (i.hasNext()) {
			Object o = i.next();
			if (!(o instanceof Node)) {
				if (o instanceof String && o.toString().trim().length() > 0) {
					if (contents.size() == 1) {
						containsSingleString = true;
					} else {
						containsSingleString = false;
						containsMarkupText = true;
					}
				}
			} else {
				Node node = (Node) o;
//				node.setDepth(depth + 1);
				node.parentNode = this instanceof Node ? (Node) this : null;
			}
		}
	}


	/**
	 * Replaces the contents of this node with a single text
	 *
	 * @param contents
	 */
	public void setContents(String contents) {
/*TODO if strict        if(contents.indexOf('<') != -1 || contents.indexOf('>') != -1)
        {
            throw new IllegalArgumentException("node may not contain tag characters");
        }*/
		setValue(contents);
	}


	/**
	 * Adds a subnode to the contents
	 *
	 * @param subNode
	 */
	public Node addNode(Node subNode) {
		if (this instanceof Node) {
			subNode.parentNode = (Node) this;
		}
		contents.add(subNode);
		return subNode;
	}


	public Node addNode(int index, Node subNode) {
		subNode.parentNode = this instanceof Node ? (Node) this : null;
		contents.add(index, subNode);
		return subNode;
	}


	protected static class Tag {
		protected String contents;
		public static final byte UNDETERMINED_TAG = 0;
		public static final byte COMMENT_TAG = 1;// <!..
		public static final byte END_TAG = 2;// </..
		public static final byte INSTRUCTION_TAG = 3;// <?..
		public static final byte START_TAG = 4;// <..>
		public static final byte SINGLE_TAG = 5;// ../>

		private byte type = UNDETERMINED_TAG;

		protected String tagname;
		protected Properties attributes;
		protected Tag correspondingTag;

		private StringBuffer nameCollector = new StringBuffer();
		private StringBuffer valueCollector;

		protected int lineNr;


		private Tag(int lineNr, byte type, String tagname, Properties attributes, Tag correspondingTag) {
			this.lineNr = lineNr;
			this.type = type;
			this.tagname = tagname;
			this.attributes = attributes;
			this.correspondingTag = correspondingTag;
		}

		protected Tag(int lineNr, String contents) throws ParseException {
			this.lineNr = lineNr;
			char firstCharacter = contents.charAt(0);
			int contentsLength = contents.length();
			char lastCharacter = contents.charAt(contentsLength - 1);
			contentsLength = determineTagType(contents, firstCharacter,
					contentsLength, lastCharacter);
			if (type >= INSTRUCTION_TAG) {
				//TODO introduce method class TagParser to sort this rubbish out

				boolean collectingName = true;
				boolean collectingAttrName = false;
				boolean insideQuotes = false;

				char currentChar = firstCharacter;
				int position = 0;
				if (type == INSTRUCTION_TAG) {
					position = 1;
				}

				while (position < contentsLength) {
					currentChar = contents.charAt(position);
					if (collectingName) {
						if (Character.isWhitespace(currentChar)) {
							collectingName = false;
							tagname = nameCollector.toString();
							nameCollector.delete(0, nameCollector.length());
							collectingAttrName = true;
						} else {
							nameCollector.append(currentChar);
						}
					} else if (collectingAttrName) {
						if (currentChar == '=') {
							collectingAttrName = false;
						} else if (!Character.isWhitespace(currentChar)) {
							//TODO check for invalid characters
							nameCollector.append(currentChar);
						}
					} else {
						if (valueCollector == null) {
							valueCollector = new StringBuffer();
							attributes = new Properties();
						}
						if (currentChar == '"') {
							if (insideQuotes) {
								appendAttribute(nameCollector.toString().trim(), valueCollector.toString());
								nameCollector.delete(0, nameCollector.length());
								valueCollector.delete(0, valueCollector.length());
								insideQuotes = false;
								collectingAttrName = true;
							} else {
								insideQuotes = true;
							}
						} else if (Character.isWhitespace(currentChar)) {
							if (!insideQuotes && valueCollector.length() > 0) {
								appendAttribute(nameCollector.toString(), valueCollector.toString());
								nameCollector.delete(0, nameCollector.length());
								valueCollector.delete(0, valueCollector.length());
								insideQuotes = false;
								collectingAttrName = true;
							} else {
								valueCollector.append(currentChar);
							}
						} else {
							valueCollector.append(currentChar);
						}
					}
					position++;
				}
				if (collectingName && nameCollector.length() > 0) {
					tagname = nameCollector.toString();
				} else if (nameCollector.length() > 0 && valueCollector != null && valueCollector.length() > 0) {
					appendAttribute(nameCollector.toString(), valueCollector.toString());
				}
				//TODO sanity checks;
			}
		}

		private int determineTagType(String contents, char firstCharacter,
									 int contentsLength, char lastCharacter) {
			switch (firstCharacter) {
				case '!':
					type = COMMENT_TAG;
					this.contents = contents.substring(1);
					break;
				case '?':
					type = INSTRUCTION_TAG;
					this.contents = contents.substring(1, contents.length() - 1);
					break;
				case '/':
					type = END_TAG;
					tagname = contents.substring(1);
					break;
				default:
					if (lastCharacter == '/') {
						type = SINGLE_TAG;
						contentsLength--;
					} else {
						type = START_TAG;
					}
			}
			return contentsLength;
		}

		public byte getType() {
			return type;
		}

		private void appendAttribute(String key, Object value) {
			if (attributes == null) {
				attributes = new Properties();
			}
			attributes.put(key, value);
		}

		public Properties getAttributes() {
			return attributes;
		}

		public String toString() {
			switch (type) {
				case COMMENT_TAG:
					return "<!" + this.contents + '>';
				case INSTRUCTION_TAG:
					return "<?" + this.contents + "?>";
				case END_TAG:
					return "</" + tagname + '>';
				default:
					return '<' + tagname + contextToNodeAttributes(attributes) + (type == SINGLE_TAG ? " /" : "") + '>';
			}
		}
	}

	public static String contextToNodeAttributes(Properties propertyBundle) {
		if (propertyBundle == null) {
			return "";
		}
		StringBuffer retval = new StringBuffer();
		Iterator i = propertyBundle.keySet().iterator();
		while (i.hasNext()) {
			String p = (String) i.next();
			retval.append(' ').append(p).append("=\"").append(propertyBundle.getProperty(p)).append('\"');
		}
		return retval.toString();
	}

	public static ArrayList split(String xmlInput, boolean interpreteAsXHTML, boolean strict) throws ParseException {
		//contents will be split into starttags, endtags and pieces of text inbetween
		//during processing start- and endtags will be matched and connected, thus determining the contents of a (sub)node

		long startTime = System.currentTimeMillis();

		//tags for which no endtag is found will be stored temporarily
		//Map of ArrayLists of tags by tagname
		HashMap looseStartTags = new HashMap();

		//list that contains XML text as a series of elements (tags and texts)
		ArrayList splitContents = new ArrayList(100);
		//true if the previously found character marks the start of a tag ('<')
		boolean insideTag = false;

		//position in the input string previously
		int currentPosition = 0;
		int nextPosition = 0;
		int testPosition = 0;


		int currentLineNr = 1;


		//container for found XML elements
		String text;
		boolean processingCData = false;

		while (nextPosition != -1) {
			if (insideTag) {
				//TODO extra care with script-tag
				if (xmlInput.charAt(currentPosition) == '!') {
					//make sure the proper ends of comment and data tags are located
					if (xmlInput.charAt(currentPosition + 1) == '-') {
						nextPosition = xmlInput.indexOf("-->", currentPosition) + 2;
					} else if (xmlInput.charAt(currentPosition + 1) == '[') {
						nextPosition = xmlInput.indexOf("]]>", currentPosition) + 2;
						processingCData = true;
					} else//(in case of !DOCTYPE)
					{
						//locate the end of the tag
						nextPosition = xmlInput.indexOf('>', currentPosition);
					}
				} else {
					//locate the end of the tag
					nextPosition = xmlInput.indexOf('>', currentPosition);
				}
				testPosition = xmlInput.indexOf('<', currentPosition);
/*      TODO if strict / exclude CDATA          if(!processingCData && testPosition >= 0 && testPosition < nextPosition)
                {
                    throw new ParseException("misplaced '<' found: " + xmlInput.substring(testPosition, nextPosition));
                }*/
			} else {
				//locate the start of the next (or first) tag
				nextPosition = xmlInput.indexOf('<', currentPosition);
/*      TODO if strict          testPosition = xmlInput.indexOf('>', currentPosition);
                if(testPosition >= 0 && testPosition < nextPosition)
                {
                    throw new ParseException("misplaced '>' found " + xmlInput.substring(testPosition, nextPosition));
                }*/
			}

			if (nextPosition != -1) {
				//read until the end of the tag or the next tag
				text = xmlInput.substring(currentPosition, nextPosition);
			} else {
				//read until the end of the input
				text = xmlInput.substring(currentPosition);
			}

			if (insideTag)//inbetween '<' and '>'
			{
				if (text.length() > 0) {
					//construct a tag out of the text containing tagname and possibly attributes
					Tag tag = new Tag(currentLineNr, text);
					if (interpreteAsXHTML && tag.type != Tag.COMMENT_TAG && tag.type != Tag.INSTRUCTION_TAG) {
						//convert the names of XHTML tags to lower case
						tag.tagname = tag.tagname.toLowerCase();
					}
					//add tag to collection
					splitContents.add(tag);

					if (tag.type == tag.START_TAG) {
						//stack starttags to be connected to appropriate endtags later
						ArrayList looseStartTagsByName = (ArrayList) looseStartTags.get(tag.tagname);
						if (looseStartTagsByName == null) {
							looseStartTagsByName = new ArrayList();
							looseStartTags.put(tag.tagname, looseStartTagsByName);
						}
						looseStartTagsByName.add(tag);
					} else if (tag.type == tag.END_TAG) {
						//try to connect start- and endtags
						ArrayList looseStartTagsByName = (ArrayList) looseStartTags.get(tag.tagname);
						if (looseStartTagsByName != null && !looseStartTagsByName.isEmpty()) {
							//get the starttag on top of the stack to maintain proper nesting order
							Tag startTag = (Tag) looseStartTagsByName.remove(looseStartTagsByName.size() - 1);
							tag.correspondingTag = startTag;
							startTag.correspondingTag = tag;

							int currentEndTagPos = splitContents.size() - 1;
							int currentStartTagPos = splitContents.indexOf(startTag);

							if (interpreteAsXHTML) {
								//if the input is an HTML file we can not trust upon correct nesting of tags
								//
								//can we locate starttag-endtag combinations that conflict
								//  with the starttag we just resolved?

								//walk from starttag to endtag
								for (int i = currentStartTagPos; i <= currentEndTagPos; i++) {
									Object examinedElement = splitContents.get(i);

									if (examinedElement instanceof Tag) {
										Tag examinedTag = (Tag) examinedElement;
										if (examinedTag.type == Tag.END_TAG) {
											int examinedStartTagPos = splitContents.indexOf(examinedTag.correspondingTag);
											if (examinedStartTagPos < currentStartTagPos) {
												if (isHTMLMarkupTag(examinedTag.tagname)) {
													//examined tag must hop over start of current tag
													//end node
													Tag insertedEndTag = new Tag(examinedTag.lineNr, Tag.END_TAG, examinedTag.tagname, null, examinedTag.correspondingTag);
													splitContents.add(currentStartTagPos, insertedEndTag);
													//start new node
													Tag insertedStartTag = new Tag(examinedTag.lineNr, Tag.START_TAG, examinedTag.tagname, new Properties(examinedTag.correspondingTag.attributes), examinedTag);
													splitContents.add(currentStartTagPos + 2, insertedStartTag);
													//fix references
													examinedTag.correspondingTag.correspondingTag = insertedEndTag;
													examinedTag.correspondingTag = insertedStartTag;
													//increase i to skip inserted tags
													i += 2;
													//increase position of endtag because of inserted tags
													currentEndTagPos += 2;
													currentStartTagPos += 1;
												} else if (isHTMLMarkupTag(startTag.tagname)) {
													//current tag must hop over end of examined tag
													//end node
													Tag insertedEndTag = new Tag(startTag.lineNr, Tag.END_TAG, startTag.tagname, null, startTag);
													splitContents.add(i, insertedEndTag);
													//start new node
													Tag insertedStartTag = new Tag(startTag.lineNr, Tag.START_TAG, startTag.tagname, new Properties(startTag.attributes), tag);
													splitContents.add(i + 2, insertedStartTag);
													//fix references
													startTag.correspondingTag = insertedEndTag;
													tag.correspondingTag = insertedStartTag;
													//increase i to skip inserted tags
													i += 2;
													//increase position of endtag because of inserted tags
													currentEndTagPos += 2;
												} else {
													if (strict) {
														throw new ParseException("cannot resolve nesting error of tags " + startTag.tagname + " and " + examinedTag.tagname);
													}
													//examined tag must include current tag
													//remove corresponding end tag
													splitContents.remove(examinedTag.correspondingTag);
													//add it after current end tag
													splitContents.add(currentEndTagPos, examinedTag.correspondingTag);
												}
											}
										}
									}
								}
							}
							//TODO if this is invalid XML is, it may contain unresolved starttags
							//in case of (X)HTML interpretation
							//these must be coupled with endtags outside the hierarchy or be
							//changed to singletags

							//SHORTCUT
							if (!interpreteAsXHTML && currentStartTagPos != 0) {
								List subList = splitContents.subList(currentStartTagPos + 1, currentEndTagPos);
								Node node = new Node(null, tag.correspondingTag, subList, interpreteAsXHTML);
								for (int x = currentStartTagPos; x <= currentEndTagPos; x++) {
									Object o = splitContents.remove(currentStartTagPos);
								}
								splitContents.add(node);
							}
						} else {
							//endtag without a start tag encounterd
							if (!strict/* || interpreteAsXHTML*/) {
								//remove loose end tags
								splitContents.remove(splitContents.size() - 1);
							} else {
								//TODO line nr (count enters)
								throw new ParseException("endtag " + tag.tagname + " without a starttag found");
							}
						}
					}
				}
				//text inside tag is collected, now proceed outside tag
				insideTag = false;
			} else {
				splitContents.add(text);
				//text inbetween tags is collected, now proceed inside next tag
				insideTag = true;
			}
			currentLineNr += StringSupport.count(text, "\n");

			currentPosition = nextPosition + 1;
		}
		//TODO catch throwable and use currentPosition for feedback
		return splitContents;
	}

	//currently known HTML markup tagnames
	public static final String[] htmlMarkupTags = {"ABBR", "ACRONYM", "B", "BASEFONT", "BIG", "BLINK", "CITE", "CODE", "DEL", "DFN", "EM", "FONT", "I", "INS", "KBD", "NOBR", "Q", "S", "SAMP", "SMALL", "STRIKE", "STRONG", "SUB", "SUP", "TT", "U", "VAR", "SPAN"};
	//structure tags that encapsulates text
	public static final String[] htmlStructureTags = {"TABLE", "TR", "TD", "DIV", "FORM"};
	public static final String[] htmlLineBreakSuitable = {"DIV", "BR", "P", "LI", "H1", "H2", "H3", "H4", "H5", "H6"};

	/**
	 * @param tagName
	 * @return true if this node stands for text markup in an HTML context
	 */
	public static boolean isHTMLMarkupTag(String tagName) {
		for (int i = 0; i < htmlMarkupTags.length; i++) {
			if (htmlMarkupTags[i].equalsIgnoreCase(tagName)) {
				return true;
			}
		}
		return false;
	}

	public boolean isHTMLMarkupTag() {
		return isHTMLMarkupTag(getName());
	}


	/**
	 * @return the contents of the XML node as a formatted text
	 */
	public String contentsToString(byte formattingStyle, int minimumLineSize) {
		return contentsToString(EOL, formattingStyle, minimumLineSize);
	}


	public String contentsToString(byte formattingStyle) {
		return contentsToString(EOL, formattingStyle, -1);
	}


	public String contentsToString(String lineFeed, byte formattingStyle) {
		return contentsToString(lineFeed, formattingStyle, -1);
	}

	public abstract boolean isPartOfText();


	public boolean contentsIsWhiteSpace() {
		Iterator i = contents.iterator();
		while (i.hasNext()) {
			if (i.next().toString().trim().length() > 0) {
				return false;
			}
		}
		return true;
	}

	public String contentsToString() {
		return contentsToString(EOL, CONDENSE);
	}

	public String contentsToStringFast(String lineFeed) {
		StringBuffer result = new StringBuffer();

		LOOP:
		for(Object o : contents) {

			if (o instanceof String || o instanceof Tag) {
				String text = o.toString();
				result.append(text);
			} else if (o instanceof Node) {
//					result.append(((Node)o).toString(depth + 1, formattingStyle));
				String text = ((Node) o).toStringFast(lineFeed + TAB);
				result.append(text);
			} else {
				throw new IllegalStateException("node " + getName() + " contains invalid element " + o.getClass().getName() + " -> " + o);
			}
			result.append(lineFeed);
		}
		return result.toString();
	}

	/**
	 * @return the contents of the XML node as a formatted text
	 */
	public String contentsToString(String lineFeed, byte formattingStyle, int minimumLineSize) {
		StringBuffer result = new StringBuffer();
		if (!contents.isEmpty() && (!contentsIsWhiteSpace() || formattingStyle == LEAVE_AS_IS)) {
			if (formattingStyle == STRETCH || !(containsSingleString || (containsMarkupText || isPartOfText()))) {
				//start output on a new, indented line if elements are not (part of) text or formattingstyle is STRETCHED
				if (!contentsIsWhiteSpace() && formattingStyle != LEAVE_AS_IS) {
					result.append(lineFeed);
/*                    result.append(EOL);
                    for (int x = 0; x < depth; x++)
                    {
                        result.append(TAB);
                    }*/
				}
			}

			Iterator i = contents.iterator();

			int currentLineLength = 0;
			Object o;
			LOOP:
			while (i.hasNext()) {
				o = i.next();
				if (o instanceof String || o instanceof Tag) {
					String text = o.toString();
					if (o instanceof String/* && isPartOfText()*/) {
						if (formattingStyle != LEAVE_AS_IS) {
							if (text.trim().length() == 0) {
								continue LOOP;
							}
							text = StringSupport.condenseWhitespace(o.toString());
							if (/*!isPartOfText() || */containsSingleString || formattingStyle == STRETCH) {
								//avoid unnecessary empty lines
								text = text.trim();
							}
							currentLineLength += text.length();
							//split long lines here if desired
							if (minimumLineSize >= 0 && currentLineLength > minimumLineSize) {
//                                if(currentLineLength > 40)
								{
//                                    result.append(lineFeed);
								}
								text = StringSupport.replaceAll(text, " ", lineFeed, minimumLineSize);
								currentLineLength = text.length() - text.lastIndexOf(lineFeed);
							}
							result.append(text);
						} else {
							currentLineLength += text.length();
							result.append(text);
						}
					} else // Tag
					{
//						currentLineLength += text.length();
//						result.append(text);
					}
				} else if (o instanceof Node) {
//					result.append(((Node)o).toString(depth + 1, formattingStyle));
					String text = null;
					if (containsMarkupText || isPartOfText()) {
						if (formattingStyle == STRETCH) {
							text = ((Node) o).toString(lineFeed + TAB, formattingStyle, minimumLineSize);
						} else {
							text = ((Node) o).toString(lineFeed, formattingStyle, minimumLineSize);
						}
//                        int prevLineLength = currentLineLength;
						if (minimumLineSize >= 0 && currentLineLength > minimumLineSize) {
//                            result.append(lineFeed + "3-" + prevLineLength + "-" + currentLineLength);
//                            text.append(lineFeed + "3-" + text.lastIndexOf(TAB) + "-" + text.length());
							text += lineFeed;
//                            currentLineLength = 0;
						}
						currentLineLength = text.length() - text.lastIndexOf(lineFeed);
						if (minimumLineSize >= 0 && currentLineLength > minimumLineSize) {
							text += lineFeed;
							currentLineLength = 0;
						}
/*                        if(result.length() > 0 && Character.isWhitespace(result.charAt(result.length() - 1)))
                        {
                            result.append(" ");
                        }*/
					} else {
						text = ((Node) o).toString(lineFeed + TAB, formattingStyle, minimumLineSize);
					}


					result.append(text);
				} else {
					throw new IllegalStateException("node " + getName() + " contains invalid element " + o.getClass().getName() + " -> " + o);
				}
				if (formattingStyle != LEAVE_AS_IS && (formattingStyle == STRETCH || !(containsSingleString || (containsMarkupText || isPartOfText()))))
//                    if (formattingStyle == STRETCH || !(containsSingleString || (containsMarkupText || isPartOfText()))/* && !(interpreteAsXHTML && this.isHTMLMarkupTag())*/)
				{
					result.append(lineFeed);
/*                    result.append(EOL);
                    for (int x = 0; x < depth; x++)
                    {
                        result.append(TAB);
                    }*/
				}
			}
		}
		return result.toString();
	}


	/**
	 * @return the contents of the XML node as a formatted text
	 */
	public String contentsToMaintainableHtml() {
		return contentsToMaintainableHtml(EOL, -1);
	}

	/**
	 * @return the contents of the XML node as a formatted text
	 */
	public String contentsToMaintainableHtml(String lineFeed, int minimumLineSize) {
		StringBuffer result = new StringBuffer();
		if (!contents.isEmpty() && !contentsIsWhiteSpace()) {
			if (!(containsSingleString || (containsMarkupText || isPartOfText()))) {
				//start output on a new, indented line if elements are not (part of) text or formattingstyle is STRETCHED
				result.append(lineFeed);
			}
			Iterator i = contents.iterator();

			int currentLineLength = 0;
			Object o;
			LOOP:
			while (i.hasNext()) {
				o = i.next();
				if (o instanceof String) {
					String text = o.toString();
					if (text.trim().length() == 0) {
						continue LOOP;
					}
					text = StringSupport.condenseWhitespace(text);
					if (containsSingleString) {
						//avoid unnecessary empty lines
						//text = text.trim();
					}
					currentLineLength += text.length();
					//split long lines here if desired
					if (minimumLineSize >= 0 && currentLineLength > minimumLineSize) {
						text = StringSupport.replaceAll(text, " ", lineFeed, minimumLineSize);
						currentLineLength = text.length() - text.lastIndexOf(lineFeed);
					}
					result.append(text);
				} else if (o instanceof Node) {
					Node node = (Node) o;
					String text = null;
					if (wantsHtmlLineBreak(node)) {
						text = ((Node) o).toHtmlString(lineFeed, minimumLineSize);
						text += lineFeed;
					} else if (containsMarkupText || isPartOfText()) {
						text = ((Node) o).toHtmlString(lineFeed, minimumLineSize);
						if (minimumLineSize >= 0 && currentLineLength > minimumLineSize) {
							text += lineFeed;
						}
						currentLineLength = text.length() - text.lastIndexOf(lineFeed);
						if (minimumLineSize >= 0 && currentLineLength > minimumLineSize) {
							text += lineFeed;
							currentLineLength = 0;
						}
					} else {
						text = ((Node) o).toHtmlString(lineFeed + TAB, minimumLineSize);
					}
					result.append(text);
				} else if (o instanceof Tag) {

				} else {
					throw new IllegalStateException("node " + getName() + " contains invalid element " + o.getClass().getName() + " -> " + o);
				}
				if ((!(containsSingleString || (containsMarkupText || isPartOfText()))))
//                    if (formattingStyle == STRETCH || !(containsSingleString || (containsMarkupText || isPartOfText()))/* && !(interpreteAsXHTML && this.isHTMLMarkupTag())*/)
				{
					result.append(lineFeed);
/*                    result.append(EOL);
                    for (int x = 0; x < depth; x++)
                    {
                        result.append(TAB);
                    }*/
				}
			}
		}
		return result.toString();
	}

	private static boolean wantsHtmlLineBreak(Node node) {
		for (String htmlTagName : htmlLineBreakSuitable) {
			if (node.getName().equalsIgnoreCase(htmlTagName)) {
				return true;
			}
		}
		return false;
	}


}
