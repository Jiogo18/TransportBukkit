package fr.jarven.transportbukkit.templates;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.utils.MovementsConstraints;
import fr.jarven.transportbukkit.utils.MovementsVector;

public class VehicleTemplate {
	private final String name;
	private final List<PartTemplate> parts = new ArrayList<>();
	private final List<SeatProperties> seats = new ArrayList<>();
	private boolean lockWhenMoving;
	private MovementsVector offset;
	private final MovementsConstraints maxSpeed;
	private final MovementsConstraints maxAcceleration;

	protected VehicleTemplate(String name, MovementsVector offset, MovementsConstraints maxSpeed, MovementsConstraints maxAcceleration) {
		this.name = name;
		this.offset = offset;
		this.maxSpeed = maxSpeed;
		this.maxAcceleration = maxAcceleration;
	}

	public String getName() {
		return name;
	}

	public List<PartTemplate> getParts() {
		return parts;
	}

	public List<SeatProperties> getSeats() {
		return seats;
	}

	public SeatProperties getSeat(int templateIndex) {
		return seats.get(templateIndex);
	}

	public MovementsVector getOffset() {
		return offset;
	}

	public MovementsConstraints getMaxSpeed() {
		return maxSpeed;
	}

	public MovementsConstraints getMaxAcceleration() {
		return maxAcceleration;
	}

	public boolean isLockWhenMoving() {
		return lockWhenMoving;
	}

	@SuppressWarnings("java:S3776") // Cognitive Complexity
	public static VehicleTemplate fromFile(File file) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		String name = config.getString("name");
		if (name == null) {
			TransportPlugin.LOGGER.warning("Vehicle template " + file.getName() + " has no name, not loaded");
			return null;
		}
		MovementsVector offset = MovementsVector.fromConfig(config.getConfigurationSection("offset"));
		MovementsConstraints maxSpeed = (MovementsConstraints) config.get("max_speed");
		if (maxSpeed == null) {
			TransportPlugin.LOGGER.warning("Vehicle template " + file.getName() + " has no max_speed, not loaded");
			return null;
		}
		MovementsConstraints maxAcceleration = config.getObject("max_acceleration", MovementsConstraints.class);
		if (maxAcceleration == null) {
			TransportPlugin.LOGGER.warning("Vehicle template " + file.getName() + " has no max_acceleration, not loaded");
			return null;
		}

		VehicleTemplate vehicleTemplate = new VehicleTemplate(name, offset, maxSpeed, maxAcceleration);

		List<String> parts = config.getStringList("parts");
		if (parts != null) {
			for (String partName : parts) {
				Optional<PartTemplate> partTemplate = TransportPlugin.getTemplateManager().getPartTemplate(partName);
				if (partTemplate.isPresent()) {
					vehicleTemplate.parts.add(partTemplate.get());
				} else {
					TransportPlugin.getInstance().getLogger().warning("Part template " + partName + " not found for vehicle " + name);
				}
			}
		}

		List<?> seats = config.getList("seats");
		if (seats != null) {
			for (Object object : seats) {
				try {
					SeatProperties seatTemplate = SeatProperties.fromConfig(object);
					seatTemplate.setSeatIndex(vehicleTemplate.seats.size());
					vehicleTemplate.seats.add(seatTemplate);
				} catch (Exception e) {
					TransportPlugin.getInstance().getLogger().warning("Invalid Seat " + object + " for vehicle " + name);
					TransportPlugin.LOGGER.warning("Invalid seat " + object + " for vehicle " + name);
				}
			}
		}

		vehicleTemplate.lockWhenMoving = config.getBoolean("lock_when_moving", false);

		return vehicleTemplate;
	}

	public void update(VehicleTemplate template) {
		this.parts.clear();
		this.parts.addAll(template.parts);
		while (this.seats.size() > template.seats.size()) {
			this.seats.remove(this.seats.size() - 1);
		}
		for (int i = 0; i < template.seats.size(); i++) {
			if (i < this.seats.size()) {
				this.seats.get(i).update(template.seats.get(i));
			} else {
				this.seats.add(template.seats.get(i));
			}
		}
		this.lockWhenMoving = template.lockWhenMoving;
		this.offset = template.offset;
		this.maxSpeed.update(template.maxSpeed);
		this.maxAcceleration.update(template.maxAcceleration);
	}
}
