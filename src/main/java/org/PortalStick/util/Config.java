package org.PortalStick.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.PortalStick.PortalStick;
import org.PortalStick.components.Bridge;
import org.PortalStick.components.Grill;
import org.PortalStick.components.Portal;
import org.PortalStick.components.Region;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sanjay900.nmsUtil.events.EntityDespawnEvent;
import com.sanjay900.nmsUtil.events.EntitySpawnEvent;
import com.sanjay900.nmsUtil.util.V10Location;


public class Config {

	private final PortalStick plugin;
	private FileConfiguration mainConfig;
	private final FileConfiguration regionConfig;
	private final FileConfiguration grillConfig;
	private final FileConfiguration bridgeConfig;

	private final File regionConfigFile;
	private final File grillConfigFile;
	private final File bridgeConfigFile;

	public HashSet<String> DisabledWorlds;
	public int PortalTool;
	public short portalToolData; //Short for spout compatiblity!
	public String portalToolName, portalToolDesc;
	public Region GlobalRegion;
	public int RegionTool;
	public boolean RestoreInvOnWorldChange;
	public List<String> ColorPresets;
	public byte portalBackData;
	public String textureURL = null;
	public String defaultTextureURL = null;

	public boolean useNativeSounds, useSpoutSounds;
	public int soundRange;
	public final String[] soundUrls = new String[Sound.values().length];
	public final String[] soundNative = new String[Sound.values().length];
	public final String[] musicUrls = new String[Sound.values().length];

	public String lang;

	public boolean debug;

	public Config (PortalStick instance) {

		plugin = instance;

		regionConfigFile = getConfigFile("regions.yml");
		grillConfigFile = getConfigFile("grills.yml");
		bridgeConfigFile = getConfigFile("bridges.yml");


		mainConfig = plugin.getConfig();
		regionConfig = getConfig(regionConfigFile);
		grillConfig = getConfig(grillConfigFile);
		bridgeConfig = getConfig(bridgeConfigFile);
	}

	public void deleteGrill(String grill) {
		List<String> list =  grillConfig.getStringList("grills");
		list.remove(grill);
		grillConfig.set("grills", list);
		saveAll();
	}

	public void deleteRegion(String name) {
		regionConfig.set(name, null);
		saveAll();
	}

	public void deleteBridge(String bridge) {
		List<String> list = bridgeConfig.getStringList("bridges");
		list.remove(bridge);
		bridgeConfig.set("bridges", list);
		saveAll();
	}

	@SuppressWarnings("deprecation")
	public void load() {
		try {
			mainConfig = plugin.getConfig();
			regionConfig.load(regionConfigFile);
			grillConfig.load(grillConfigFile);
			bridgeConfig.load(bridgeConfigFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        //Load main settings
        DisabledWorlds = new HashSet<String>(getStringList("main.disabled-worlds", new ArrayList<String>()));
        String[] split = getString("main.portal-tool", "280:0").split(":");
        PortalTool = Integer.parseInt(split[0]);
        if(split.length > 1)
          portalToolData = Short.parseShort(split[1]);
        else
          portalToolData = 0;
        portalToolName = ChatColor.translateAlternateColorCodes('&', getString("main.portal-tool-name", "&6Aperture Science Handheld Portal Device"));
        portalToolDesc = ChatColor.translateAlternateColorCodes('&', getString("main.portal-tool-description", "&aThanks to the ASHPD,\n&2the impossible is easy."));
        RegionTool = getInt("main.region-tool", 268);
        RestoreInvOnWorldChange = getBoolean("main.restore-inventory-on-world-change", true);
        ColorPresets = getStringList("main.portal-color-presets", Arrays.asList(new String[]{"9-1","3-10","4-14"}));
        Iterator<String> iter = ColorPresets.iterator();
        debug = getBoolean("Debug", true); //TODO: True cause beta.
        String st;
        byte a = 0, b = a;
        boolean valid, changed = false;;
        
        while(iter.hasNext()) {
            st = iter.next();
            split = st.split("-");
            valid = true;
            if(split.length != 2)
                valid = false;
            if(valid)
                try {
                    a = Byte.parseByte(split[0]);
                    b = Byte.parseByte(split[1]);
                } catch(NumberFormatException e) {
                    valid = false;
                }
            if(valid)
                valid = a > 0 && b > 0 &&
                        a < 16 && b < 16 &&
                        !DyeColor.getByData(a).name().contains("GRAY") &&
                        !DyeColor.getByData(b).name().contains("GRAY");
            if(!valid) {
                if(debug)
                    plugin.getLogger().warning("Removing invalid color preset: "+st);
                iter.remove();
                changed = true;
            }
        }
        if(changed)
            mainConfig.set("main.portal-color-presets", ColorPresets);
        split = getString("main.fill-portal-back", "-1").split(":");
        if(split.length > 1)
          portalBackData = Byte.parseByte(split[1]);
        else
          portalBackData = 0;
        
        //Load texture settings
        boolean useURL = getBoolean("texture.use-custom-texture", true);
        toggleTextureURL(false);
        if(!useURL)
            textureURL = null;
        
        //Load sound settings
        useNativeSounds = getBoolean("sounds.use-minecraft-sounds", true);
        soundNative[Sound.PORTAL_CREATE_BLUE.ordinal()] = getString("sounds.minecraft.create-blue-portal", "STEP_WOOL:0.3");
        soundNative[Sound.PORTAL_CREATE_ORANGE.ordinal()] = getString("sounds.minecraft.create-orange-portal", "STEP_WOOL:0.3");
        soundNative[Sound.PORTAL_EXIT_BLUE.ordinal()] = getString("sounds.minecraft.exit-blue-portal", "ENDERMAN_TELEPORT");
        soundNative[Sound.PORTAL_EXIT_ORANGE.ordinal()] = getString("sounds.minecraft.exit-orange-portal", "ENDERMAN_TELEPORT");
        soundNative[Sound.PORTAL_CANNOT_CREATE.ordinal()] = getString("sounds.minecraft.cannot-create-portal", ""); //TODO: Find sound...
        soundNative[Sound.GRILL_EMANCIPATE.ordinal()] = getString("sounds.minecraft.grill-emancipate", "FIZZ");
        soundNative[Sound.FAITHPLATE_LAUNCH.ordinal()] = getString("sounds.minecraft.faith-plate-launch", "EXPLODE:0.5");
        soundNative[Sound.GEL_BLUE_BOUNCE.ordinal()] = getString("sounds.minecraft.blue-gel-bounce", "SLIME_WALK2");
        
        useSpoutSounds = getBoolean("sounds.use-spout-sounds", false);
        
        soundUrls[Sound.PORTAL_CREATE_BLUE.ordinal()] = getString("sounds.spout.create-blue-portal-url", "");
        soundUrls[Sound.PORTAL_CREATE_ORANGE.ordinal()] = getString("sounds.spout.create-orange-portal-url", "");
        soundUrls[Sound.PORTAL_EXIT_BLUE.ordinal()] = getString("sounds.spout.exit-blue-portal-url", "");
        soundUrls[Sound.PORTAL_EXIT_ORANGE.ordinal()] = getString("sounds.spout.exit-orange-portal-url", "");
        soundUrls[Sound.PORTAL_CANNOT_CREATE.ordinal()] = getString("sounds.spout.cannot-create-portal-url", "");
        soundUrls[Sound.GRILL_EMANCIPATE.ordinal()] = getString("sounds.spout.grill-emancipate-url", "");
        soundUrls[Sound.FAITHPLATE_LAUNCH.ordinal()] = getString("sounds.spout.faith-plate-launch-url", "");
        soundUrls[Sound.GEL_BLUE_BOUNCE.ordinal()] = getString("sounds.spout.blue-gel-bounce-url", "");
        
        soundRange = getInt("sounds.sound-range", 20);
        
        Locale locale = Locale.getDefault();
        lang = getString("Language", locale.getLanguage().toLowerCase()+"_"+locale.getCountry());
        
		//Load all current users
//		for (Player player : plugin.getServer().getOnlinePlayers())
//			plugin.userManager.createUser(player);

        //Load all regions
        for (String regionName : regionConfig.getKeys(false))
        	if(!regionName.equals("global"))
        		plugin.regionManager.loadRegion(regionName, null, null);
        plugin.regionManager.loadRegion("global", null, null);
        if(debug)
        	plugin.getLogger().info((plugin.regionManager.regions.size()-1) + " (" + plugin.regionManager.regions.size() + ") region(s) loaded");
        
        //Validate regions
        for(Region region: plugin.regionManager.regions.values())
        	if(!region.validateRedGel() && debug)
        		plugin.getLogger().info("Inavlid red-gel-max-velocity for region \""+region.name+"\" - fixing!");
        
        //Load grills
        for (String grill : (grillConfig.getStringList("grills")))
        	plugin.grillManager.loadGrill(grill);
        if(debug)
        	plugin.getLogger().info(plugin.grillManager.grills.size() + " grill(s) loaded");
        //Load bridges
        for (String bridge : bridgeConfig.getStringList("bridges"))
        	plugin.funnelBridgeManager.loadBridge(bridge);
        if(debug)
        	plugin.getLogger().info(plugin.funnelBridgeManager.bridges.size() + " bridge(s) loaded");
        
        saveAll();
		for(World w: plugin.getServer().getWorlds()) {
		    if(!DisabledWorlds.contains(w.getName())) {
		        for(Chunk c: w.getLoadedChunks()) {
		            for(Entity e: c.getEntities()) {
		                plugin.eL.spawn(new EntitySpawnEvent(e));
		            }
		        }
		    }
		}
	}

	public boolean toggleTextureURL(boolean save) {
	    if(textureURL == null) {
	        if(save)
	            mainConfig.set("texture.use-custom-texture", true);
	        textureURL = getString("texture.custom-URL", "https://eyrenetwork.net/resourcepacks/Portal.zip");
	    } else {
	        if(save)
	            mainConfig.set("texture.use-custom-texture", false);
	        textureURL = null;
	    }
	    defaultTextureURL = getString("texture.default-URL", "https://eyrenetwork.net/resourcepacks/default.zip");
	    if(save)
	        saveAll();
	    return textureURL != null;
	}

	private int getInt(String path, int def)
	{

		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);

		return mainConfig.getInt(path, def);
	}

	private String getString(String path, String def)
	{
		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);

		return mainConfig.getString(path, def);
	}

	private List<String> getStringList(String path, List<String> def)
	{
		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);

		return mainConfig.getStringList(path);
	}

	private boolean getBoolean(String path, Boolean def)
	{
		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);

		return mainConfig.getBoolean(path, def);
	}

	public void reLoad() {
		unLoad();
		load();
	}

	public void unLoad()
	{
		
		plugin.funnelBridgeManager.deleteAll();
		for(Portal p: plugin.portalManager.portals.toArray(new Portal[0]))
			p.delete();
		plugin.portalManager.portals.clear();
		plugin.grillManager.deleteAll();
		for(V10Location loc: plugin.gelManager.gels.keySet())
		  plugin.gelManager.stopGelTube(loc);
		
		for(World w: plugin.getServer().getWorlds()) {
		    if(!DisabledWorlds.contains(w.getName())) {
		        for(Chunk c: w.getLoadedChunks()) {
		            for(Entity e: c.getEntities()) {
		                plugin.eL.despawn(new EntityDespawnEvent(e));
		            }
		        }
		    }
		}
	}

	public boolean loadRegionSettings(Region region, Player player) {
		for (RegionSetting setting : RegionSetting.values()) {
			Object prop = regionConfig.get(region.name + "." + setting.getYaml());
    		if (prop == null)
    		{
    			if(!region.settings.containsKey(setting))
    				region.settings.put(setting, setting.getDefault());
    		}
    		else
    			region.settings.put(setting, prop);
    		regionConfig.set(region.name + "." + setting.getYaml(), region.settings.get(setting));
    	}
		return region.updateLocation(player);
	}

	private File getConfigFile(String filename)
	{
		if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();

		File file = new File(plugin.getDataFolder(), filename);
		return file;
	}
	private FileConfiguration getConfig(File file) {
		FileConfiguration config = null;
		try {
			config = new YamlConfiguration();
			if (file.exists())
			{
				config.load(file);
				config.set("setup", null);
			}
			config.save(file);

			return config;
		} catch (Exception e) {
			plugin.getLogger().severe("Unable to load YAML file " + file.getAbsolutePath());
			e.printStackTrace();
		}
		return null;
	}

	public void saveAll() {

		//Save regions
		for (Map.Entry<String, Region> entry : plugin.regionManager.regions.entrySet()) {
			Region region = entry.getValue();
			for (Entry<RegionSetting, Object> setting : region.settings.entrySet())
				regionConfig.set(region.name + "." + setting.getKey().getYaml(), setting.getValue());
		}
		try
		{
			regionConfig.save(regionConfigFile);
		}
		catch (Exception e)
		{
			plugin.getLogger().severe("Error while writing to regions.yml");
			e.printStackTrace();
		}

		//Save grills
		grillConfig.set("grills", null);
		List<String> list = new ArrayList<String>();
		for (Grill grill : plugin.grillManager.grills)
			list.add(grill.getStringLocation());
		grillConfig.set("grills", list);
		try
		{
			grillConfig.save(grillConfigFile);
		}
		catch (Exception e)
		{
			plugin.getLogger().severe("Error while writing to grills.yml");
			e.printStackTrace();
		}

		//Save bridges
		bridgeConfig.set("bridges", null);
		list = new ArrayList<String>();
		for (Bridge bridge : plugin.funnelBridgeManager.bridges)
			list.add(bridge.getStringLocation());
		bridgeConfig.set("bridges", list);
		try
		{
			bridgeConfig.save(bridgeConfigFile);
		}
		catch (Exception e)
		{
			plugin.getLogger().severe("Error while writing to bridges.yml");
			e.printStackTrace();
		}

		//Save main
		mainConfig.set("Language", lang);
		mainConfig.set("Debug", debug);
		plugin.saveConfig();			
	}

	public enum Sound {
		PORTAL_CREATE_BLUE,
		PORTAL_CREATE_ORANGE,
		PORTAL_EXIT_BLUE,
		PORTAL_EXIT_ORANGE,
		PORTAL_CANNOT_CREATE,
		GRILL_EMANCIPATE,
		FAITHPLATE_LAUNCH,
		GEL_BLUE_BOUNCE,
	}
}