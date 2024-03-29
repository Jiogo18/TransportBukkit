package fr.jarven.transportbukkit.vehicles;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.tasks.MovementTask;
import fr.jarven.transportbukkit.tasks.SaveTask;
import fr.jarven.transportbukkit.templates.VehicleTemplate;

public class VehicleManager {
	private final SortedSet<Vehicle> vehicles = new TreeSet<>((a, b) -> a.getName().compareTo(b.getName()));
	private final File vehicleFolder;
	private final Map<UUID, Seat> seatsByPlayer = new HashMap<>();

	public VehicleManager() {
		this.vehicleFolder = new File(TransportPlugin.getInstance().getDataFolder(), "vehicles");
	}

	public Set<Vehicle> getVehicles() {
		return vehicles;
	}

	public Vehicle createVehicle(String name, VehicleTemplate template, Location location) {
		if (getVehicle(name).isPresent()) {
			return null;
		}
		Vehicle vehicle = new Vehicle(name, template, location);
		if (vehicles.add(vehicle)) {
			vehicle.applyTemplate();
			vehicle.makeDirty();
			return vehicle;
		} else {
			return null;
		}
	}

	public boolean removeVehicle(Vehicle vehicle) {
		try {
			if (vehicle.getFile().exists())
				Files.delete(vehicle.getFile().toPath());
			return removeVehicleDontDeleteFile(vehicle);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean removeVehicleDontDeleteFile(Vehicle vehicle) {
		if (this.vehicles.remove(vehicle)) {
			vehicle.removeInternal();
			return true;
		} else {
			return false;
		}
	}

	public Optional<Vehicle> getVehicle(String vehicleName) {
		return vehicles.stream().filter(vehicle -> vehicle.getName().equals(vehicleName)).findFirst();
	}

	public File getVehicleFolder() {
		return vehicleFolder;
	}

	public void makeVehicleFolderIfNeeded() {
		if (!vehicleFolder.exists()) {
			vehicleFolder.mkdirs();
		}
	}

	public void reload() {
		onDisable();
		SaveTask.reload();

		if (!vehicleFolder.exists()) {
			makeVehicleFolderIfNeeded();
		} else {
			final SortedSet<Vehicle> previousVehicles = new TreeSet<>(vehicles);

			List<Vehicle> newVehicles = new ArrayList<>();

			for (File file : vehicleFolder.listFiles()) {
				if (!file.getName().endsWith(".yml"))
					continue;

				String name = file.getName().substring(0, file.getName().length() - 4); // Remove .yml

				try {
					Optional<Vehicle> previousVehicle = previousVehicles.stream().filter(r -> r.getName().equals(name)).findAny();
					Vehicle vehicle = Vehicle.fromConfig(file, previousVehicle);
					if (vehicle != null) {
						newVehicles.add(vehicle);
					}
				} catch (Exception e) {
					TransportPlugin.LOGGER.warning("Vehicle not loaded : '" + name + "'");
					e.printStackTrace();
				}
			}

			List<Vehicle> vehiclesToRemove = vehicles
								 .stream()
								 .filter(vehicle -> newVehicles.stream().noneMatch(v -> v.getName().equals(vehicle.getName())))
								 .collect(Collectors.toList());
			vehiclesToRemove.forEach(this::removeVehicleDontDeleteFile);
			newVehicles.forEach(vehicles::add); // Add without dirty
		}

		vehicles.forEach(Vehicle::updatePassengers);

		TransportPlugin.LOGGER.info("Loaded " + vehicles.size() + " vehicles");
	}

	public void onDisable() {
		seatsByPlayer.clear();
		SaveTask.onDisable();
		MovementTask.onDisable();
	}

	public Optional<Seat> getSeatByPassenger(Entity entity) {
		if (entity.getVehicle() == null) return Optional.empty();
		if (seatsByPlayer.containsKey(entity.getUniqueId())) {
			return Optional.of(seatsByPlayer.get(entity.getUniqueId()));
		}
		return vehicles
			.stream()
			.map(Vehicle::getSeats)
			.flatMap(List::stream)
			.filter(seat -> seat.getPassenger().isPresent() && seat.getPassenger().get().getUniqueId().equals(entity.getUniqueId()))
			.findFirst();
	}

	public Map<UUID, Seat> getSeatByPassengerPlayer() {
		return seatsByPlayer;
	}

	public Optional<Vehicle> getVehicleByEntity(Entity entity) {
		UUID uuid = entity.getUniqueId();
		return vehicles
			.stream()
			.filter(vehicle -> {
				return vehicle
					       .getParts()
					       .values()
					       .stream()
					       .anyMatch(part -> part.isEntityUUID(uuid))
					|| vehicle
						   .getSeats()
						   .stream()
						   .anyMatch(seat -> seat.isEntityUUID(uuid));
			})
			.findFirst();
	}

	protected void onSeatEnter(Entity entity, Seat seat) {
		if (entity instanceof Player) {
			seatsByPlayer.put(entity.getUniqueId(), seat);
		}
	}

	protected void onSeatExit(Entity entity) {
		if (entity instanceof Player) {
			seatsByPlayer.remove(entity.getUniqueId());
		}
	}

	public Set<UUID> getPlayersPassengers() {
		return seatsByPlayer.keySet();
	}

	public void onPlayerJoin(Player player) {
		Optional<Seat> seat = getSeatByPassenger(player);
		if (seat.isPresent()) {
			seatsByPlayer.put(player.getUniqueId(), seat.get());
		}
	}
}
