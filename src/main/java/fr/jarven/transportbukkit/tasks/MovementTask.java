package fr.jarven.transportbukkit.tasks;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.utils.LocationRollable;
import fr.jarven.transportbukkit.vehicles.Vehicle;
import fr.jarven.transportbukkit.vehicles.VehicleMover;

public class MovementTask {
	private static BukkitTask task;
	private static final Map<Vehicle, VehicleMover> vehicles = new HashMap<>();

	private MovementTask() {
	}

	private static void startTask() {
		if (task == null) {
			task = Bukkit.getScheduler().runTaskTimer(TransportPlugin.getInstance(), () -> {
				for (VehicleMover mover : vehicles.values()) {
					mover.tickMovement();
				}
			}, 1, 1);
		}
	}

	public static void stopTask() {
		if (task != null) {
			if (!task.isCancelled()) {
				task.cancel();
			}
			task = null;
		}
		if (!vehicles.isEmpty()) {
			vehicles.clear();
		}
	}

	public static void onDisable() {
		stopTask();
	}

	public static void initiateMovements(Vehicle vehicle, LocationRollable destination, float velocityAtEnd) {
		if (vehicle == null) return;

		if (!vehicles.containsKey(vehicle)) {
			if (VehicleMover.haveToMove(vehicle, destination)) {
				vehicles.put(vehicle, new VehicleMover(vehicle, destination, velocityAtEnd));
			}
			// Else, don't have to move to the destination => do nothing
		} else {
			VehicleMover mover = vehicles.get(vehicle);
			if (VehicleMover.canMoveTo(vehicle, destination)) {
				mover.setDestination(destination, velocityAtEnd);
			} else {
				mover.setDestination(null, 0); // Slow down and stop
			}
			vehicles.get(vehicle).setDestination(destination, velocityAtEnd);
		}
		if (task == null) {
			startTask();
		}
	}

	public static void stopMovements(Vehicle vehicle) {
		if (vehicle == null) return;

		if (vehicles.containsKey(vehicle)) {
			VehicleMover mover = vehicles.get(vehicle);
			vehicles.remove(vehicle);
			mover.stopMovement();
		}
		if (vehicles.isEmpty()) {
			stopTask();
		}
	}

	public static LocationRollable getDestination(Vehicle vehicle) {
		if (vehicle == null) return null;

		if (vehicles.containsKey(vehicle)) {
			return vehicles.get(vehicle).getDestination();
		}
		return null;
	}
}
