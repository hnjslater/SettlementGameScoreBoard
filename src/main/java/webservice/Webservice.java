package webservice;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import model.PlayerColor;
import model.RulesBrokenException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
public class Webservice  {

	private model.Game game;
	private	HttpServer server = null;
	private int port;

	public Webservice(Game game) {
		this.game = game;
	}

	public void handleRoot(HttpExchange t) throws IOException {
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		OutputStream os = t.getResponseBody();       

		try {

			TransformerHandler hd = createTransformHandler(response);
			AttributesImpl atts = new AttributesImpl();
			hd.startElement("","","game",atts);
			generateRulesXML(hd);
			atts.clear();
			generatePlayersXML(hd);
			hd.endElement("","","game");
			hd.endDocument();

			t.sendResponseHeaders(200, response.size());
			response.writeTo(os); 
			os.close();        
		}
		catch (Exception e) {

			sendErrorMessage(500, t, e.getMessage());    
		}
	}

	private void generateRulesXML(TransformerHandler hd)
			throws SAXException {
		AttributesImpl atts = new AttributesImpl();
		atts.addAttribute("","","winning_victory_points","CDATA",Integer.toString(game.getWinningVP()));
		hd.startElement("","","rules", atts);
		for (Achievement a: this.game.getAchievements()) {
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

	private void generatePlayersXML(TransformerHandler hd)
	throws SAXException {

		AttributesImpl atts = new AttributesImpl();
		hd.startElement("", "", "players", atts);
		for (Player p : game.getLeaderBoard())
		{
			generatePlayerXML(hd, p);
		}
		hd.endElement("", "", "players");
	}

	public void handlePlayers(HttpExchange t) throws IOException {
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		OutputStream os = t.getResponseBody();     

		String method = t.getRequestMethod().toLowerCase();

		try {
			TransformerHandler hd = createTransformHandler(response);

			// /players
			if (t.getRequestURI().getPath().equals("/players") || t.getRequestURI().getPath().equals("/players/")) {
				generatePlayersXML(hd);
			}
			// /players/COLOR
			else if (t.getRequestURI().getPath().matches("^/players/[^/]*/{0,1}$")){

				PlayerColor playercolor = game.getPlayerColor(t.getRequestURI().getPath().split("/")[2]);
				Player player = game.getPlayer(playercolor);

				if (method.equals("post") || method.equals("put")) {
					updatePlayer(t, player);		    
				}
				else if (method.equals("get")) {
					Player p = game.getPlayer(playercolor);
					generatePlayerXML(hd, p);
				}
				else {
					sendErrorMessage(405, t, "Bad Method");
					return;
				}
			}

			// /players/COLOR/achievements
			else if (t.getRequestURI().getPath().matches("^/players/[^/]*/achievements/{0,1}$")) {

				PlayerColor playercolor = game.getPlayerColor(t.getRequestURI().getPath().split("/")[2]);
				Player player = game.getPlayer(playercolor);

				generatePlayerAchievementsXML(hd, player);

			}

			// /players/COLOR/achievements/ACHIEVEMENT
			else if (t.getRequestURI().getPath().matches("^/players/[^/]*/achievements/[^/]+{0,1}$")) {

				String[] path = t.getRequestURI().getPath().split("/");
				PlayerColor playercolor = game.getPlayerColor(path[2]);
				Achievement achievement = Achievement.valueOf(game.getAchievements(), path[4]);
				if (achievement == null)
					throw new RuntimeException("Achievement '" + path[4] + "' not recognized");

				if (method.equals("put") || method.equals("post"))
					game.getPlayer(playercolor).add(achievement);
				else if (method.equals("delete"))
					game.getPlayer(playercolor).remove(achievement);
				else {
					sendErrorMessage(405, t, "Bad Method");
					return;
				}
			}
			else {
				sendErrorMessage(404, t, "Resource Not Found");
				return;
			}
			hd.endDocument();

			if (response.size() == 0)
				new PrintWriter(response).write("OK");
			t.sendResponseHeaders(200, response.size());
			response.writeTo(os); 
			os.close();
		}
		catch (Exception e) {
			//e.printStackTrace(new PrintStream(response));

			sendErrorMessage(500, t, e.getMessage());    
		}

	}

	private void updatePlayer(HttpExchange t, Player player) throws ParserConfigurationException, SAXException,
	IOException, RulesBrokenException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse(t.getRequestBody());

		if (dom.getDocumentElement().hasAttribute("name"))
			player.setName(dom.getDocumentElement().getAttribute("name"));

		if (dom.getDocumentElement().hasAttribute("settlement_victory_points"))
			player.setVP(Integer.parseInt(dom.getDocumentElement().getAttribute("settlement_victory_points")));

		List<Achievement> incoming_achievements = new ArrayList<Achievement>();
		NodeList player_children = dom.getDocumentElement().getChildNodes();
		if (player_children.getLength() > 0) {
			NodeList achievements = player_children.item(0).getChildNodes();

			for (int i = 0; i < achievements.getLength(); i++) {
				Node achievement = achievements.item(i);
				incoming_achievements.add(Achievement.valueOf(game.getAchievements(), achievement.getAttributes().getNamedItem("name").getNodeValue()));

			}

			for (Achievement a : player.getAchievements()) {
				if (!incoming_achievements.contains(a))
					player.remove(a);
			}

			for (Achievement a : incoming_achievements) {
				player.add(a);
			}
		}
	}

	private void sendErrorMessage(int code, HttpExchange t, String message) {
		try {
		ByteArrayOutputStream response = new ByteArrayOutputStream();
		PrintWriter p = new PrintWriter(response);
		p.write(message);
		p.close();
		t.sendResponseHeaders(code, response.size());

		OutputStream os = t.getResponseBody(); 
		response.writeTo(os);
		os.close();
		}
		catch (Exception ex) {
			t.close();
		
		}
	}

	private void generatePlayerXML(TransformerHandler hd, Player p)
	throws SAXException {
		AttributesImpl atts = new AttributesImpl();

		atts.clear();
		atts.addAttribute("","","name","CDATA",p.getName());
		atts.addAttribute("","","color","CDATA",p.getPlayerColor().toString());
		atts.addAttribute("", "", "settlement_victory_points", "CDATA", Integer.toString(p.getSettlementVP()));
		hd.startElement("","","player",atts);

		atts.clear();
		generatePlayerAchievementsXML(hd, p);
		hd.endElement("","","player");
	}

	private void generatePlayerAchievementsXML(TransformerHandler hd, Player p) throws SAXException {
		if (p.getAchievements().size() > 0) {
			AttributesImpl atts = new AttributesImpl();
			hd.startElement("", "", "achievements", atts);

			for (Achievement a : p.getAchievements()) {
				atts.clear();
				atts.addAttribute("", "", "link", "CDATA", "#" + a.getID());


				hd.startElement("", "", "achievement", atts);
				hd.endElement("", "", "achievement");
			}
			hd.endElement("", "", "achievements");
		}
	}

	private TransformerHandler createTransformHandler(
			ByteArrayOutputStream response)
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

	public void start() throws IOException {
		if (server == null) {
			int[] ports = {80, 8000, 8001, 8002, 8003, 12000};
			String errorMessage = "Could not bind to port";
			for (int i : ports) {
				try {
					port = i;
					server = HttpServer.create(new InetSocketAddress(i), 0);
					break;
				}
				catch (Exception e) {
					errorMessage = e.getMessage();
				}
			}
			if (server == null) {
				throw new RuntimeErrorException(null, errorMessage);
			}
			server.createContext("/", new HttpHandler() {
				@Override
				public void handle(HttpExchange arg0) throws IOException {
					handleRoot(arg0);		
				}	       
			});
			server.createContext("/players", new HttpHandler() {
				@Override
				public void handle(HttpExchange arg0) throws IOException {
					handlePlayers(arg0);		
				}	       
			});
			server.createContext("/gui", new HttpHandler() {
				@Override
				public void handle(HttpExchange arg0) throws IOException {
					handleGui(arg0);		
				}	       
			});

			server.setExecutor(null); // creates a default executor
			server.start();
		}
	}
	
	public void stop() {
		server.stop(1);
		server = null;
	}

	protected void handleGui(HttpExchange t) {
		// TODO Auto-generated method stub
		try {

			ByteArrayOutputStream response = new ByteArrayOutputStream();
			PrintWriter p = new PrintWriter(response);
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("gui.html");
			if (is == null)
				throw new RuntimeException("Couldn't find resource");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			
            while ((line = br.readLine()) != null) {
                //Process the data, here we just print it out
                p.write(line + "\n");
            }

			p.close();
			
			t.sendResponseHeaders(200, response.size());

			OutputStream os = t.getResponseBody(); 
			response.writeTo(os);
			os.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.fillInStackTrace();
			sendErrorMessage(500, t, e.getMessage());
		}
	}
	
	public List<String> getInterfaces() throws SocketException {
		List<String> interfaceIPv4 = new ArrayList<String>();
		List<String> interfaceIPv6 = new ArrayList<String>();
		if (server != null) {
			Enumeration<NetworkInterface> interfaces =
				NetworkInterface.getNetworkInterfaces();				 
				while (interfaces.hasMoreElements())
				{
					NetworkInterface nif = interfaces.nextElement();
					if (!nif.isLoopback() && nif.isUp()) {
						Enumeration<InetAddress> addrs = nif.getInetAddresses();
						while (addrs.hasMoreElements()) {
							InetAddress ia = addrs.nextElement();
							// Can't work out how to check the IP version.
							if (ia.getAddress().length == 4)
								interfaceIPv4.add("http://" + ia.getHostAddress() + ":" + Integer.toString(this.port) + "/gui");
							else
								interfaceIPv6.add("http://[" + ia.getHostAddress() + "]:" + Integer.toString(this.port) + "/gui");
						}
					}

				}
		}
		interfaceIPv4.addAll(interfaceIPv6);
		return interfaceIPv4;
	}
}

