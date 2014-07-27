package org.PortalStick;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;

import org.PortalStick.commands.BaseCommand;
import org.PortalStick.commands.DeleteAllCommand;
import org.PortalStick.commands.DeleteCommand;
import org.PortalStick.commands.DeleteRegionCommand;
import org.PortalStick.commands.FlagCommand;
import org.PortalStick.commands.GetGunCommand;
import org.PortalStick.commands.HelpCommand;
import org.PortalStick.commands.LanguageCommand;
import org.PortalStick.commands.RegionInfoCommand;
import org.PortalStick.commands.RegionListCommand;
import org.PortalStick.commands.RegionToolCommand;
import org.PortalStick.commands.ReloadCommand;
import org.PortalStick.commands.SayCommand;
import org.PortalStick.commands.SetRegionCommand;
import org.PortalStick.fallingblocks.FlyingBlocksAPI;
import org.PortalStick.fallingblocks.TagIdGenerator;
import org.PortalStick.listeners.PortalStickEventListener;
import org.PortalStick.listeners.PortalStickBlockListener;
import org.PortalStick.listeners.PortalStickEntityListener;
import org.PortalStick.listeners.PortalStickPlayerListener;
import org.PortalStick.listeners.PortalStickVehicleListener;
import org.PortalStick.util.BlockUtil;
import org.PortalStick.util.Config;
import org.PortalStick.util.Util;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class PortalStick extends JavaPlugin {
	
	public BaseCommand[] commands;
	public Config config;
	public I18n i18n;
	
	public final EntityManager entityManager = new EntityManager(this);
	public final FunnelBridgeManager funnelBridgeManager = new FunnelBridgeManager(this);
	public final GelManager gelManager = new GelManager(this);
	public final GrillManager grillManager = new GrillManager(this);
	public final PortalManager portalManager = new PortalManager(this);
	public final RegionManager regionManager = new RegionManager(this);
	public final UserManager userManager = new UserManager(this);
	public PortalStickEventListener eventListener = new PortalStickEventListener(this);
	public WorldGuardPlugin worldGuard = null;
	
	public final Util util = new Util(this);
	public final BlockUtil blockUtil = new BlockUtil();
	
	public final PortalStickEntityListener eL = new PortalStickEntityListener(this);
	
	public final Random rand = new Random();
	
	public final FlyingBlocksAPI flyingBlocksAPI = new FlyingBlocksAPI(this);
	public final TagIdGenerator tagIdGenerator = new TagIdGenerator();

	public void onDisable() {
		//config.unLoad() handles cleanup, so let's call it
		config.unLoad();
		getServer().getScheduler().cancelTasks(this);
	}
	
	public void onEnable() {
		config = new Config(this);
		
		//Register events
		Server s = getServer();
		PluginManager pm = s.getPluginManager();
		pm.registerEvents(flyingBlocksAPI, this);
		pm.registerEvents(new PortalStickPlayerListener(this), this);
		pm.registerEvents(new PortalStickBlockListener(this), this);
		pm.registerEvents(new PortalStickVehicleListener(this), this);
		pm.registerEvents(eL, this);
		pm.registerEvents(eventListener, this);
		
		worldGuard = (WorldGuardPlugin) pm.getPlugin("WorldGuard");
		
		config.load();
		i18n = new I18n(this, getFile());
		
		//Teleport all entities.
		s.getScheduler().scheduleSyncRepeatingTask(this, entityManager, 1L, 1L);
		//Garbage-collect the users drop lists.
		s.getScheduler().scheduleSyncRepeatingTask(this, userManager, 600L, 600L);
		
		//Register commands
		ArrayList<BaseCommand> tmpList = new ArrayList<BaseCommand>();
		tmpList.add(new RegionToolCommand(this));
		tmpList.add(new SayCommand(this));
		tmpList.add(new SetRegionCommand(this));
		tmpList.add(new ReloadCommand(this));
		tmpList.add(new DeleteAllCommand(this));
		tmpList.add(new DeleteCommand(this));
		tmpList.add(new HelpCommand(this));
		tmpList.add(new RegionListCommand(this));
		tmpList.add(new DeleteRegionCommand(this));
		tmpList.add(new FlagCommand(this));
		tmpList.add(new RegionInfoCommand(this));
		tmpList.add(new LanguageCommand(this));
		tmpList.add(new GetGunCommand(this));
		commands = tmpList.toArray(new BaseCommand[0]);
		/*ProtocolLibrary.getProtocolManager().addPacketListener(
				new PacketAdapter(this, ListenerPriority.NORMAL,
						PacketType.Play.Server.ENTITY_METADATA) {
					@Override
					public void onPacketSending(final PacketEvent event) {
						PacketContainer packet = event.getPacket();
						
					}
				});*/
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[])
	{
		if (args.length == 0)
			args = new String[]{"help"};
		for (BaseCommand command : commands) {
			if (command.name.equalsIgnoreCase(args[0]))
				return command.run(sender, args, commandLabel);
		}
		return false;
	}
	
	public final String PERM_CREATE_BRIDGE	= "portalstick.createbridge";
	public final String PERM_CREATE_GRILL	= "portalstick.creategrill";
	public final String PERM_PLACE_PORTAL	= "portalstick.placeportal";
	public final String PERM_DELETE_ALL		= "portalstick.admin.deleteall";
	public final String PERM_ADMIN_REGIONS	= "portalstick.admin.regions";
	public final String PERM_DELETE_BRIDGE	= "portalstick.deletebridge";
	public final String PERM_DELETE_GRILL	= "portalstick.deletegrill";
    public final String PERM_GET_GUN        = "portalstick.gun";
	public final String PERM_DAMAGE_BOOTS	= "portalstick.damageboots";
	public final String PERM_TELEPORT 		= "portalstick.teleport";
	public final String PERM_LANGUAGE		= "portalstick.admin.language";
	public final String PERM_DEBUG			= "portalstick.admin.debug";
    
	public boolean hasPermission(Player player, String node) {
		if(player.hasPermission(node))
			return true;
		while(node.contains("."))
		{
			node = node.substring(0, node.lastIndexOf("."));
			if(player.hasPermission(node))
				return true;
			node = node.substring(0, node.length() - 1);
			if(player.hasPermission(node))
				return true;
		}
		return player.hasPermission("*");
	}
}
		    
