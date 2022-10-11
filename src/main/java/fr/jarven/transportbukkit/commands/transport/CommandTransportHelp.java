package fr.jarven.transportbukkit.commands.transport;

import org.bukkit.command.CommandSender;

import java.util.List;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.commands.CommandTools;
import fr.jarven.transportbukkit.utils.Messages.MessageBuilder;
import fr.jarven.transportbukkit.utils.Messages.Resources;

public class CommandTransportHelp extends CommandTools {
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("help")
			.executes((sender, args) -> {
				sendHelpMessage(sender);
				return 1;
			});
	}

	public void sendHelpMessage(CommandSender sender) {
		getHelpMessage().send(sender);
	}

	public static MessageBuilder getHelpMessage() {
		String version = TransportPlugin.getInstance().getDescription().getVersion();
		List<String> authors = TransportPlugin.getInstance().getDescription().getAuthors();
		String authorList = String.join(", ", authors);

		return Resources.HELP_BODY
			.replace("%version%", version)
			.replace("%author%", authorList);
	}
}
