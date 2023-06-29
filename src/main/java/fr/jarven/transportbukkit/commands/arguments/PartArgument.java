package fr.jarven.transportbukkit.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.Map;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import fr.jarven.transportbukkit.commands.CommandTools;
import fr.jarven.transportbukkit.templates.PartTemplate;
import fr.jarven.transportbukkit.utils.Messages;
import fr.jarven.transportbukkit.vehicles.Vehicle;
import fr.jarven.transportbukkit.vehicles.VehiclePart;

public class PartArgument extends CustomArgument<VehiclePart, String> {
	public PartArgument(String nodeName) {
		super(new StringArgument(nodeName), PartArgument::parseVehicle);
		replaceSuggestions(vehicleSuggestions);
	}

	private static ArgumentSuggestions<CommandSender> vehicleSuggestions = (info, builder) -> {
		Vehicle vehicle = CommandTools.getVehicle(info.previousArgs());
		if (vehicle == null) {
			return builder.buildFuture();
		}
		String current = info.currentArg().toLowerCase();
		// List of vehicle names
		for (Map.Entry<PartTemplate, VehiclePart> part : vehicle.getParts().entrySet()) {
			if (part.getKey().getName().toLowerCase().startsWith(current)) {
				builder.suggest(part.getKey().getName());
			}
		}
		return builder.buildFuture();
	};

	private static VehiclePart parseVehicle(CustomArgumentInfo<String> info) throws CustomArgumentException {
		Vehicle vehicle = CommandTools.getVehicle(info.previousArgs());
		String partName = info.input();
		return vehicle
			.getParts()
			.values()
			.stream()
			.filter(part -> part.getTemplate().getName().equals(partName))
			.findFirst()
			.orElseThrow(() -> Messages.createCustomArgumentException(info, Messages.Resources.PART_UNKNOWN.replace("%part%", partName)));
	}

	public static VehiclePart getPart(CommandArguments args, String nodeName) {
		return (VehiclePart) args.get(nodeName);
	}
}