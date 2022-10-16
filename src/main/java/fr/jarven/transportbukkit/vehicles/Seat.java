package fr.jarven.transportbukkit.vehicles;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.templates.SeatProperties;
import fr.jarven.transportbukkit.utils.LocationRollable;

public class Seat {
	private final Vehicle vehicle;
	private SeatProperties template;
	private Entity seatEntity;
	private UUID seatEntityUuid;

	private static final Method[] methods = ((Supplier<Method[]>) () -> {
		try {
			Method getHandle = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftEntity").getDeclaredMethod("getHandle");
			return new Method[] {
				getHandle, getHandle.getReturnType().getDeclaredMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class)};
		} catch (Exception ex) {
			return null;
		}
	}).get();

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

	protected void updateFakeLocation() {
		if (seatEntity != null && seatEntity.isValid()) {
			Location loc = getLocation();
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

	protected void updateRealLocation() {
		if (seatEntity != null && seatEntity.isValid()) {
			Location loc = getLocation();
			if (hasPassenger()) {
				try {
					methods[1].invoke(methods[0].invoke(seatEntity), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
				} catch (Exception ex) {
				}
			} else {
				seatEntity.teleport(loc, TeleportCause.PLUGIN);
			}
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