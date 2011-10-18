package model;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;




public class GameOptionsLoader {
	private GameOptionsLoader() {
		
	}
	
	public static GameOptions load() throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathException {
		List<Achievement> achievements = new ArrayList<Achievement>();
		Set<PlayerColor> playerColors = new HashSet<PlayerColor>();
		
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("game.xml");
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(is);

		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "/game/achievements/achievement";
		NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
		
		Node node;
		for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			NamedNodeMap attrs = node.getAttributes();
			String name = attrs.getNamedItem("name").getNodeValue();
			int victory_points = Integer.parseInt(attrs.getNamedItem("victory_points").getNodeValue());
			char character = attrs.getNamedItem("character").getNodeValue().toCharArray()[0];
			String ID = attrs.getNamedItem("id").getNodeValue();
			
			int max_in_game = Integer.MAX_VALUE;
			if (attrs.getNamedItem("max_in_game") != null) {
				String max_in_game_str = attrs.getNamedItem("max_in_game").getNodeValue();
				max_in_game = (max_in_game_str.equals("unlimited")) ? Integer.MAX_VALUE : Integer.parseInt(max_in_game_str);
			}

			int max_per_player = Integer.MAX_VALUE;
			if (attrs.getNamedItem("max_per_player") != null) {
				String max_per_player_string = attrs.getNamedItem("max_per_player").getNodeValue();
				max_per_player = (max_per_player_string.equals("unlimited")) ? Integer.MAX_VALUE : Integer.parseInt(max_per_player_string);
			}
			
			achievements.add(new Achievement(name, victory_points, character, ID, max_in_game, max_per_player));
		}
		
		expression = "/game/colors/color";
		nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);		
			NamedNodeMap attrs = node.getAttributes();
			String name = attrs.getNamedItem("name").getNodeValue();
			String hex_value = attrs.getNamedItem("hex_value").getNodeValue();
			char character = attrs.getNamedItem("character").getNodeValue().toCharArray()[0];
			playerColors.add(new PlayerColor(name, Color.decode(hex_value), character));
		}
		return new GameOptions(achievements, playerColors);
	}
}
