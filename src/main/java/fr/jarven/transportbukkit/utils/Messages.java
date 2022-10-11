package fr.jarven.transportbukkit.utils;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentInfo;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.templates.PartTemplate;
import fr.jarven.transportbukkit.templates.VehicleTemplate;
import fr.jarven.transportbukkit.vehicles.Vehicle;
import fr.jarven.transportbukkit.vehicles.VehiclePart;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class Messages {
	private static String defaultLanguage = "en_US";
	private static YamlConfiguration defaultTranslations = null;
	private static Map<String, YamlConfiguration> languages = new HashMap<>();
	private static final String LANGUAGE_VERSION = "2022-10-06";
	private static boolean alwaysSaveLang = false;

	public enum Resources {
		HELP_BODY("transport.help.body"),

		VEHICLE_UNKNOWN("transport.vehicle.unknown"),
		VEHICLE_INFO("transport.vehicle.info"),
		VEHICLE_LIST("transport.vehicle.list"),
		VEHICLE_ALREADY_EXISTS("transport.vehicle.already_exists"),
		VEHICLE_CREATED("transport.vehicle.created"),
		VEHICLE_CREATION_FAILED("transport.vehicle.creation_failed"),
		VEHICLE_REMOVED("transport.vehicle.removed"),
		VEHICLE_REMOVAL_FAILED("transport.vehicle.removal_failed"),
		VEHICLE_TELEPORTED("transport.vehicle.teleported"),

		TEMPLATE_VEHICLE_UNKNOWN("transport.template.vehicle.unknown"),
		TEMPLATE_VEHICLE_LIST("transport.template.vehicle.list"),
		TEMPLATE_VEHICLE_INFO("transport.template.vehicle.info"),
		TEMPLATE_PART_UNKNOWN("transport.template.part.unknown"),
		TEMPLATE_PART_LIST("transport.template.part.list"),
		TEMPLATE_PART_INFO("transport.template.part.info"),

		PART_UNKNOWN("transport.part.unknown"),
		PART_INFO("transport.part.info"),

		PLAYER_INFO("transport.player.info"),
		NOT_AN_ENTITY("transport.not_an_entity"),
		NEED_LOCATION("transport.need_location"),

		VEHICLE_ENTERED("transport.sit.enter"),
		VEHICLE_FULL("transport.sit.full"),
		VEHICLE_EXITED("transport.sit.exit"),
		NOT_IN_VEHICLE("transport.sit.not_in_vehicle"),
		VEHICLE_LOCKED("transport.sit.locked"),
		VEHICLE_ALREADY_LOCKED("transport.sit.already_locked"),
		VEHICLE_UNLOCKED("transport.sit.unlocked"),
		VEHICLE_ALREADY_UNLOCKED("transport.sit.already_unlocked"),
		;

		private String key;
		Resources(String key) {
			this.key = key;
		}
		private String getKey() {
			return key;
		}
		public MessageBuilder getBuilder() {
			return new MessageBuilder(this);
		}
		public MessageBuilder replace(String key, String value) {
			return getBuilder().replace(key, value);
		}
		public MessageBuilder params(Object... objects) {
			return getBuilder().params(objects);
		}
		private String build(CommandSender sender) {
			if (sender instanceof NativeProxyCommandSender) return build(((NativeProxyCommandSender) sender).getCaller());
			return Messages.tr(sender, this);
		}
		public Resources send(CommandSender sender) {
			sender.sendMessage(build(sender));
			return this;
		}
		public Resources send(NativeProxyCommandSender proxy) {
			proxy.sendMessage(build(proxy));
			return this;
		}
		public void send(Player player, ChatMessageType position) {
			player.spigot().sendMessage(position, TextComponent.fromLegacyText(build(player)));
		}
		public Resources sendFailure(CommandSender sender) {
			sender.spigot().sendMessage(new ComponentBuilder(build(sender)).color(ChatColor.RED).create());
			return this;
		}
		public Resources sendFailure(NativeProxyCommandSender proxy) {
			proxy.spigot().sendMessage(new ComponentBuilder(build(proxy)).color(ChatColor.RED).create());
			return this;
		}
		public Resources sendFailure(Player player, ChatMessageType position) {
			player.spigot().sendMessage(position, new ComponentBuilder(build(player)).color(ChatColor.RED).create());
			return this;
		}
	}

	public static class MessageBuilder {
		private final Resources messageResource;
		private final Map<String, String> replacements = new HashMap<>();
		private final Map<String, MessageBuilder> messageReplacements = new HashMap<>();
		private MessageBuilder(Resources key) {
			this.messageResource = key;
		}
		public MessageBuilder replace(String key, String value) {
			replacements.put(key, value);
			return this;
		}
		public MessageBuilder replace(String key, MessageBuilder value) {
			messageReplacements.put(key, value);
			return this;
		}
		public MessageBuilder params(Object... objects) {
			for (int i = 0; i < objects.length; i++) {
				Object o = objects[i];
				if (o == null) {
				} else if (o instanceof Vehicle) {
					replace("%vehicle%", ((Vehicle) o).getName());
				} else if (o instanceof VehiclePart) {
					replace("%vehicle_part%", ((VehiclePart) o).getTemplate().getName());
				} else if (o instanceof PartTemplate) {
					replace("%part_template%", ((PartTemplate) o).getName());
				} else if (o instanceof VehicleTemplate) {
					replace("%vehicle_template%", ((VehicleTemplate) o).getName());
				} else if (o instanceof Player) {
					replace("%player%", ((Player) o).getName());
				} else if (o instanceof CommandSender) {
					replace("%sender%", ((CommandSender) o).getName());
				} else if (o instanceof Location) {
					Location loc = (Location) o;
					replace("%world%", loc.getWorld().getName());
					replace("%x%", String.valueOf(loc.getX()));
					replace("%y%", String.valueOf(loc.getY()));
					replace("%z%", String.valueOf(loc.getZ()));
					replace("%pitch%", String.valueOf(loc.getPitch()));
					replace("%yaw%", String.valueOf(loc.getYaw()));
					replace("%X%", String.valueOf(loc.getBlockX()));
					replace("%Y%", String.valueOf(loc.getBlockY()));
					replace("%Z%", String.valueOf(loc.getBlockZ()));
				} else {
					replace("%" + o.getClass().getName().toLowerCase() + "%", objects[i + 1].toString());
				}
			}
			return this;
		}
		public String build(CommandSender sender) {
			if (sender instanceof NativeProxyCommandSender) {
				return build(((NativeProxyCommandSender) sender).getCaller());
			}
			String message = Messages.tr(sender, messageResource);
			for (Map.Entry<String, MessageBuilder> entry : messageReplacements.entrySet()) {
				if (message.contains(entry.getKey())) {
					message = message.replace(entry.getKey(), entry.getValue().build(sender));
				}
			}
			for (Map.Entry<String, String> entry : replacements.entrySet()) {
				if (message.contains(entry.getKey())) {
					message = message.replace(entry.getKey(), entry.getValue());
				}
			}
			return message;
		}
		public MessageBuilder send(CommandSender sender) {
			sender.sendMessage(build(sender));
			return this;
		}
		public MessageBuilder send(NativeProxyCommandSender proxy) {
			proxy.getCaller().sendMessage(build(proxy.getCaller()));
			return this;
		}
		public MessageBuilder send(Player player, ChatMessageType position) {
			player.spigot().sendMessage(position, TextComponent.fromLegacyText(build(player)));
			return this;
		}
		public MessageBuilder sendToBoth(NativeProxyCommandSender proxy) {
			if (!proxy.getCallee().equals(proxy.getCaller())) {
				send(proxy.getCaller());
			}
			send(proxy.getCallee());
			return this;
		}
		public MessageBuilder sendFailure(CommandSender sender) {
			sender.spigot().sendMessage(new ComponentBuilder(build(sender)).color(ChatColor.RED).create());
			return this;
		}
		public MessageBuilder sendFailure(NativeProxyCommandSender proxy) {
			proxy.getCaller().spigot().sendMessage(new ComponentBuilder(build(proxy.getCaller())).color(ChatColor.RED).create());
			return this;
		}
		public MessageBuilder sendFailure(Player player, ChatMessageType position) {
			player.spigot().sendMessage(position, new ComponentBuilder(build(player)).color(ChatColor.RED).create());
			return this;
		}
	}

	private Messages() {}

	public static void loadConfig(FileConfiguration config) {
		defaultLanguage = config.getString("lang", "en_US");
		alwaysSaveLang = config.getBoolean("always_save_lang", false);
		// The folder /lang to store the language files (and custom ones)
		File folder = new File(TransportPlugin.getInstance().getDataFolder(), "lang");
		if (!folder.exists()) {
			folder.mkdir();
		}
		// Save if not present
		saveLanguageFile("en_US", false);
		saveLanguageFile("fr_FR", false);
		// Get every files written in the /lang folder (even the files wich are not in the resources)
		for (File file : folder.listFiles(f -> f.getName().endsWith(".yml"))) {
			addLanguage(file);
		}
		// Determine the default language
		defaultTranslations = languages.get(defaultLanguage.toLowerCase());
		if (defaultTranslations == null) {
			defaultTranslations = languages.get("en_US");
			if (defaultTranslations == null) {
				TransportPlugin.LOGGER.warning("No default language found, using en_US or " + defaultLanguage);
			}
		}
	}

	private static void saveLanguageFile(String lang, boolean replace) {
		File file = new File(TransportPlugin.getInstance().getDataFolder(), "lang/" + lang + ".yml");
		if (!file.exists() || replace) {
			TransportPlugin.getInstance().saveResource("lang/" + lang + ".yml", replace);
		}
	}

	private static void addLanguage(File file) {
		String name = file.getName().replace(".yml", "");
		YamlConfiguration lang = YamlConfiguration.loadConfiguration(file);
		String version = lang.getString("version.revision", "");
		boolean versionWarning = lang.getBoolean("version.warnings");
		boolean autoupdate = lang.getBoolean("version.autoupdate");
		// If outdated
		if (!version.equals(LANGUAGE_VERSION) || alwaysSaveLang) {
			// Update it
			if (autoupdate) {
				if (versionWarning) {
					TransportPlugin.LOGGER.warning("Language file " + name + " is outdated, updating to version " + LANGUAGE_VERSION);
				}
				saveLanguageFile(name, true);
				lang = YamlConfiguration.loadConfiguration(file);

				version = lang.getString("version.revision", "");
				// If the resource is outdated
				if (!version.equals(LANGUAGE_VERSION)) {
					TransportPlugin.LOGGER.severe("Language file " + name + " of TransportBukkit's resources is outdated. Please, contact the plugin author to update it."
						+ " (expected " + LANGUAGE_VERSION + ", found " + version + ")");
				}
			} else {
				if (versionWarning) {
					TransportPlugin.LOGGER.warning("Language file " + name + " is outdated, please update or delete it");
				}
			}
		}
		// Register the language's config
		languages.put(name.toLowerCase(), lang);
	}

	private static String tr(String local, String messageKey) {
		YamlConfiguration lang = languages.getOrDefault(local.toLowerCase(), defaultTranslations);
		if (lang == null) {
			TransportPlugin.LOGGER.warning("No language found for " + local + " and " + defaultLanguage);
			return messageKey;
		}
		String translated = lang.getString(messageKey, null);
		if (translated != null) return translated; // local translation

		if (defaultTranslations == null) return messageKey; // no default language (shouldn't happen)

		return defaultTranslations.getString(messageKey, messageKey); // default translation
	}

	public static String tr(CommandSender sender, Resources message) {
		String local = defaultLanguage;
		if (sender instanceof Player) local = ((Player) sender).getLocale();
		return tr(local, message.getKey());
	}

	public static CustomArgumentException createCustomArgumentException(CustomArgumentInfo<?> info, Resources message) {
		return new CustomArgumentException(tr(info.sender(), message));
	}

	public static CustomArgumentException createCustomArgumentException(CustomArgumentInfo<?> info, MessageBuilder builder) {
		return new CustomArgumentException(builder.build(info.sender()));
	}
}