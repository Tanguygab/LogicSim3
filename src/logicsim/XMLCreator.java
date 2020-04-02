package logicsim;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import logicsim.gates.MODIN;
import logicsim.gates.MODOUT;

/**
 * XML Creator for circuit and module files
 * 
 * taken from https://argonrain.wordpress.com/2009/10/27/000/
 * 
 * @author Peter Gabriel
 * @version 1.0
 */
public class XMLCreator {

	public static String createXML(LogicSimFile f) {
		DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder xmlBuilder;
		try {
			xmlBuilder = xmlFactory.newDocumentBuilder();
			Document doc = xmlBuilder.newDocument();
			String rootName = "logicsim";

			if (f.circuit.isModule()) {
				rootName = "logicsim";
			}
			Element mainRootElement = doc.createElement(rootName);
			mainRootElement.setAttribute("version", XMLLoader.formatVersion);
			doc.appendChild(mainRootElement);

			if (f.info != null) {
				Element node = doc.createElement("info");
				for (Iterator<String> iterator = f.info.keySet().iterator(); iterator.hasNext();) {
					String key = iterator.next();
					String value = f.info.get(key);
					Element n = doc.createElement(key);
					n.setTextContent(value);
					node.appendChild(n);
				}
				mainRootElement.appendChild(node);
			}

			if (f.getGates() != null) {
				Element node = doc.createElement("gates");
				for (Gate g : f.getGates()) {
					Node gnode = createGateNode(doc, g);
					node.appendChild(gnode);
				}
				mainRootElement.appendChild(node);
			}

			if (f.getWires() != null) {
				Element node = doc.createElement("wires");
				for (Wire w : f.getWires()) {
					Node wnode = createWireNode(doc, w);
					node.appendChild(wnode);
				}
				mainRootElement.appendChild(node);
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
			DOMSource source = new DOMSource(doc);

			if (f.fileName == null) {
				StringWriter writer = new StringWriter();
				transformer.transform(source, new StreamResult(writer));
				String xmlString = writer.getBuffer().toString();
				return xmlString;
			} else {
				FileOutputStream outStream = new FileOutputStream(new File(f.fileName));
				transformer.transform(source, new StreamResult(outStream));
				f.changed = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Node createGateNode(Document doc, Gate g) {
		Element node = doc.createElement("gate");
		if (g instanceof Module) {
			node.setAttribute("type", ((Module) g).type);
			node.setAttribute("module", "true");
		} else {
			node.setAttribute("type", g.type);
		}
		node.setAttribute("x", String.valueOf(g.getX()));
		node.setAttribute("y", String.valueOf(g.getY()));

		if (g.supportsVariableInputs() && g.getNumInputs() != 2)
			node.setAttribute("inputs", String.valueOf(g.getNumInputs()));

		// settings
		Node snode = createSettingsNode(doc, g);
		if (snode != null)
			node.appendChild(snode);

		for (Connector c : g.getInputs()) {
			if ((g instanceof MODIN && c.label != null)
					|| (c.ioType == Connector.INPUT && c.levelType != Connector.NORMAL)) {
				node.appendChild(createInputNode(doc, c));
			}
		}
		for (Connector c : g.getOutputs()) {
			if ((g instanceof MODOUT && c.label != null)
					|| (c.ioType == Connector.INPUT && c.levelType != Connector.NORMAL)) {
				node.appendChild(createOutputNode(doc, c));
			}
		}
		return node;
	}

	private static Node createSettingsNode(Document doc, Gate g) {
		if (g.getProperties().size() > 0) {
			Element node = doc.createElement("properties");
			Properties properties = g.getProperties();
			for (Object key : properties.keySet()) {
				String keyS = (String) key;
				Element n = doc.createElement("property");
				n.setAttribute("key", keyS);
				n.setTextContent(properties.getProperty(keyS));
				node.appendChild(n);
			}
			return node;
		}
		return null;
	}

	private static Node createOutputNode(Document doc, Connector output) {
		Element node = doc.createElement("io");
		node.setAttribute("iotype", "output");
		node.setAttribute("number", String.valueOf(output.number));

		if (output.label != null)
			node.setAttribute("label", output.label);
		return node;
	}

	private static Node createInputNode(Document doc, Connector input) {
		Element node = doc.createElement("io");
		node.setAttribute("iotype", "input");
		node.setAttribute("number", String.valueOf(input.number));
		int inputType = input.levelType;
		if (inputType != Connector.NORMAL) {
			String inpType = "";
			if (inputType == Connector.HIGH)
				inpType = "high";
			else if (inputType == Connector.LOW)
				inpType = "low";
			else if (inputType == Connector.INVERTED)
				inpType = "inv";
			node.setAttribute("type", inpType);
		}
		if (input.label != null) {
			node.setAttribute("label", input.label);
		}
		return node;
	}

	private static Node createWireNode(Document doc, Wire w) {
		if (w != null) {
			Element n = doc.createElement("wire");
			if (w.fromConn != null) {
				Element g = doc.createElement("from");
				g.setAttribute("id", w.fromConn.gate.getId());
				g.setAttribute("number", String.valueOf(w.fromConn.number));
				n.appendChild(g);
			}

			if (w.toConn != null) {
				Element g = doc.createElement("to");
				g.setAttribute("id", w.toConn.gate.getId());
				g.setAttribute("number", String.valueOf(w.toConn.number));
				n.appendChild(g);
			}

			for (int i = 0; i < w.points.size(); i++) {
				WirePoint wp = w.points.get(i);
				Element point = doc.createElement("point");
				point.setAttribute("number", String.valueOf(i));
				point.setAttribute("x", String.valueOf(wp.getX()));
				point.setAttribute("y", String.valueOf(wp.getY()));
				point.setAttribute("node", String.valueOf(wp.active));
				n.appendChild(point);
			}
			return n;
		}
		return null;
	}
}
