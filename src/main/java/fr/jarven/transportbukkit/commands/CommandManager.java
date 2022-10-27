package fr.jarven.transportbukkit.commands;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.Predicate;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.commands.transport.CommandTransportCreate;
import fr.jarven.transportbukkit.commands.transport.CommandTransportDelete;
import fr.jarven.transportbukkit.commands.transport.CommandTransportHelp;
import fr.jarven.transportbukkit.commands.transport.CommandTransportInfo;
import fr.jarven.transportbukkit.commands.transport.CommandTransportList;
import fr.jarven.transportbukkit.commands.transport.CommandTransportMovement;
import fr.jarven.transportbukkit.commands.transport.CommandTransportReload;
import fr.jarven.transportbukkit.commands.transport.CommandTransportRespawn;
import fr.jarven.transportbukkit.commands.transport.CommandTransportSit;
import fr.jarven.transportbukkit.commands.transport.CommandTransportTpHere;
import fr.jarven.transportbukkit.commands.transport.CommandTransportTpTo;

public class CommandManager {
	private CommandManager() {}

	public static void onEnable() {
		Predicate<CommandSender> requireAdmin = s -> s != null && s.hasPermission("transport.admin");
		Predicate<CommandSender> requireWatcher = s -> s != null && s.hasPermission("transport.watcher");

		CommandTree transport = new CommandTree("transport");

		List<String> commandAliases = TransportPlugin.getInstance().getConfig().getStringList("command_aliases");
		String aliases = "";
		if (commandAliases != null && !commandAliases.isEmpty()) {
			transport = transport.withAliases(commandAliases.toArray(new String[0]));
			aliases = "\n§6Aliases: §e" + String.join(", ", commandAliases);
		}

		transport
			.withHelp("Plugin TransportBukkit by Jarven", CommandTransportHelp.getHelpMessage().build(null) + aliases)
			.withRequirement(s -> s != null && s.hasPermission("transport"))
			.then(new CommandTransportHelp().getArgumentTree())
			.then(new CommandTransportInfo().getArgumentTree().withRequirement(requireWatcher))
			.then(new CommandTransportList().getArgumentTree().withRequirement(requireWatcher))
			.then(new CommandTransportReload().getArgumentTree().withRequirement(requireAdmin))
			.then(new CommandTransportCreate().getArgumentTree().withRequirement(requireAdmin))
			.then(new CommandTransportDelete().getArgumentTree().withRequirement(requireAdmin))
			.then(new CommandTransportTpTo().getArgumentTree().withRequirement(requireAdmin))
			.then(new CommandTransportTpHere().getArgumentTree().withRequirement(requireAdmin))
			.then(new CommandTransportSit().getArgumentTree().withRequirement(requireWatcher))
			.then(new CommandTransportRespawn().getArgumentTree().withRequirement(requireAdmin))
			.then(new CommandTransportMovement().getArgumentTree().withRequirement(requireAdmin))
			.register();
	}

	public static void onDisable() {
		CommandAPI.unregister("transport");
	}
}
