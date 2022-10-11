package fr.jarven.transportbukkit.vehicles;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

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
		if (seatEntity == null || !seatEntity.isValid()) {
			spawn();
		} else {
			updateLocation();
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
				seatEntity.addScoreboardTag("TransportBukkit_Seat");
				seatEntityUuid = seatEntity.getUniqueId();
				vehicle.makeDirty();
			}
		} else {
			updateLocation();
		}
	}

	protected void updateLocation() {
		if (seatEntity != null && seatEntity.isValid()) {
			Location loc = getLocation();
			seatEntity.teleport(loc, TeleportCause.PLUGIN);

			PacketContainer fakeTp = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
			fakeTp.getIntegers().write(0, seatEntity.getEntityId());
			fakeTp.getDoubles()
				.write(0, loc.getX())
				.write(1, loc.getY())
				.write(2, loc.getZ());
			fakeTp.getBytes()
				.write(0, (byte) (loc.getYaw() * 256.0F / 360.0F))
				.write(1, (byte) (loc.getPitch() * 256.0F / 360.0F));
			ProtocolManager protocolManager = TransportPlugin.getProtocolManager();
			protocolManager.broadcastServerPacket(fakeTp);
		}
	}

	public LocationRollable getLocation() {
		return template.getLocationIfEntity(vehicle.getLocation(), 1);
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
		return hasSeat() && !hasPassenger() && this.seatEntity.addPassenger(passenger);
	}

	public boolean removePassenger(Entity passenger) {
		return hasSeat() && this.seatEntity.removePassenger(passenger);
	}

	public boolean ejectPassenger() {
		return hasSeat() && this.seatEntity.eject();
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