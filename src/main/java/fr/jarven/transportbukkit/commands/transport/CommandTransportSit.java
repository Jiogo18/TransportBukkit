package fr.jarven.transportbukkit.commands.transport;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.Optional;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.commands.CommandTools;
import fr.jarven.transportbukkit.utils.Messages.Resources;
import fr.jarven.transportbukkit.vehicles.Seat;
import fr.jarven.transportbukkit.vehicles.Vehicle;

public class CommandTransportSit extends CommandTools {
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("sit")
			.then(literal("enter")
					.then(vehicleArgument("vehicle_name")
							.executesNative((sender, args) -> { return enterVehicle(sender, (Vehicle) args[0]); })))
			.then(literal("exit")
					.executesNative((sender, args) -> { return exitVehicle(sender); }))
			.then(literal("lock")
					.then(vehicleArgument("vehicle_name")
							.executesNative((sender, args) -> { return lockVehicle(sender, (Vehicle) args[0]); })))
			.then(literal("unlock")
					.then(vehicleArgument("vehicle_name")
							.executesNative((sender, args) -> { return unlockVehicle(sender, (Vehicle) args[0]); })));
	}

	public int enterVehicle(NativeProxyCommandSender proxy, Vehicle vehicle) {
		CommandSender puppet = proxy.getCallee();
		if (puppet instanceof Entity) {
			if (vehicle.isLocked()) {
				Resources.VEHICLE_LOCKED.replace("%vehicle%", vehicle.getName()).replace("%player%", puppet.getName()).send(proxy);
				return 0;
			}
			if (vehicle.addPassenger((Entity) puppet)) {
				Resources.VEHICLE_ENTERED.replace("%vehicle%", vehicle.getName()).replace("%player%", puppet.getName()).send(proxy);
				return 1;
			} else {
				Resources.VEHICLE_FULL.replace("%vehicle%", vehicle.getName()).send(proxy);
				return 0;
			}
		} else {
			Resources.NOT_AN_ENTITY.send(proxy);
			return 0;
		}
	}

	public int exitVehicle(NativeProxyCommandSender proxy) {
		if (proxy.getCallee() instanceof Entity) {
			Entity puppet = (Entity) proxy.getCallee();
			Optional<Seat> seat = TransportPlugin.getVehicleManager().getSeatByPassenger(puppet);
			if (seat.isPresent()) {
				Vehicle vehicle = seat.get().getVehicle();
				if (vehicle.isLocked()) {
					Resources.VEHICLE_LOCKED.replace("%vehicle%", vehicle.getName()).replace("%player%", puppet.getName()).send(proxy);
					return 0;
				} else if (vehicle.removePassenger(puppet)) {
					Resources.VEHICLE_EXITED.replace("%vehicle%", vehicle.getName()).replace("%player%", puppet.getName()).send(proxy);
					return 1;
				} else {
					Resources.NOT_IN_VEHICLE.replace("%player%", puppet.getName()).send(proxy);
					return 0;
				}
			} else {
				Resources.NOT_IN_VEHICLE.replace("%player%", puppet.getName()).send(proxy);
				return 0;
			}
		} else {
			Resources.NOT_AN_ENTITY.send(proxy);
			return 0;
		}
	}

	public int lockVehicle(NativeProxyCommandSender proxy, Vehicle vehicle) {
		if (vehicle.isLocked()) {
			Resources.VEHICLE_ALREADY_LOCKED.replace("%vehicle%", vehicle.getName()).send(proxy);
			return 0;
		} else {
			vehicle.lock(true);
			Resources.VEHICLE_LOCKED.replace("%vehicle%", vehicle.getName()).send(proxy);
			return 1;
		}
	}

	public int unlockVehicle(NativeProxyCommandSender proxy, Vehicle vehicle) {
		if (!vehicle.isLocked()) {
			Resources.VEHICLE_ALREADY_UNLOCKED.replace("%vehicle%", vehicle.getName()).send(proxy);
			return 0;
		} else {
			vehicle.lock(false);
			Resources.VEHICLE_UNLOCKED.replace("%vehicle%", vehicle.getName()).send(proxy);
			return 1;
		}
	}
}
