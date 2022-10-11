package fr.jarven.transportbukkit.commands.arguments;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.utils.Messages;
import fr.jarven.transportbukkit.vehicles.Vehicle;

public class VehicleArgument extends CustomArgument<Vehicle, String> {
	public VehicleArgument(String nodeName) {
		super(new StringArgument(nodeName), VehicleArgument::parseVehicle);
		replaceSuggestions(vehicleSuggestions);
	}

	private static ArgumentSuggestions vehicleSuggestions = (info, builder) -> {
		String current = info.currentArg().toLowerCase();
		// List of vehicle names
		for (Vehicle vehicle : TransportPlugin.getVehicleManager().getVehicles()) {
			if (vehicle.getName().toLowerCase().startsWith(current)) {
				builder.suggest(vehicle.getName());
			}
		}
		return builder.buildFuture();
	};

	private static Vehicle parseVehicle(CustomArgumentInfo<String> info) throws CustomArgumentException {
		String vehicleName = info.input();
		return TransportPlugin
			.getVehicleManager()
			.getVehicle(vehicleName)
			.orElseThrow(() -> Messages.createCustomArgumentException(info, Messages.Resources.VEHICLE_UNKNOWN.replace("%vehicle%", vehicleName)));
	}

	public static Vehicle getVehicle(Object[] args, int argIndex) {
		return (Vehicle) args[argIndex];
	}
}