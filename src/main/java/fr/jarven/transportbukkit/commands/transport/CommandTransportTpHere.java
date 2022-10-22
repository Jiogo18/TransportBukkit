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
			.then(vehicleArgument("vehicle_name")
					.executesNative((sender, args) -> { return tpVehicle(sender, (Vehicle) args[0], new LocationRollable(sender.getLocation())); })
					.then(locationRollableArgument(1, (sender, args, loc) -> tpVehicle(sender, (Vehicle) args[0], loc)))
					.executesConsole((sender, args) -> { Resources.NEED_LOCATION.send(sender); return 1; }));
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
