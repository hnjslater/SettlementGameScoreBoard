package webservice;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import model.Achievement;
import model.Game;
import model.Player;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class XMLSerialiser {
	
	void generateXML(OutputStream response, Game game) throws SAXException, TransformerConfigurationException {
		
		TransformerHandler hd = createTransformHandler(response);
		hd.startDocument();
		AttributesImpl atts = new AttributesImpl();
		if (game.getWinner() != null) {
			atts.addAttribute("", "", "winner", "CDATA", game.getWinner().getPlayerColor().toString());
		}
		hd.startElement("","","game",atts);
		generateXML(hd, game.getWinningVP(), game.getAchievements());
		atts.clear();
		generateXML(hd, game.getLeaderBoard());
		hd.endElement("","","game");
		hd.endDocument();	
	}
	void generateXML(OutputStream response, List<Player> players) throws SAXException, TransformerConfigurationException {
		TransformerHandler hd = createTransformHandler(response);
		hd.startDocument();
		generateXML(hd, players);
		hd.endDocument();
	}
	
	void generateXML(TransformerHandler hd, List<Player> players)
	throws SAXException {

		AttributesImpl atts = new AttributesImpl();
		hd.startElement("", "", "players", atts);
		for (Player p : players)
		{
			generateXML(hd, p);
		}
		hd.endElement("", "", "players");
	}

	void generateXML(OutputStream response, Player p) throws SAXException, TransformerConfigurationException {
		TransformerHandler hd = createTransformHandler(response);
		hd.startDocument();
		generateXML(hd, p);
		hd.endDocument();
	}
	private void generateXML(TransformerHandler hd, Player p)
	throws SAXException {
		AttributesImpl atts = new AttributesImpl();

		atts.clear();
		atts.addAttribute("","","name","CDATA",p.getName());
		atts.addAttribute("","","color","CDATA",p.getPlayerColor().toString());
		atts.addAttribute("", "", "settlement_victory_points", "CDATA", Integer.toString(p.getSettlementVP()));
		hd.startElement("","","player",atts);

		atts.clear();
		if (p.getAchievements().size() > 0) {
			generateXML(hd, p.getAchievements());
		}
		
		hd.endElement("","","player");
	}

	void generateXML(OutputStream response, Collection<Achievement> achievements) throws SAXException, TransformerConfigurationException {
		TransformerHandler hd = createTransformHandler(response);
		hd.startDocument();
		generateXML(hd, achievements);
		hd.endDocument();
	}
	private void generateXML(TransformerHandler hd, Collection<Achievement> achievements)
			throws SAXException {
		AttributesImpl atts = new AttributesImpl();
			hd.startElement("", "", "achievements", atts);

			for (Achievement a : achievements) {
				atts.clear();
				atts.addAttribute("", "", "link", "CDATA", "#" + a.getID());


				hd.startElement("", "", "achievement", atts);
				hd.endElement("", "", "achievement");
			}
			hd.endElement("", "", "achievements");
		
	}
	
	void generateXML(OutputStream response, int winningVP, Collection<Achievement> achievements) throws SAXException, TransformerConfigurationException {
		TransformerHandler hd = createTransformHandler(response);
		hd.startDocument();
		generateXML(hd, winningVP, achievements);
		hd.endDocument();
	}
	private void generateXML(TransformerHandler hd, int winningVP, Collection<Achievement> achievements)
	throws SAXException {
		AttributesImpl atts = new AttributesImpl();
		atts.addAttribute("","","winning_victory_points","CDATA",Integer.toString(winningVP));
		hd.startElement("","","rules", atts);
		for (Achievement a: achievements) {
			atts.clear();
			atts.addAttribute("", "", "name", "CDATA", a.getName());
			atts.addAttribute("", "", "victory_points", "CDATA", Integer.toString(a.getVictoryPoints()));
			atts.addAttribute("", "", "short_name", "CDATA", a.getShortName());
			atts.addAttribute("", "", "id", "CDATA", a.getID());
			hd.startElement("", "", "achievement", atts);
			hd.endElement("", "", "achievement");
		}
		hd.endElement("", "", "rules");
	}

	private TransformerHandler createTransformHandler(OutputStream response)
	throws TransformerFactoryConfigurationError,
	TransformerConfigurationException, SAXException {
		PrintWriter out = new PrintWriter(response);
		StreamResult streamResult = new StreamResult(out);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		// SAX2.0 ContentHandler.
		TransformerHandler hd = tf.newTransformerHandler();
		Transformer serializer = hd.getTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
		serializer.setOutputProperty(OutputKeys.INDENT,"yes");
		hd.setResult(streamResult);
		hd.startDocument();
		return hd;
	}
}
