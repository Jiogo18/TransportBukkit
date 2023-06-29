package fr.jarven.transportbukkit.commands.arguments;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.templates.PartTemplate;
import fr.jarven.transportbukkit.utils.Messages;

public class PartTemplateArgument extends CustomArgument<PartTemplate, String> {
	public PartTemplateArgument(String nodeName) {
		super(new StringArgument(nodeName), PartTemplateArgument::parsePart);
		replaceSuggestions(PartSuggestions);
	}

	private static ArgumentSuggestions<CommandSender> PartSuggestions = (info, builder) -> {
		String current = info.currentArg().toLowerCase();
		// List of Part names
		for (PartTemplate template : TransportPlugin.getTemplateManager().getPartTemplates()) {
			if (template.getName().toLowerCase().startsWith(current)) {
				builder.suggest(template.getName());
			}
		}
		return builder.buildFuture();
	};

	private static PartTemplate parsePart(CustomArgumentInfo<String> info) throws CustomArgumentException {
		String name = info.input();
		return TransportPlugin
			.getTemplateManager()
			.getPartTemplate(name)
			.orElseThrow(() -> Messages.createCustomArgumentException(info, Messages.Resources.TEMPLATE_PART_UNKNOWN.replace("%template_part%", name)));
	}

	public static PartTemplate getTemplate(CommandArguments args, String nodeName) {
		return (PartTemplate) args.get(nodeName);
	}
}