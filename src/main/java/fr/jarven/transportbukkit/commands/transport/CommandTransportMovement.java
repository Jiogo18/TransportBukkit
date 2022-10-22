package fr.jarven.transportbukkit.commands.transport;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import fr.jarven.transportbukkit.commands.CommandTools;
import fr.jarven.transportbukkit.tasks.MovementTask;
import fr.jarven.transportbukkit.utils.LocationRollable;
import fr.jarven.transportbukkit.utils.Messages.Resources;
import fr.jarven.transportbukkit.vehicles.Vehicle;

public class CommandTransportMovement extends CommandTools {
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("movement")
			.then(literal("move_here")
					.then(vehicleArgument("vehicle_name")
							.executesNative((sender, args) -> { return moveVehicle(sender, (Vehicle) args[0], new LocationRollable(sender.getLocation())); })
							.then(locationRollableArgument(1, (sender, args, loc) -> moveVehicle(sender, (Vehicle) args[0], loc))))
					.executesConsole(this::needLocation))
			.then(literal("move_here_no_rotation")
					.then(vehicleArgument("vehicle_name")
							.executesNative((sender, args) -> {
								return moveVehicle(sender, (Vehicle) args[0], catLocationRotation(sender.getLocation(), ((Vehicle) args[0]).getLocation()));
							})
							.then(new LocationArgument("location").executes((sender, args) -> {
								return moveVehicle(sender, (Vehicle) args[0], catLocationRotation((Location) args[1], ((Vehicle) args[0]).getLocation()));
							}))
							.executesConsole(this::needLocation)))
			.then(literal("stop")
					.then(vehicleArgument("vehicle_name")
							.executesNative((sender, args) -> { return stopVehicle(sender, (Vehicle) args[0]); })));
	}

	public int moveVehicle(CommandSender sender, Vehicle vehicle, LocationRollable destination) {
		vehicle.setDestination(destination);

		LocationRollable dest = MovementTask.getDestination(vehicle);

		if (dest != null) {
			Resources.VEHICLE_MOVING
				.replace("%vehicle%", vehicle.getName())
				.replace("%destination%", toReadbleString(destination))
				.send(sender);
		} else {
			Resources.VEHICLE_ARRIVED
				.replace("%vehicle%", vehicle.getName())
				.replace("%destination%", toReadbleString(destination))
				.send(sender);
		}

		return 1;
	}

	public int stopVehicle(CommandSender sender, Vehicle vehicle) {
		LocationRollable dest = MovementTask.getDestination(vehicle);
		vehicle.setDestination(null);

		if (dest != null) {
			Resources.VEHICLE_STOPPED
				.replace("%vehicle%", vehicle.getName())
				.send(sender);
		} else {
			Resources.VEHICLE_NOT_MOVING
				.replace("%vehicle%", vehicle.getName())
				.send(sender);
		}
		return 1;
	}

	private LocationRollable catLocationRotation(Location blockDestination, LocationRollable rotationDestination) {
		LocationRollable destination = new LocationRollable(blockDestination);
		destination.setYaw(rotationDestination.getYaw());
		destination.setPitch(rotationDestination.getPitch());
		destination.setRoll(rotationDestination.getRoll());
		return destination;
	}

	private int needLocation(CommandSender sender, Object[] args) {
		Resources.NEED_LOCATION.send(sender);
		return 0;
	}
}
