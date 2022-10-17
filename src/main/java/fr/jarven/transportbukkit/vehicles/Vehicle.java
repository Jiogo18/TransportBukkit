package fr.jarven.transportbukkit.vehicles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.tasks.MovementTask;
import fr.jarven.transportbukkit.tasks.SaveTask;
import fr.jarven.transportbukkit.templates.PartTemplate;
import fr.jarven.transportbukkit.templates.SeatProperties;
import fr.jarven.transportbukkit.templates.VehicleTemplate;
import fr.jarven.transportbukkit.utils.LocationRollable;
import fr.jarven.transportbukkit.utils.MovementsVector;

public class Vehicle {
	private final String name;
	private final VehicleTemplate template;
	private final Map<PartTemplate, VehiclePart> parts = new HashMap<>();
	private final List<Seat> seats = new ArrayList<>();
	private LocationRollable location;
	private LocationRollable locationWithOffset;
	private MovementsVector velocity = new MovementsVector(0, 0, 0);
	private MovementsVector acceleration = new MovementsVector(0, 0, 0);
	private long saveTimestamp = 0;
	private boolean locked = false;
	private YamlConfiguration config;
	private static final Random random = new Random();
	private BukkitTask updateRealTask = null;

	public Vehicle(String name, VehicleTemplate template, Location location) {
		this(name, template, new LocationRollable(location, 0));
	}

	public Vehicle(String name, VehicleTemplate template, LocationRollable location) {
		this.name = name;
		this.template = template;
		this.setLocation(location);
		this.setDestination(null);
	}

	protected void applyTemplate() {
		applyTemplateParts();
		applyTemplateSeats();
		applyTemplateProperties();
		teleport(getLocation());
	}

	private void applyTemplateParts() {
		List<PartTemplate> partsTemplates = template.getParts();
		parts.keySet().stream().filter(partTemplate -> !partsTemplates.contains(partTemplate)).forEach(this::removePart);

		// Add and update parts from config
		for (PartTemplate partTemplate : partsTemplates) {
			boolean partAlreadyCreated = parts.containsKey(partTemplate);
			VehiclePart part;
			if (partAlreadyCreated) {
				part = parts.get(partTemplate);
			} else {
				part = VehiclePart.createPart(this, partTemplate);
			}
			if (config != null) {
				ConfigurationSection partConfig = config.getConfigurationSection("parts." + partTemplate.getName());
				if (partConfig != null) {
					part.loadConfig(partConfig);
				}
			}
			if (partAlreadyCreated) {
				part.update();
			} else {
				addPart(part);
			}
		}
	}

	private void applyTemplateSeats() {
		List<SeatProperties> seatsTemplates = template.getSeats();
		for (int i = seatsTemplates.size(); i < seats.size(); i++) {
			seats.stream().skip(i).findFirst().ifPresent(seats::remove);
		}

		for (SeatProperties seatTemplate : seatsTemplates) {
			boolean seatAlreadyCreated = seatsTemplates.indexOf(seatTemplate) < seats.size();
			Seat seat;
			if (seatAlreadyCreated) {
				seat = seats.get(seatsTemplates.indexOf(seatTemplate));
			} else {
				seat = new Seat(this, seatTemplate);
				seats.add(seat);
			}
			if (config != null) {
				ConfigurationSection seatConfig = config.getConfigurationSection("seats." + seatTemplate.getSeatIndex());
				if (seatConfig != null) {
					seat.loadConfig(seatConfig);
				}
			}
			seat.spawn();
		}
	}

	private void applyTemplateProperties() {
		velocity.applyMaximum(template.getMaxSpeed());
		acceleration.applyMaximum(template.getMaxAcceleration());
	}

	public String getName() {
		return name;
	}

	public VehicleTemplate getTemplate() {
		return template;
	}

	public Map<PartTemplate, VehiclePart> getParts() {
		return parts;
	}

	private VehiclePart addPart(VehiclePart part) {
		parts.put(part.getTemplate(), part);
		part.setVehicle(this);
		part.update();
		if (part.getTemplate().isAnimated()) {
			TransportPlugin.getAnimationManager().animateVehiclePart(part);
		}
		return part;
	}

	private void removePart(PartTemplate partTemplate) {
		VehiclePart part = parts.remove(partTemplate);
		if (partTemplate.isAnimated()) {
			TransportPlugin.getAnimationManager().animateVehiclePart(part);
		}
		part.removeInternal();
	}

	public boolean addPassenger(Entity passenger) {
		if (locked) {
			return false;
		}

		// A seat without passenger (sorted randomly)
		Optional<Seat> randomSeat =
			seats
				.stream()
				.filter(seat -> !seat.hasPassenger())
				.sorted((a, b) -> random.nextInt())
				.findAny();

		if (randomSeat.isPresent()) {
			randomSeat.get().addPassenger(passenger);
			return true;
		} else {
			return false;
		}
	}

	public boolean addPassenger(Entity entity, Seat seat) {
		if (locked || seat.hasPassenger()) {
			return false;
		}
		seat.addPassenger(entity);
		return true;
	}

	public boolean removePassenger(Entity passenger) {
		if (locked) {
			return false;
		}
		for (Seat seat : seats) {
			if (seat.removePassenger(passenger)) {
				return true;
			}
		}
		return false;
	}

	public boolean isLocked() {
		return this.locked;
	}

	public void lock(boolean locked) {
		this.locked = locked;
	}

	public boolean isFull() {
		return seats.stream().allMatch(Seat::hasPassenger);
	}

	public List<Entity> getPassengers() {
		return this.seats.stream().map(Seat::getPassenger).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
	}

	public List<Seat> getSeats() {
		return seats;
	}

	public void respawn() {
		for (VehiclePart part : parts.values()) {
			part.respawn();
		}
		for (Seat seat : seats) {
			seat.respawn();
		}
	}

	protected void removeInternal() {
		this.parts.values().forEach(VehiclePart::removeInternal);
		this.parts.clear();
		this.seats.forEach(Seat::removeInternal);
		this.seats.clear();
	}

	public void teleport(LocationRollable location) {
		if (location == null) return;
		LocationRollable previousLocation = this.location;
		this.setLocation(location);
		updateFakeLocation(); // Send packets
		updateRealLocation(); // Update real location with teleport (delayed)
		if (!this.location.equals(previousLocation)) {
			makeLazyDirty(); // Save in a long time
		}
	}

	private void updateFakeLocation() {
		for (VehiclePart part : parts.values()) {
			part.updateFakeLocation();
		}
		for (Seat seat : seats) {
			seat.updateFakeLocation();
		}
	}

	private void updateRealLocation() {
		// Create a bukit task to update the location of the vehicle
		if (updateRealTask == null) {
			updateRealTask = Bukkit.getScheduler().runTaskLater(TransportPlugin.getInstance(), () -> {
				for (VehiclePart part : parts.values()) {
					part.updateRealLocation();
				}
				for (Seat seat : seats) {
					seat.updateRealLocation();
				}
				// Send a packet in 2 ticks to for update
				Bukkit.getScheduler().runTaskLater(TransportPlugin.getInstance(), () -> updateFakeLocation(), 2L);
				Bukkit.getScheduler().runTaskLater(TransportPlugin.getInstance(), () -> updateFakeLocation(), 4L);
				updateRealTask = null;
			}, 200L); // Update the real location (not a fake packet) every 10 seconds
		}
	}

	private void setLocation(LocationRollable location) {
		this.location = location;
		this.locationWithOffset = new LocationRollable(location).add(template.getOffset());
	}

	public LocationRollable getLocation() {
		return location;
	}

	public LocationRollable getLocationWithOffset() {
		return new LocationRollable(locationWithOffset); // clone
	}

	public void setDestination(LocationRollable destination) {
		if (destination != null && this.location != null && this.location.getWorld().getName().equals(destination.getWorld().getName())) {
			MovementTask.initiateMovements(this, destination, 0);
		} else {
			MovementTask.stopMovements(this);
		}
	}

	public Location getDestination() {
		return MovementTask.getDestination(this);
	}

	void setVelocity(MovementsVector velocity) {
		this.velocity = velocity;
	}

	public MovementsVector getAllVelocity() {
		return velocity;
	}

	public double getVelocity() {
		return velocity.getDistanceWithOrigin();
	}

	void setAcceleration(MovementsVector acceleration) {
		this.acceleration = acceleration;
	}

	public MovementsVector getAllAcceleration() {
		return acceleration;
	}

	public double getAcceleration() {
		return acceleration.getDistanceWithOrigin();
	}

	protected void makeDirty() {
		SaveTask.saveLater(this);
	}

	/**
	 * Save the vehicle in a long time
	 */
	protected void makeLazyDirty() {
		SaveTask.saveInALongTime(this);
	}

	protected void makeClean() {
		SaveTask.cancelSaveLater(this);
	}

	protected File getFile() {
		return new File(TransportPlugin.getVehicleManager().getVehicleFolder(), getName() + ".yml");
	}

	public void save() {
		File file = getFile();
		if (!file.exists()) {
			try {
				TransportPlugin.getVehicleManager().makeVehicleFolderIfNeeded();
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		if (config == null) {
			config = new YamlConfiguration();
		}
		saveTimestamp = System.currentTimeMillis();
		config.set("name", name);
		config.set("template", template.getName());
		config.set("location", location);
		config.set("saveTimestamp", saveTimestamp);
		config.set("locked", locked);
		for (Map.Entry<PartTemplate, VehiclePart> entry : parts.entrySet()) {
			entry.getValue().saveConfig(config.createSection("parts." + entry.getKey().getName()));
		}
		for (Seat seat : seats) {
			seat.saveConfig(config.createSection("seats." + seats.indexOf(seat)));
		}

		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		makeClean();
	}

	protected void loadConfig() {
		SaveTask.saveNowIfNeeded(this);
		File file = getFile();
		if (!file.exists())
			return;

		config = YamlConfiguration.loadConfiguration(file);
		String newName = config.getString("name");
		if (!newName.equals(this.name)) {
			TransportPlugin.LOGGER.warning("Vehicle " + this.name + " has been renamed to " + newName + " in file " + file.getName() + ". Need restart to apply.");
		}
		String templateName = config.getString("template");
		if (!templateName.equals(this.template.getName())) {
			TransportPlugin.LOGGER.warning("Vehicle " + this.name + " has been changed template to " + templateName + " in file " + file.getName() + ". Need restart to apply.");
		}
		this.setLocation((LocationRollable) config.get("location"));
		this.saveTimestamp = config.getLong("saveTimestamp");
		this.locked = config.getBoolean("locked");

		// Update parts and seats with applyTemplate
		applyTemplate();

		makeClean();
	}

	protected static Vehicle fromConfig(File file, Optional<Vehicle> existingVehicle) {
		Vehicle vehicle;

		if (existingVehicle.isPresent()) {
			vehicle = existingVehicle.get();
		} else {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

			String name = config.getString("name");
			if (name == null) {
				throw new IllegalArgumentException("Vehicle Template's name can't be null (" + file.getName() + ")");
			}

			String templateName = config.getString("template");
			Optional<VehicleTemplate> template = TransportPlugin.getTemplateManager().getVehicleTemplate(templateName);
			if (!template.isPresent()) {
				throw new IllegalArgumentException("Invalid Vehicle Template " + templateName + " (" + file.getName() + ")");
			}

			LocationRollable location = (LocationRollable) config.get("location");
			if (location == null) {
				throw new IllegalArgumentException("Invalid Vehicle Location " + location + " (" + file.getName() + ")");
			}

			vehicle = new Vehicle(name, template.get(), location);
			vehicle.config = config;
		}
		vehicle.loadConfig();
		return vehicle;
	}

	public long getSaveTimestamp() {
		return saveTimestamp;
	}
}
