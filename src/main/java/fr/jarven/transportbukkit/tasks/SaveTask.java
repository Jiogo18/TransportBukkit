package fr.jarven.transportbukkit.tasks;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.vehicles.Vehicle;

public class SaveTask {
	private static final Map<Vehicle, BukkitTask> vehicles = new HashMap<>();
	private static final Map<Vehicle, BukkitTask> vehiclesLazySaves = new HashMap<>();
	public static int saveDelay = 10; // seconds
	public static int lazySaveDelay = 600; // seconds

	private SaveTask() {
	}

	public static void saveAllNowIfNeeded() {
		new HashSet<Vehicle>(vehicles.keySet()).forEach(SaveTask::saveNowIfNeeded);
		new HashSet<Vehicle>(vehiclesLazySaves.keySet()).forEach(SaveTask::saveNowIfNeeded);
	}

	public static void onDisable() {
		saveAllNowIfNeeded();
	}

	public static void saveLater(Vehicle vehicle) {
		if (vehicle == null) return;
		if (vehicles.containsKey(vehicle))
			return;

		cancelLazySave(vehicle);

		vehicles.put(
			vehicle,
			Bukkit.getScheduler().runTaskLater(TransportPlugin.getInstance(), () -> {
				vehicle.save();
				vehicles.remove(vehicle);
			}, saveDelay * 20L));
	}

	/**
	 * Lazy save : save the vehicle in a long time
	 * (usually when the vehicle has moved)
	 */
	public static void saveInALongTime(Vehicle vehicle) {
		if (vehicle == null) return;
		if (vehiclesLazySaves.containsKey(vehicle) || vehicles.containsKey(vehicle))
			return;

		vehiclesLazySaves.put(
			vehicle,
			Bukkit.getScheduler().runTaskLater(TransportPlugin.getInstance(), () -> {
				vehicle.save();
				vehiclesLazySaves.remove(vehicle);
			}, lazySaveDelay * 20L));
	}

	public static void cancelSaveLater(Vehicle vehicle) {
		if (vehicle == null) return;
		if (vehicles.containsKey(vehicle)) {
			BukkitTask task = vehicles.remove(vehicle);
			if (!task.isCancelled())
				task.cancel();
		}
		if (vehiclesLazySaves.containsKey(vehicle)) {
			BukkitTask task = vehiclesLazySaves.remove(vehicle);
			if (!task.isCancelled())
				task.cancel();
		}
	}

	public static void cancelLazySave(Vehicle vehicle) {
		if (vehicle == null) return;
		if (!vehiclesLazySaves.containsKey(vehicle))
			return;
		BukkitTask task = vehiclesLazySaves.remove(vehicle);
		if (!task.isCancelled())
			task.cancel();
	}

	public static void saveNowIfNeeded(Vehicle vehicle) {
		if (vehicles.containsKey(vehicle)) {
			cancelSaveLater(vehicle);
			vehicle.save();
		}
	}

	public static void reload() {
		SaveTask.saveDelay = TransportPlugin.getInstance().getConfig().getInt("saveDelay", 10);
		SaveTask.lazySaveDelay = TransportPlugin.getInstance().getConfig().getInt("lazySaveDelay", 600);
	}
}
