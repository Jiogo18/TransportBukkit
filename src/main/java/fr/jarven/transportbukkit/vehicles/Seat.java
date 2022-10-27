package fr.jarven.transportbukkit.vehicles;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.templates.SeatProperties;
import fr.jarven.transportbukkit.utils.LocationRollable;

public class Seat {
	private final Vehicle vehicle;
	private SeatProperties template;
	private Entity seatEntity;
	private UUID seatEntityUuid;

	protected Seat(Vehicle vehicle, SeatProperties template) {
		this.vehicle = vehicle;
		this.template = template;
	}

	protected void update() {
		if (seatEntity != null && seatEntity.isValid()) {
			updateRealLocation();
		}
	}

	protected void spawn() {
		if (seatEntity == null) {
			if (seatEntityUuid != null) {
				seatEntity = Bukkit.getEntity(seatEntityUuid);
			}
			if (seatEntity == null) {
				seatEntity = template.spawnEntity(getLocation());
				seatEntity.setCustomName(vehicle.getName() + " Seat " + template.getSeatIndex());
				seatEntityUuid = seatEntity.getUniqueId();
				vehicle.makeDirty();
			} else {
				updateRealLocation();
			}
		} else {
			updateRealLocation();
		}
	}

	protected void respawn() {
		if (seatEntity == null || !seatEntity.isValid()) {
			spawn();
		} else {
			updateRealLocation();
		}
	}

	protected void updateRealLocation() {
		if (seatEntity != null && seatEntity.isValid()) {
			template.teleport(seatEntity, getLocation());
		}
	}

	public LocationRollable getLocation() {
		return template.getLocationIfEntity(vehicle.getLocationWithOffset(), null, 1);
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public SeatProperties getTemplate() {
		return template;
	}

	public UUID getEntityUUID() {
		return seatEntityUuid;
	}

	public boolean hasSeat() {
		return seatEntity != null && seatEntity.isValid();
	}

	public boolean addPassenger(Entity passenger) {
		if (!hasSeat()) return false;
		boolean alreadyIn = seatEntity.getPassengers().stream().anyMatch(p -> p.getUniqueId().equals(passenger.getUniqueId()));
		if (hasPassenger() && !alreadyIn) return false;
		if (!this.seatEntity.addPassenger(passenger)) return false;
		TransportPlugin.getVehicleManager().onSeatEnter(passenger, this);
		return true;
	}

	public boolean removePassenger(Entity passenger) {
		if (hasSeat() && this.seatEntity.removePassenger(passenger)) {
			TransportPlugin.getVehicleManager().onSeatExit(passenger);
			return true;
		} else {
			return false;
		}
	}

	public boolean ejectPassenger() {
		if (!hasSeat()) return false;
		List<Entity> passengers = this.seatEntity.getPassengers();
		for (Entity passenger : passengers) {
			TransportPlugin.getVehicleManager().onSeatExit(passenger);
		}
		return this.seatEntity.eject();
	}

	public Optional<Entity> getPassenger() {
		return hasPassenger() ? Optional.of(this.seatEntity.getPassengers().get(0)) : Optional.empty();
	}

	public boolean hasPassenger() {
		return hasSeat() && !this.seatEntity.getPassengers().isEmpty();
	}

	protected void removeInternal() {
		ejectPassenger();
		if (seatEntity != null && seatEntity.isValid()) {
			seatEntity.remove();
		}
		seatEntity = null;
		seatEntityUuid = null;
	}

	protected void saveConfig(ConfigurationSection section) {
		section.set("template", template.getSeatIndex());
		section.set("entityUuid", seatEntityUuid != null ? seatEntityUuid.toString() : "");
	}

	protected void loadConfig(ConfigurationSection section) {
		int templateIndex = section.getInt("template");
		if (templateIndex != this.template.getSeatIndex()) {
			SeatProperties newTemplate = vehicle.getTemplate().getSeat(templateIndex);
			if (newTemplate != null) {
				this.template = newTemplate;
			} else {
				TransportPlugin.getInstance().getLogger().warning("Invalid seat template index " + templateIndex + " for vehicle " + vehicle.getName());
			}
		}
		try {
			if (section.isString("entityUuid") && !section.getString("entityUuid").isEmpty()) {
				seatEntityUuid = UUID.fromString(section.getString("entityUuid"));
				seatEntity = Bukkit.getEntity(seatEntityUuid);
			}
		} catch (IllegalArgumentException e) {
			seatEntityUuid = null;
			seatEntity = null;
		}
	}
}