package io.github.tanguygab.logicsim3;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XML Component
 * 
 * taken from https://argonrain.wordpress.com/2009/10/27/000/
 * 
 * @author unknown
 * @version 1.0
 */
public class Xml {

	private String name;
	private String content;
	private Map<String, String> nameAttributes = new HashMap<String, String>();
	private Map<String, ArrayList<io.github.tanguygab.logicsim3.Xml>> nameChildren = new HashMap<String, ArrayList<io.github.tanguygab.logicsim3.Xml>>();

	public Xml(InputStream inputStream, String rootName) {
		this(rootElement(inputStream, rootName));
	}

	public Xml(String filename, String rootName) {
		this(fileInputStream(filename), rootName);
	}

	public Xml(String rootName) {
		this.name = rootName;
	}

	private Xml(Element element) {
		this.name = element.getNodeName();
		this.content = element.getTextContent();
		NamedNodeMap namedNodeMap = element.getAttributes();
		int n = namedNodeMap.getLength();
		for (int i = 0; i < n; i++) {
			Node node = namedNodeMap.item(i);
			String name = node.getNodeName();
			addAttribute(name, node.getNodeValue());
		}
		NodeList nodes = element.getChildNodes();
		n = nodes.getLength();
		for (int i = 0; i < n; i++) {
			Node node = nodes.item(i);
			int type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				io.github.tanguygab.logicsim3.Xml child = new io.github.tanguygab.logicsim3.Xml((Element) node);
				addChild(node.getNodeName(), child);
			}
		}
	}

	@Override
	public String toString() {
		String s = "[Xml: ";
		s += name != null ? name : "";
		s += "/" + content != null ? content : "";
		s += "]";
		return s;
	}

	public void addAttribute(String name, String value) {
		nameAttributes.put(name, value);
	}

	private void addChild(String name, io.github.tanguygab.logicsim3.Xml child) {
		ArrayList<io.github.tanguygab.logicsim3.Xml> children = nameChildren.get(name);
		if (children == null) {
			children = new ArrayList<io.github.tanguygab.logicsim3.Xml>();
			nameChildren.put(name, children);
		}
		children.add(child);
	}

	public String name() {
		return name;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String content() {
		return content;
	}

	public void addChild(io.github.tanguygab.logicsim3.Xml XML) {
		addChild(XML.name(), XML);
	}

	public void addChildren(io.github.tanguygab.logicsim3.Xml... XMLs) {
		for (io.github.tanguygab.logicsim3.Xml XML : XMLs)
			addChild(XML.name(), XML);
	}

	public io.github.tanguygab.logicsim3.Xml child(String name) {
		io.github.tanguygab.logicsim3.Xml child = optChild(name);
		if (child == null)
			throw new RuntimeException("Could not find child node: " + name);
		return child;
	}

	public io.github.tanguygab.logicsim3.Xml optChild(String name) {
		ArrayList<io.github.tanguygab.logicsim3.Xml> children = children(name);
		int n = children.size();
		if (n > 1)
			throw new RuntimeException("Could not find individual child node: " + name);
		return n == 0 ? null : children.get(0);
	}

	public boolean option(String name) {
		return optChild(name) != null;
	}

	public ArrayList<io.github.tanguygab.logicsim3.Xml> children(String name) {
		if (name == null) {
			ArrayList<io.github.tanguygab.logicsim3.Xml> all = new ArrayList<io.github.tanguygab.logicsim3.Xml>();
			// get all children
			for (ArrayList<io.github.tanguygab.logicsim3.Xml> children : nameChildren.values()) {
				all.addAll(children);
			}
			return all;
		}
		ArrayList<io.github.tanguygab.logicsim3.Xml> children = nameChildren.get(name);
		return children == null ? new ArrayList<io.github.tanguygab.logicsim3.Xml>() : children;
	}

	public String string(String name) {
		String value = optString(name);
		if (value == null) {
			throw new RuntimeException("Could not find attribute: " + name + ", in node: " + this.name);
		}
		return value;
	}

	public String optString(String name) {
		return nameAttributes.get(name);
	}

	public int integer(String name) {
		return Integer.parseInt(string(name));
	}

	public Integer optInteger(String name) {
		String string = optString(name);
		return string == null ? null : integer(name);
	}

	public double doubleValue(String name) {
		return Double.parseDouble(optString(name));
	}

	public Double optDouble(String name) {
		String string = optString(name);
		return string == null ? null : doubleValue(name);
	}

	private static Element rootElement(InputStream inputStream, String rootName) {
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(inputStream);
			Element rootElement = document.getDocumentElement();
			if (!rootElement.getNodeName().equals(rootName))
				throw new RuntimeException("Could not find root node: " + rootName);
			return rootElement;
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		} catch (ParserConfigurationException exception) {
			throw new RuntimeException(exception);
		} catch (SAXException exception) {
			throw new RuntimeException(exception);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception exception) {
					throw new RuntimeException(exception);
				}
			}
		}
	}

	private static FileInputStream fileInputStream(String filename) {
		try {
			return new FileInputStream(filename);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

}