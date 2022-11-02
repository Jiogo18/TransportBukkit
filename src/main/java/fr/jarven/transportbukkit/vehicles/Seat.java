package fr.jarven.transportbukkit.vehicles;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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
	private long lastEntityGetTimestamp = 0;

	protected Seat(Vehicle vehicle, SeatProperties template) {
		this.vehicle = vehicle;
		this.template = template;
	}

	protected void spawn() {
		if (seatEntity == null || !seatEntity.isValid()) {
			if (seatEntityUuid != null) {
				retrieveSeatEntity();
			} else {
				seatEntity = template.spawnEntity(getLocation(), vehicle.getName());
				seatEntityUuid = seatEntity.getUniqueId();
				vehicle.makeDirty();
			}
		}
		updateRealLocation();
	}

	protected void respawn() {
		if (seatEntity == null || !seatEntity.isValid() && seatEntityUuid != null) {
			retrieveSeatEntity();
			if (seatEntity == null) {
				seatEntityUuid = null;
			}
		}
		spawn();
	}

	protected void updateRealLocation() {
		if (hasSeatLazy()) {
			template.teleport(seatEntity, getLocation());
			updatePassengerRotation();
		}
	}

	private void updatePassengerRotation() {
		LocationRollable location = getLocation();
		getPassenger().ifPresent(entity -> {
			if (!(entity instanceof Player)) {
				entity.setRotation(location.getYaw(), entity.getLocation().getPitch());
			} else if (entity.hasMetadata("NPC")) {
				ProtocolManager protocolManager = TransportPlugin.getProtocolManager();
				PacketContainer fakeRotation = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
				fakeRotation.getIntegers().write(0, entity.getEntityId());
				fakeRotation.getBytes().write(0, (byte) (location.getYaw() * 256 / 360));
				protocolManager.broadcastServerPacket(fakeRotation);
			}
		});
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

	private void retrieveSeatEntity() {
		if (seatEntityUuid != null) {
			seatEntity = Bukkit.getEntity(seatEntityUuid);
			if (seatEntity != null && seatEntity.isValid()) {
				updateRealLocation();
			}
		}
	}

	public boolean hasSeat() {
		if (seatEntity != null && seatEntity.isValid()) {
			return true;
		} else if (seatEntityUuid != null) {
			retrieveSeatEntity();
			return seatEntity != null && seatEntity.isValid();
		} else {
			return false;
		}
	}

	private boolean hasSeatLazy() {
		if (seatEntity != null && seatEntity.isValid()) {
			return true;
		} else if (seatEntityUuid != null) {
			long now = System.currentTimeMillis();
			long timeSinceLastGet = now - lastEntityGetTimestamp;
			if (timeSinceLastGet < 5000) {
				return false;
			}
			retrieveSeatEntity();
			return seatEntity != null && seatEntity.isValid();
		} else {
			return false;
		}
	}

	public boolean addPassenger(Entity passenger) {
		if (!hasSeat()) return false;
		if (passenger.getScoreboardTags().contains("TransportBukkit_Entity")) return false;
		boolean alreadyIn = seatEntity.getPassengers().stream().anyMatch(p -> p.getUniqueId().equals(passenger.getUniqueId()));
		if (hasPassenger() && !alreadyIn) return false;
		if (!this.seatEntity.addPassenger(passenger)) return false;
		TransportPlugin.getVehicleManager().onSeatEnter(passenger, this);
		updatePassengerRotation();
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
		if (hasSeat()) {
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
			if (section.isString("entityUuid")) {
				String uuidString = section.getString("entityUuid");
				if (!uuidString.isEmpty()) {
					seatEntityUuid = UUID.fromString(uuidString);
					retrieveSeatEntity();
				}
			}
		} catch (IllegalArgumentException e) {
			seatEntityUuid = null;
			seatEntity = null;
		}
	}
}