package NextBlock;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;

public class NextBlock extends JavaPlugin{
	//private boolean HolographicDisplays = false;
	private JavaPlugin plugin = this;
	public static World wor, wor_ref;
	private int worMin = 0;
	private int worMax = 128;
	private int worRadius = 1000;
	FileConfiguration config;
	Random rnd = new Random(System.currentTimeMillis());
	String noperm = String.format("%sYou don't have permission [NextBlock.set].", ChatColor.RED);
	String noplace = String.format("%sIt is impossible to place a block on an ungenerated territory!", ChatColor.RED);
	String nogener = String.format("%sWait for the end of the previous generation!", ChatColor.DARK_AQUA);
	ChunkGenerator GenNow = new VoidChunkGenerator();
	private ArrayList<String> generators = new ArrayList<>();
	private CustomItems genItems = new CustomItems();
	private ArrayList<PlayerInfo> players = new ArrayList<>();
	Long genSpeed = 80L, genExSpeed = 10L;
	boolean base = true;
	
	public class TaskBase implements Runnable {
        public void run() {
        	if (!base)
        		return;
        	for (Player p : wor.getPlayers()) {
				PlayerInventory pInv = p.getInventory();
				if(!pInv.containsAtLeast(genItems.gen1x1, 1))
					pInv.addItem(genItems.gen1x1);
				for (ItemStack sr : pInv) {
					if (sr == null)
						continue;
					ItemStack sr1 = sr.clone();
					sr1.setAmount(1);
					if (sr1.equals(genItems.gen1x1))
						sr.setAmount(1);
				}
			}
        }
	}
	
	public class wor_null implements Runnable {
        public void run() {
            if (wor == null || wor_ref == null) {
                Bukkit.getConsoleSender().sendMessage(String.format("\n%s\n%s",
                		"[NB] WORLD INITIALIZATION ERROR! world = null",
                		"[NB] Trying to initialize the world again..."));
                wor = Bukkit.getWorld(config.getString("world"));
                wor_ref = Bukkit.getWorld(config.getString("world_reference"));
            } else {
                Bukkit.getConsoleSender().sendMessage("[NB] The initialization of the world was successful!");
                wor_ok();
            }
        }
    }
	
	private void wor_ok() {
        Bukkit.getScheduler().cancelTasks(plugin);
        world = BukkitAdapter.adapt(wor);
   	 	world_reference = BukkitAdapter.adapt(wor_ref);
        UpdateMinMax();
        Bukkit.getScheduler().runTaskTimer(this, new TaskBase(), 7, 14);
    }
    
    private void UpdateMinMax() {
    	worMax = wor.getMaxHeight() - 64;
        worMin = wor.getMinHeight();
        if (worMax == 192)
        	worMax-=64;
        worRadius = (int) wor.getWorldBorder().getSize() / 2;
    }
	
	public class TBCR implements Runnable{
    	private Location loc;
    	private Player p;
    	
    	public TBCR(Location loc_, Player p_) {
    		loc = loc_;
    		p = p_;
    	}
    	
    	@Override
    	public void run() {
    		TeleportBlockCheck(loc, p);
			generators.remove(p.getName());
    	}
    }
	
	@Override 
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {return GenNow;}
	
	public class NextBlockEvents implements Listener {
		@EventHandler
		public void onBlockPlace(BlockPlaceEvent e) {
			Block bl = e.getBlock();
			if (bl.getWorld() != wor)
				return;
			if (e.getPlayer().hasPermission("NextBlock.place"))
				return;
			if (wor.getBlockAt(bl.getX(), worMin, bl.getZ()).getType() == Material.BEDROCK)
				return;
			e.getPlayer().sendMessage(noplace);
			e.setCancelled(true);
		}

		@EventHandler
		public void Resp(PlayerRespawnEvent e) {
			if (e.getPlayer().getWorld().equals(wor)) {
				PlayerInfo pl = getPlayerInfo(e.getPlayer().getName());
				if (pl != null && pl.loc != null) {
					Location loc = pl.loc;
					loc.setWorld(wor);
					e.setRespawnLocation(loc);
				}
			}
		}
		
		@EventHandler
	    public void onPlayerDropItem(PlayerDropItemEvent e) {
	        if (e.isCancelled())
	            return;
	        ItemStack itst = e.getItemDrop().getItemStack().clone();
	        itst.setAmount(1);
	        if (itst.equals(genItems.gen1x1))
	            e.setCancelled(true);
	    }

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent e) {
			Block bl = e.getClickedBlock();
			if (bl == null) return;
			ItemStack iStack = e.getItem();
			if (iStack == null) return;
			ItemStack check = iStack.clone();
			check.setAmount(1);
			if (bl.getWorld().equals(wor) && genItems.Gens.contains(check)) {
				e.setCancelled(true);
				if (wor == null) return;
				Player pl = e.getPlayer();
				String name = pl.getName();
				if (generators.contains(name)) {
					pl.sendMessage(nogener);
					return;
				}
				Location loc = bl.getLocation().add(e.getBlockFace().getDirection());
				
				if (wor.getBlockAt(loc.getBlockX(), worMin, loc.getBlockZ()).getType() == Material.BEDROCK) return;
				//
				// 1 слойные генераторы
				//
				int r = 0;
				Long delay = genSpeed;
				
				if (check.equals(genItems.gen1x1))  iStack.setAmount(iStack.getAmount() + 1);
				else if (check.equals(genItems.gen3x3)) {
					r = 1;
				} else if (check.equals(genItems.gen5x5)) {
					r = 2;
				} else if (check.equals(genItems.gen15x15)) {
					r = 7;
				} else if (check.equals(genItems.exGen3x3)) {
					delay = genExSpeed;
					r = 1;
				} else if (check.equals(genItems.exGen5x5)) {
					delay = genExSpeed;
					r = 2;
				} else if (check.equals(genItems.exGen9x9)) {
					delay = genExSpeed;
					r = 4;
				} else if (check.equals(genItems.exGen15x15)) {
					delay = genExSpeed;
					r = 7;
				}
				Long regenTime = (2*r+1) * (2*r+1) * delay;
				
				iStack.setAmount(iStack.getAmount() - 1);
				Regen(loc, r, delay);
				addRemGen(name, regenTime, GenPercent(loc.clone(), regenTime, 30L, check));
			}
		}
		
		@EventHandler
	    public void onPlayerClickInventory(final InventoryClickEvent e){
			ItemStack iStack = e.getCurrentItem();
			if (iStack == null) return;
			ItemStack check = iStack.clone();
			check.setAmount(1);
			if (genItems.gen1x1.equals(check)) 
				e.setCancelled(true);
	    }
	}
	
	private Location GetSpawnLocation(int around) {
		return new Location(wor, rnd.nextInt(around*2) - around, worMin, rnd.nextInt(around*2) - around);
	}
	
	private void Regen(Location loc, int radius, Long tick) {
		int x = loc.getBlockX()-radius, z = loc.getBlockZ()-radius;
		int count = radius*2+1;
   	 	
   	 	Long add = 0L;
   	 	
   	 	for (int i = 0;i<count;i++) 
   	 		for (int u = 0;u<count;u++)
   	 			if (wor.getBlockAt(x+i, worMin, z+u).getType() != Material.BEDROCK) {
   	 				CuboidRegion reg = new CuboidRegion(
   	 					BlockVector3.at(x+i, worMin, z+u),
   	 					BlockVector3.at(x+i, worMax, z+u));
   	 				
	   				Bukkit.getScheduler().runTaskLater(plugin, new RunnableRegen(reg), add += tick);
   	 			}
	}
	
	private com.sk89q.worldedit.world.World world, world_reference;
	
	public class RunnableRegen implements Runnable{
		private CuboidRegion copy;
		public RunnableRegen(CuboidRegion copy_) { copy = copy_; }
		@Override
		public void run() { Regen(copy); }
	}
	
	private void Regen(CuboidRegion reg) {
		BlockArrayClipboard clipboard = new BlockArrayClipboard(reg);
	 	ForwardExtentCopy opr1 = new ForwardExtentCopy(world_reference, reg, clipboard, reg.getMinimumPoint());
	 			
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
   	 		Operations.complete(opr1);
   	 		
	   	 	Operation opr2 = new ClipboardHolder(clipboard)
	   	            .createPaste(editSession)
	   	            .to(reg.getMinimumPoint())
	   	            .copyEntities(false)
	   	            .ignoreAirBlocks(true)
	   	            .build();
	   	 	Operations.complete(opr2);
	 	} catch (Exception e) {}
	}
	
	@Override
    public void onEnable() {
		Configfile();
		genItems.ReLoadHead();;
		//HolographicDisplays = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
		if (!config.isString("world"))
			return;
        if (wor == null)
        	Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new wor_null(), 64, 128);
        else
        	wor_ok();
        //Events
		Bukkit.getPluginManager().registerEvents(new NextBlockEvents(), plugin);
		//PlData JSON Load
		File PlData = new File(getDataFolder(), "PlData.json");
		if (PlData.exists())
			players = JsonSimple.Read(PlData);
	}
	
    private void TeleportBlockCheck(Location loc, Player p, Long ticks) {
    	Bukkit.getScheduler().runTaskLater(plugin, new TBCR(loc ,p), ticks);
    }
    
    private void TeleportBlockCheck(Location loc, Player p) {
    	loc.setWorld(wor);
    	loc.setY(worMin);
    	if (wor.getBlockAt(loc).getType() != Material.BEDROCK) {
    		Bukkit.getScheduler().runTaskLater(plugin, new TBCR(loc ,p), 100L);
    		return;
    	}
    	loc.setY(worMax);
    	for(;wor.getBlockAt(loc).getType() == Material.AIR ;loc.add(0, -3, 0));
    	loc.add(0, 3, 0);
    	p.teleport(loc);
    }
    
    private void addRemGen(String name, Long time, Hologram holo) {
    	generators.add(name);
    	Bukkit.getScheduler().runTaskLater(plugin, 
    			() -> { 
    				generators.remove(name); 
    				if (!holo.isDeleted()) holo.delete();
    			}, time);
    }
    
    private Hologram GenPercent(Location where, Long ticks, Long tick, ItemStack iStack) {
    	where.add(0.5, 3.2, 0.5);
		Hologram holo = HologramsAPI.createHologram(plugin, where);
    	holo.appendTextLine(String.format("%sGeneration...", ChatColor.BLUE));
    	holo.appendTextLine("0%");
    	holo.appendItemLine(iStack);
    	GenPercent(holo, 1L, ticks, tick);
    	return holo;
    }
    
    private void GenPercent(Hologram holo, Long now, Long ticks, Long tick) {
    	if (ticks <= now) {
    		if (!holo.isDeleted()) holo.delete();
    		return;
    	}
    	if (holo.isDeleted()) return;
    	holo.removeLine(1);
    	holo.insertTextLine(1, String.format("%d%%",(int)(100f/(ticks.floatValue()/now.floatValue()))));
	    Bukkit.getScheduler().runTaskLater(plugin, () -> { 
	    	GenPercent(holo, (now+tick), ticks, tick);
	    }, tick);
    }
    
    private void GenPercent(CommandSender sender, Long ticks, Long tick) {
    	sender.sendMessage(String.format("%sGeneration...", ChatColor.BLUE));
    	GenPercent(sender, tick, ticks, tick);
    }
    
    private void GenPercent(CommandSender sender, Long now, Long ticks, Long tick) {
    	if (ticks <= now) {
    		sender.sendMessage("100%...");
    		return;
    	}
    	sender.sendMessage(String.format("%d%%",(int)(100f/(ticks.floatValue()/now.floatValue()))));
	    Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> { GenPercent(sender, now + tick, ticks, tick); }, tick);
    }
    
    private PlayerInfo getPlayerInfo(String name) {
    	for (PlayerInfo pl : players)
    		if (pl.nick.contains(name))
    			return pl;
		return null;
    }
    
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("nextblock")) {
			//
			if (args.length == 0)
				return ((Player) sender).performCommand("anb j");

			if (!sender.hasPermission("NextBlock.join")) {
				sender.sendMessage(String.format("%sYou don't have permission [NextBlock.join].", ChatColor.RED));
				return true;
			}
			//
			switch (args[0].toLowerCase()) {
				case ("j"):
				case ("join"): {
					Player p = (Player) sender;
					String name = p.getName();
					if (generators.contains(name))
						return true;
					if (wor == null)
						return true;
					PlayerInfo pl = getPlayerInfo(name);
					if (pl == null) {
						Long tick = genExSpeed;
						int r = 3;
						Long ticks = (2*r+1) * (2*r+1) * tick;
						Location loc = GetSpawnLocation(worRadius-64);
						Regen(loc, r, tick);
						GenPercent(sender, ticks, tick*4);
						TeleportBlockCheck(loc, p, ticks);
						players.add(new PlayerInfo(name, loc));
						return true;
					}
					TeleportBlockCheck(pl.loc, p);
					return true;
				}
				case ("create"):{
					if (!sender.hasPermission("NextBlock.set")) {
	                    sender.sendMessage(noperm);
	                    return true;
	                }
					Player p = (Player) sender;
					config.set("world", "NextBlock");
					p.performCommand("mv create NextBlock_Reference NORMAL");
					p.performCommand("mv create NextBlock NORMAL -g NextBlock");
					sender.sendMessage("Now let's generate the Next Block_Reference world!");
					worRadius = 1000;
					if (args.length >= 2) {
	                    try { worRadius = Integer.parseInt(args[1]);
	                    } catch (Exception e) {}}
					if (worRadius < 300)
						worRadius = 300;
					p.performCommand(String.format("fcp start %d NextBlock_Reference", worRadius * 4 + 64));
					Bukkit.getWorld("NextBlock").getWorldBorder().setSize(worRadius * 2);
					return true;
				}
				case ("reload"):{
					if (!sender.hasPermission("NextBlock.set")) {
	                    sender.sendMessage(noperm);
	                    return true;
	                }
					sender.sendMessage("Reloading the configuration!");
					Configfile();
					return true;
				}
				case ("base"):{
					if (!sender.hasPermission("NextBlock.set")) {
	                    sender.sendMessage(noperm);
	                    return true;
	                }
					base = !base;
					sender.sendMessage("base now: " + base);
					config.set("base", base);
					return true;
				}
				case ("give"):{
					if (!sender.hasPermission("NextBlock.set")) {
	                    sender.sendMessage(noperm);
	                    return true;
	                }
					Player p;
					PlayerInventory inv;
					if (args.length == 1) {
						p = (Player) sender;
						inv = p.getInventory();
						for (ItemStack gen : genItems.Gens) 
							inv.addItem(gen);
	                    return true;
	                }
					int i = 1;
					if (args.length >= 3) {
						p = Bukkit.getPlayer(args[1]);
						if (p == null) {
							sender.sendMessage("The player has not been found!!!");
							return true;
						}
						inv = p.getInventory();
						i++;
					} else {
						p = (Player) sender;
						inv = p.getInventory();
					}
					
					ItemStack item = null;
					if (args[i].equals("gen1x1"))
						item = genItems.gen1x1;
					if (args[i].equals("gen3x3"))
						item = genItems.gen3x3;
					if (args[i].equals("gen5x5"))
						item = genItems.gen5x5;
					if (args[i].equals("gen15x15"))
						item = genItems.gen15x15;
					if (args[i].equals("exGen3x3"))
						item = genItems.exGen3x3;
					if (args[i].equals("exGen5x5"))
						item = genItems.exGen5x5;
					if (args[i].equals("exGen9x9"))
						item = genItems.exGen9x9;
					if (args[i].equals("exGen15x15"))
						item = genItems.exGen15x15;
					if (item == null)
						return true;
					
					int temp = 1;
					if (args.length >= 4)
	                    try { temp = Integer.parseInt(args[3]);
	                    } catch (Exception e) {}
					while(0 < temp--)
						inv.addItem(item);
					return true;
				}
				default://ver
		            sender.sendMessage(String.format("%s%s\n%s\n%s\n%s",
		            	ChatColor.values()[rnd.nextInt(ChatColor.values().length)],
		            	"▄   ▄ ▄▄",
		            	"█▀▄█ █▄▀",
		            	"█   █ █▄▀",
		            	"Create by MrMarL\nPlugin version: v0.0.7"));
		            return true;
			}
		}
		return true;
	}
	
	public void onDisable() {
		File con = new File(getDataFolder(), "config.yml");
		try {config.save(con);
		} catch (Exception e) {}
		//PlData JSON Save
		File PlData = new File(getDataFolder(), "PlData.json");
		JsonSimple.Write(players, PlData);
	}
	
	String Check(String type, String data) {
    	if (!config.isString(type))
            config.set(type, data);
    	return config.getString(type);
    }
    int Check(String type, int data) {
    	if (!config.isInt(type))
            config.set(type, data);
    	return config.getInt(type);
    }
    double Check(String type, double data) {
    	if (!config.isDouble(type))
            config.set(type, data);
    	return config.getDouble(type);
    }
    boolean Check(String type, boolean data) {
    	if (!config.isBoolean(type))
            config.set(type, data);
    	return config.getBoolean(type);
    }
    Long Check(String type, Long data) {
    	if (!config.isInt(type))
            config.set(type, data);
    	return config.getLong(type);
    }
	
	private void Configfile() {
    	File con = new File(getDataFolder(), "config.yml");
        if (!con.exists())
            saveResource("config.yml", false);
        config = this.getConfig();
        wor = Bukkit.getWorld(Check("world", "NextBlock"));
        wor_ref = Bukkit.getWorld(Check("world_reference", "NextBlock_Reference"));
        genSpeed = Check("genSpeed", genSpeed);
        genExSpeed = Check("genExSpeed", genExSpeed);
        genItems.head1x1 = Check("head1x1", "MHF_Exclamation");
        genItems.head3x3 = Check("head3x3", "MHF_Exclamation");
        genItems.head5x5 = Check("head5x5", "MHF_Exclamation");
        genItems.head15x15 = Check("head15x15", "MHF_Exclamation");
        genItems.exHead3x3 = Check("exHead3x3", "MHF_Exclamation");
        genItems.exHead5x5 = Check("exHead5x5", "MHF_Exclamation");
        genItems.exHead9x9 = Check("exHead9x9", "MHF_Exclamation");
        genItems.exHead15x15 = Check("exHead15x15", "MHF_Exclamation");
        base = Check("base", base);
    }
	
	@Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> commands = new ArrayList<>();
        
        switch (args.length) {
	        case 1: 
	        	commands.addAll(Arrays.asList("j","join","ver"));
	            if (sender.hasPermission("NextBlock.set"))
	            	commands.addAll(Arrays.asList("reload","create","give","base"));
	        	break;
	        case 2: 
	        case 3: 
	        	if (sender.hasPermission("Oneblock.set"))
	        		if (args[0].equals("give")) {
	        			commands.addAll(Arrays.asList("gen1x1","gen3x3","gen5x5","gen15x15",
	        					"exGen3x3","exGen5x5","exGen9x9","exGen15x15"));
	        			if (args.length == 2)
	        				for(Player pl : wor.getPlayers())
	        					commands.add(pl.getName());
	        		} else
	        		if (args[0].equals("create"))
	        			commands.add("10000");
	        	break;
	        case 4: 
	        	if (sender.hasPermission("Oneblock.set"))
	        		if (args[0].equals("give"))
	        			for (int i = 1;i <= 1024; i*=2)
	        				commands.add(String.valueOf(i));
	        	break;
        }
        return commands;
    }
}
