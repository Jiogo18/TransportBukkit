package fr.jarven.transportbukkit.commands.transport;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.transportbukkit.commands.CommandTools;
import fr.jarven.transportbukkit.utils.Messages.Resources;
import fr.jarven.transportbukkit.vehicles.Vehicle;

public class CommandTransportRespawn extends CommandTools {
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("respawn")
			.then(vehicleArgument()
					.executesNative((sender, args) -> (respawnVehicle(sender, getVehicle(args)))));
	}

	public int respawnVehicle(CommandSender sender, Vehicle vehicle) {
		vehicle.respawn();
		Resources.VEHICLE_RESPAWNED.replace("%vehicle%", vehicle.getName()).send(sender);
		return 1;
	}
}
