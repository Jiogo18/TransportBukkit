package fr.jarven.transportbukkit.vehicles;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.tasks.SaveTask;
import fr.jarven.transportbukkit.templates.VehicleTemplate;

public class VehicleManager {
	final SortedSet<Vehicle> vehicles = new TreeSet<>((a, b) -> a.getName().compareTo(b.getName()));
	private final File vehicleFolder;

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
		return vehicles.remove(vehicle);
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
		SaveTask.reload();
		vehicles.forEach(Vehicle::applyTemplate);

		onDisable();
		if (!vehicleFolder.exists()) {
			makeVehicleFolderIfNeeded();
		} else {
			final SortedSet<Vehicle> previousVehicles = new TreeSet<>(vehicles);

			List<Vehicle> newVehicles =
				Stream.of(vehicleFolder.listFiles(file -> file.getName().endsWith(".yml")))
					.map(file -> {
						String name = file.getName().substring(0, file.getName().length() - 4); // Remove .yml
						try {
							Optional<Vehicle> previousVehicle = previousVehicles.stream().filter(r -> r.getName().equals(name)).findAny();
							return Vehicle.fromConfig(file, previousVehicle);
						} catch (Exception e) {
							TransportPlugin.LOGGER.warning("Vehicle not loaded : '" + name + "'");
							e.printStackTrace();
							return null;
						}
					})
					.filter(vehicle -> vehicle != null)
					.collect(Collectors.toList());

			List<Vehicle> vehiclesToRemove = vehicles
								 .stream()
								 .filter(vehicle -> newVehicles.stream().noneMatch(v -> v.getName().equals(vehicle.getName())))
								 .collect(Collectors.toList());
			vehiclesToRemove.forEach(this::removeVehicleDontDeleteFile);
			newVehicles.forEach(vehicles::add); // Add without dirty
		}
		TransportPlugin.LOGGER.info("Loaded " + vehicles.size() + " vehicles");
	}

	public void onDisable() {
		SaveTask.onDisable();
	}

	public Optional<Seat> getSeatByPassenger(Entity entity) {
		if (entity.getVehicle() == null) return Optional.empty();
		return vehicles
			.stream()
			.map(Vehicle::getSeats)
			.flatMap(List::stream)
			.filter(seat -> seat.getPassenger().isPresent() && seat.getPassenger().get().getUniqueId().equals(entity.getUniqueId()))
			.findFirst();
	}

	public Optional<Vehicle> getVehicleByEntity(Entity entity) {
		return vehicles
			.stream()
			.filter(vehicle -> {
				return vehicle
					       .getParts()
					       .values()
					       .stream()
					       .anyMatch(part -> entity.getUniqueId().equals(part.getEntityUUID()))
					|| vehicle
						   .getSeats()
						   .stream()
						   .anyMatch(seat -> entity.getUniqueId().equals(seat.getEntityUUID()));
			})
			.findFirst();
	}
}
