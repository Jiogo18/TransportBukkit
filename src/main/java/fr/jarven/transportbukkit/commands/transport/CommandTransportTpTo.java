package fr.jarven.transportbukkit.commands.transport;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.transportbukkit.commands.CommandTools;
import fr.jarven.transportbukkit.utils.Messages.Resources;
import fr.jarven.transportbukkit.vehicles.Vehicle;

public class CommandTransportTpTo extends CommandTools {
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("tpto")
			.then(vehicleArgument("vehicle_name")
					.executesNative((sender, args) -> { return tpToVehicle(sender, (Vehicle) args[0]); }));
	}

	public int tpToVehicle(NativeProxyCommandSender proxy, Vehicle vehicle) {
		CommandSender puppet = proxy.getCallee();
		if (puppet instanceof Entity) {
			((Entity) puppet).teleport(vehicle.getLocation());
			return 1;
		} else {
			Resources.NOT_AN_ENTITY.send(puppet);
			return 0;
		}
	}
}
