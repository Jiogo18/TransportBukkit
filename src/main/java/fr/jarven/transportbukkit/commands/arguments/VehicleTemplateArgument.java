package fr.jarven.transportbukkit.commands.arguments;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.templates.VehicleTemplate;
import fr.jarven.transportbukkit.utils.Messages;

public class VehicleTemplateArgument extends CustomArgument<VehicleTemplate, String> {
	public VehicleTemplateArgument(String nodeName) {
		super(new StringArgument(nodeName), VehicleTemplateArgument::parseVehicle);
		replaceSuggestions(vehicleSuggestions);
	}

	private static ArgumentSuggestions vehicleSuggestions = (info, builder) -> {
		String current = info.currentArg().toLowerCase();
		// List of vehicle names
		for (VehicleTemplate template : TransportPlugin.getTemplateManager().getVehicleTemplates()) {
			if (template.getName().toLowerCase().startsWith(current)) {
				builder.suggest(template.getName());
			}
		}
		return builder.buildFuture();
	};

	private static VehicleTemplate parseVehicle(CustomArgumentInfo<String> info) throws CustomArgumentException {
		String name = info.input();
		return TransportPlugin
			.getTemplateManager()
			.getVehicleTemplate(name)
			.orElseThrow(() -> Messages.createCustomArgumentException(info, Messages.Resources.TEMPLATE_VEHICLE_UNKNOWN.replace("%template_vehicle%", name)));
	}

	public static VehicleTemplate getTemplate(Object[] args, int argIndex) {
		return (VehicleTemplate) args[argIndex];
	}
}