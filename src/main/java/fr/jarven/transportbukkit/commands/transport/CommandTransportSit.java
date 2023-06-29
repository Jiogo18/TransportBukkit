package fr.jarven.transportbukkit.commands.transport;

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
					.then(vehicleArgument()
							.executesNative((sender, args) -> (enterVehicle(sender, getVehicle(args), false)))
							.then(literal("--force-lock")
									.executesNative((sender, args) -> (enterVehicle(sender, getVehicle(args), true))))))
			.then(literal("exit")
					.executesNative((sender, args) -> (exitVehicle(sender, false)))
					.then(literal("--force-lock")
							.executesNative((sender, args) -> (exitVehicle(sender, true)))))
			.then(literal("lock")
					.then(vehicleArgument()
							.executesNative((sender, args) -> (lockVehicle(sender, getVehicle(args))))))
			.then(literal("unlock")
					.then(vehicleArgument()
							.executesNative((sender, args) -> (unlockVehicle(sender, getVehicle(args))))));
	}

	public int enterVehicle(NativeProxyCommandSender proxy, Vehicle vehicle, boolean force) {
		if (!(proxy.getCallee() instanceof Entity)) {
			Resources.NOT_AN_ENTITY.send(proxy);
			return 0;
		}
		Entity puppet = (Entity) proxy.getCallee();

		boolean success;
		if (force) {
			success = vehicle.addPassengerForce(puppet);
		} else if (vehicle.isLocked()) {
			Resources.VEHICLE_LOCKED.replace("%vehicle%", vehicle.getName()).replace("%player%", puppet.getName()).send(proxy);
			return 0;
		} else {
			success = vehicle.addPassenger(puppet);
		}
		if (success) {
			Resources.VEHICLE_ENTERED.replace("%vehicle%", vehicle.getName()).replace("%player%", puppet.getName()).send(proxy);
			return 1;
		} else {
			Resources.VEHICLE_FULL.replace("%vehicle%", vehicle.getName()).send(proxy);
			return 0;
		}
	}

	public int exitVehicle(NativeProxyCommandSender proxy, boolean force) {
		if (!(proxy.getCallee() instanceof Entity)) {
			Resources.NOT_AN_ENTITY.send(proxy);
			return 0;
		}
		Entity puppet = (Entity) proxy.getCallee();

		Optional<Seat> seat = TransportPlugin.getVehicleManager().getSeatByPassenger(puppet);
		if (!seat.isPresent()) {
			Resources.NOT_IN_VEHICLE.replace("%player%", puppet.getName()).send(proxy);
			return 0;
		}

		Vehicle vehicle = seat.get().getVehicle();
		boolean success;
		if (force) {
			success = vehicle.removePassengerForce(puppet);
		} else if (vehicle.isLocked()) {
			Resources.VEHICLE_LOCKED.replace("%vehicle%", vehicle.getName()).replace("%player%", puppet.getName()).send(proxy);
			return 0;
		} else {
			success = vehicle.removePassenger(puppet);
		}
		if (success) {
			Resources.VEHICLE_EXITED.replace("%vehicle%", vehicle.getName()).replace("%player%", puppet.getName()).send(proxy);
			return 1;
		} else {
			Resources.NOT_IN_VEHICLE.replace("%player%", puppet.getName()).send(proxy);
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
