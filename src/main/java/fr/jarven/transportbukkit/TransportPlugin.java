package fr.jarven.transportbukkit;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.logging.Logger;

import fr.jarven.transportbukkit.animations.AnimationManager;
import fr.jarven.transportbukkit.commands.CommandManager;
import fr.jarven.transportbukkit.listeners.PlayerListener;
import fr.jarven.transportbukkit.templates.SeatProperties;
import fr.jarven.transportbukkit.templates.TemplateManager;
import fr.jarven.transportbukkit.utils.ItemTemplate;
import fr.jarven.transportbukkit.utils.LocationRollable;
import fr.jarven.transportbukkit.utils.Messages;
import fr.jarven.transportbukkit.utils.MovementsConstraints;
import fr.jarven.transportbukkit.vehicles.VehicleManager;

public class TransportPlugin extends JavaPlugin {
	public static final Logger LOGGER = Logger.getLogger("TransportBukkit");
	private static TransportPlugin instance;
	private TemplateManager templateManager;
	private VehicleManager vehicleManager;
	private AnimationManager animationManager;
	private ProtocolManager protocolManager;

	@Override
	public void onLoad() {
		instance = this;
		Path dataFolder = getDataFolder().toPath();
		if (!dataFolder.toFile().exists()) {
			dataFolder.toFile().mkdir();
		}
		ConfigurationSerialization.registerClass(SeatProperties.class);
		ConfigurationSerialization.registerClass(MovementsConstraints.class);
		ConfigurationSerialization.registerClass(ItemTemplate.class);
		ConfigurationSerialization.registerClass(LocationRollable.class);
		templateManager = new TemplateManager();
		vehicleManager = new VehicleManager();
		animationManager = new AnimationManager();
		protocolManager = ProtocolLibrary.getProtocolManager();
		loadConfig();
	}

	@Override
	public void onEnable() {
		super.onEnable();
		LOGGER.info("Loading plugin & configurations...");
		try {
			templateManager.reload();
			vehicleManager.reload();
			animationManager.reload();
			CommandManager.onEnable();
			registerListeners();
			LOGGER.info("TransportBukkit enabled");
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.severe("Failed to load plugin");
		}
	}

	public void loadConfig() {
		saveDefaultConfig();
		FileConfiguration config = getConfig();
		Messages.loadConfig(config);
	}

	public void reload() {
		LOGGER.info("Reloading TransportBukkit...");
		super.reloadConfig();
		loadConfig();
		templateManager.reload();
		vehicleManager.reload();
		animationManager.reload();
	}

	@Override
	public void onDisable() {
		super.onDisable();
		CommandManager.onDisable();
		vehicleManager.onDisable();
		LOGGER.info("TransportBukkit is disabled!");
	}

	private void registerListeners() {
		PluginManager pluginManager = Bukkit.getPluginManager();
		try {
			pluginManager.registerEvents(new PlayerListener(), this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static TransportPlugin getInstance() {
		return instance;
	}

	public static TemplateManager getTemplateManager() {
		return instance.templateManager;
	}

	public static VehicleManager getVehicleManager() {
		return instance.vehicleManager;
	}

	public static AnimationManager getAnimationManager() {
		return instance.animationManager;
	}

	public static ProtocolManager getProtocolManager() {
		return instance.protocolManager;
	}
}
