package NextBlock;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JsonSimple {

	@SuppressWarnings("unchecked")
	public static void Write(ArrayList<PlayerInfo> pls, File f) {
		JSONObject main = new JSONObject();
		
		for (PlayerInfo pl : pls) {
			if (pl.nick == null || pl.loc == null)
				continue;
			JSONObject user = new JSONObject();
			user.put("x", pl.loc.getBlockX());
			user.put("y", pl.loc.getBlockY());
			user.put("z", pl.loc.getBlockZ());
			main.put(pl.nick, user);
		}
		
		try {
			FileWriter file = new FileWriter(f);
			file.write(main.toJSONString());
			file.flush();
			file.close();
		} catch (Exception e) {}
	}

	@SuppressWarnings("rawtypes")
	public static ArrayList<PlayerInfo> Read(File f)  {
		JSONObject main = null;
		JSONParser parser = new JSONParser();
		try {
			main = (JSONObject) parser.parse(new FileReader(f));
		} catch (Exception e) {}
		
		ArrayList <PlayerInfo> pls = new ArrayList <PlayerInfo>();
		if (main == null)
			return pls;
		for (Iterator names = main.keySet().iterator(); names.hasNext();) {
			String name = (String) names.next();
			JSONObject plJSON = (JSONObject) main.get(name);
			PlayerInfo pl = new PlayerInfo();
			pl.nick = name;
			pl.loc = new Location(NextBlock.wor, 
					((Number)plJSON.get("x")).intValue(),
					((Number)plJSON.get("y")).intValue(),
					((Number)plJSON.get("z")).intValue());
			pls.add(pl);
		}
		return pls;
	}
}
