package org.PortalStick;

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
import org.PortalStick.commands.ToggleTextureCommand;
import org.PortalStick.listeners.PortalStickBlockListener;
import org.PortalStick.listeners.PortalStickEntityListener;
import org.PortalStick.listeners.PortalStickPlayerListener;
import org.PortalStick.listeners.PortalStickVehicleListener;
import org.PortalStick.managers.CubeManager;
import org.PortalStick.managers.EntityManager;
import org.PortalStick.managers.FunnelBridgeManager;
import org.PortalStick.managers.GelManager;
import org.PortalStick.managers.GrillManager;
import org.PortalStick.managers.PortalManager;
import org.PortalStick.managers.RegionManager;
import org.PortalStick.managers.UserManager;
import org.PortalStick.managers.WireManager;
import org.PortalStick.util.Config;
import org.PortalStick.util.I18n;
import org.PortalStick.util.Util;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.sanjay900.nmsUtil.util.BlockUtil;
import com.sanjay900.nmsUtil.util.V10Location;
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
	public final CubeManager cubeManager = new CubeManager();
	public final WireManager wireManager = new WireManager(this);
	public WorldGuardPlugin worldGuard = null;
	
	public final Util util = new Util(this);
	public final BlockUtil blockUtil = new BlockUtil();
	
	public final PortalStickEntityListener eL = new PortalStickEntityListener(this);
	
	public final Random rand = new Random();
	
	public void onDisable() {
		//config.unLoad() handles cleanup, so let's call it
		wireManager.cleanUpWire();
		config.unLoad();
		getServer().getScheduler().cancelTasks(this);
		for (V10Location vloc : cubeManager.buttonsToEntity.values()) {
			util.changeBtn(vloc, false);
		}
	}
	
	public void onEnable() {
		config = new Config(this);
		//Register events
		Server s = getServer();
		PluginManager pm = s.getPluginManager();
		PortalStickPlayerListener pL = new PortalStickPlayerListener(this);
		ProtocolLibrary.getProtocolManager().addPacketListener(pL);
		pm.registerEvents(pL, this);
		pm.registerEvents(new PortalStickBlockListener(this), this);
		pm.registerEvents(new PortalStickVehicleListener(this), this);
		pm.registerEvents(eL, this);
		
		worldGuard = (WorldGuardPlugin) pm.getPlugin("WorldGuard");
		
		config.load();
		i18n = new I18n(this, getFile());
		
		//Teleport all entities. Check if any entities have moved.
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
		tmpList.add(new ToggleTextureCommand(this));
		commands = tmpList.toArray(new BaseCommand[0]);
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
	public final String PERM_TEXTURE        = "portalstick.admin.texture";
	public final String PERM_SAY            = "portalstick.admin.say";
    
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
		    
