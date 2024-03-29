package fr.jarven.transportbukkit.commands.transport;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.commands.CommandTools;
import fr.jarven.transportbukkit.templates.VehicleTemplate;
import fr.jarven.transportbukkit.utils.Messages.Resources;
import fr.jarven.transportbukkit.vehicles.Vehicle;

public class CommandTransportCreate extends CommandTools {
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("create")
			.then(vehicleTemplateArgument()
					.then(new StringArgument("vehicle_name")
							.replaceSuggestions((info, builder) -> {
								VehicleTemplate template = getVehicleTemplate(info.previousArgs());
								String templateName = template.getName().toLowerCase() + '_';
								int index = 1;
								while (TransportPlugin.getVehicleManager().getVehicle(templateName + index).isPresent()) {
									index++;
								}
								return builder.suggest(templateName + index).buildFuture();
							})
							.executesNative((sender, args) -> (createVehicle(sender, getVehicleTemplate(args), (String) args.get("vehicle_name"), sender.getLocation())))
							.then(locationRollableArgument((sender, args, loc) -> createVehicle(sender, getVehicleTemplate(args), (String) args.get("vehicle_name"), loc)))
							.executesConsole((sender, args) -> { Resources.NEED_LOCATION.send(sender); return 0; })));
	}

	public int createVehicle(CommandSender sender, VehicleTemplate template, String name, Location location) {
		if (TransportPlugin.getVehicleManager().getVehicle(name).isPresent()) {
			Resources.VEHICLE_ALREADY_EXISTS
				.replace("%vehicle%", name)
				.send(sender);
			return 0;
		}

		Vehicle vehicle = TransportPlugin.getVehicleManager().createVehicle(name, template, location);
		if (vehicle == null) {
			Resources.VEHICLE_CREATION_FAILED
				.replace("%vehicle%", name)
				.send(sender);
			return 0;
		} else {
			Resources.VEHICLE_CREATED
				.replace("%vehicle%", name)
				.send(sender);
			return 1;
		}
	}
}
