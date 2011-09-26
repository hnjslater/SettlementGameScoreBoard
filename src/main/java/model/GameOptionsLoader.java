package model;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;


public class GameOptionsLoader {
	private GameOptionsLoader() {
		
	}
	
	public static GameOptions load() throws ParserConfigurationException, SAXException, IOException, TransformerException {
		Set<Achievement> achievements = new HashSet<Achievement>();
		Set<PlayerColor> playerColors = new HashSet<PlayerColor>();
		

		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("game.xml");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse(is);
		dom.getDocumentElement().normalize();
		

		NodeIterator nl = XPathAPI.selectNodeIterator(dom.getDocumentElement(), "/game/achievements/achievement");
		Node node;
		while ((node = nl.nextNode()) != null) {
			NamedNodeMap attrs = node.getAttributes();
			String name = attrs.getNamedItem("name").getNodeValue();
			int victory_points = Integer.parseInt(attrs.getNamedItem("victory_points").getNodeValue());
			String short_name = attrs.getNamedItem("short_name").getNodeValue();
			char character = attrs.getNamedItem("character").getNodeValue().toCharArray()[0];
			achievements.add(new Achievement(name, victory_points, short_name, character));
		}
		
		nl = XPathAPI.selectNodeIterator(dom.getDocumentElement(), "/game/colors/color");
		while ((node = nl.nextNode()) != null) {
			NamedNodeMap attrs = node.getAttributes();
			String name = attrs.getNamedItem("name").getNodeValue();
			String hex_value = attrs.getNamedItem("hex_value").getNodeValue();
			char character = attrs.getNamedItem("character").getNodeValue().toCharArray()[0];
			playerColors.add(new PlayerColor(name, Color.decode(hex_value), character));
		}
		
		return new GameOptions(achievements, playerColors);
	}
}
