package fr.jarven.transportbukkit.commands.transport;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.commands.CommandTools;

public class CommandTransportReload extends CommandTools {
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("reload")
			.then(literal("animations").executes((sender, args) -> {
				TransportPlugin.getAnimationManager().reload();
				sender.sendMessage("§fReloaded animations");
				return 1;
			}))
			.then(literal("templates").executes((sender, args) -> {
				TransportPlugin.getTemplateManager().reload();
				sender.sendMessage("§fReloaded templates");
				return 1;
			}))
			.then(literal("config").executes((sender, args) -> {
				TransportPlugin.getInstance().reloadConfig();
				;
				sender.sendMessage("§fReloaded config");
				return 1;
			}))
			.then(literal("vehicles").executes((sender, args) -> {
				TransportPlugin.getVehicleManager().reload();
				sender.sendMessage("§fReloaded vehicles");
				return 1;
			}))
			.executes((sender, args) -> {
				TransportPlugin.getInstance().reload();
				sender.sendMessage("§fTransportPlugin reloaded");
				return 1;
			});
	}
}
