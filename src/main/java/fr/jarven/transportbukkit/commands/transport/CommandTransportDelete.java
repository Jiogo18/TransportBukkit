package fr.jarven.transportbukkit.commands.transport;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.commands.CommandTools;
import fr.jarven.transportbukkit.utils.Messages.Resources;
import fr.jarven.transportbukkit.vehicles.Vehicle;

public class CommandTransportDelete extends CommandTools {
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("delete")
			.then(vehicleArgument()
					.executes((sender, args) -> (removeVehicle(sender, getVehicle(args)))));
	}

	public int removeVehicle(CommandSender sender, Vehicle vehicle) {
		if (TransportPlugin.getVehicleManager().removeVehicle(vehicle)) {
			Resources.VEHICLE_REMOVED
				.replace("%vehicle%", vehicle.getName())
				.send(sender);
			return 1;
		} else {
			Resources.VEHICLE_REMOVAL_FAILED
				.replace("%vehicle%", vehicle.getName())
				.send(sender);
			return 0;
		}
	}
}
