package NextBlock;

import org.bukkit.Location;

public class PlayerInfo {
	public PlayerInfo(String nick, Location loc) {
		this.nick = nick;
		this.loc = loc;
	}
	
	public PlayerInfo() {}
	
	public String nick;
	public Location loc;
}
