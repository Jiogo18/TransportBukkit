package fr.jarven.transportbukkit.commands.transport;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.transportbukkit.commands.CommandTools;
import fr.jarven.transportbukkit.utils.LocationRollable;
import fr.jarven.transportbukkit.utils.Messages.Resources;
import fr.jarven.transportbukkit.vehicles.Vehicle;

public class CommandTransportTpHere extends CommandTools {
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("tphere")
			.then(vehicleArgument()
					.executesNative((sender, args) -> (tpVehicle(sender, getVehicle(args), new LocationRollable(sender.getLocation()))))
					.then(locationRollableArgument((sender, args, loc) -> tpVehicle(sender, getVehicle(args), loc)))
					.executesConsole(sendNeedLocation));
	}

	public int tpVehicle(CommandSender sender, Vehicle vehicle, LocationRollable location) {
		vehicle.teleport(location);

		Resources.VEHICLE_TELEPORTED
			.replace("%vehicle%", vehicle.getName())
			.replace("%location%", toReadbleString(location))
			.send(sender);
		return 1;
	}
}
