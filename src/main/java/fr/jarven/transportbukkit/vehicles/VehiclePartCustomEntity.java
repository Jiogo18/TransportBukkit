package fr.jarven.transportbukkit.vehicles;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.templates.PartTemplate;
import fr.jarven.transportbukkit.utils.LocationRollable;

// An entity riding a small armor stand
public class VehiclePartCustomEntity extends VehiclePartEntity {
	private Entity entity;

	protected VehiclePartCustomEntity(Vehicle vehicle, PartTemplate properties) {
		super(vehicle, properties);
	}

	@Override
	Entity getEntity() {
		return entity;
	}

	@Override
	Entity getEntityForce() {
		if (entity == null || !entity.isValid()) {
			entity = Bukkit.getEntity(entityUuid);
		}
		return entity;
	}

	@Override
	Entity spawnEntity() {
		if (entityUuid == null && !isEntityValid()) {
			entity = template.spawnEntity(getLocation(), getVehicle().getName());
		}
		return entity;
	}

	@Override
	public LocationRollable getLocation() {
		return template.getLocationIfEntity(getVehicle().getLocationWithOffset(), getOffsetAnimation());
	}

	@Override
	public void updateFakeLocation() {
		if (isEntityValid()) {
			if (template.getRotationType() == PartTemplate.RotationType.TELEPORT) {
				updateRealLocation();
				return;
			}
			LocationRollable loc = getLocation();
			float yaw = loc.getYaw();
			float pitch = loc.getPitch();

			ProtocolManager protocolManager = TransportPlugin.getProtocolManager();

			PacketContainer fakeTp = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
			fakeTp.getIntegers().write(0, entity.getEntityId());
			fakeTp.getDoubles()
				.write(0, loc.getX())
				.write(1, loc.getY())
				.write(2, loc.getZ());

			if (template.getRotationType() == PartTemplate.RotationType.FAKE_TELEPORT) {
				// Yaw sent by teleport packet
				// Pitch and roll sent by head rotation
				fakeTp.getBytes()
					.write(0, (byte) (yaw * 256.0F / 360.0F))
					.write(1, (byte) (pitch * 256.0F / 360.0F));
			} else {
				throw new IllegalStateException("Invalid rotation type here");
			}

			protocolManager.broadcastServerPacket(fakeTp);
		}
	}

	@Override
	public void updateRealLocation() {
		if (isEntityValid()) {
			LocationRollable loc = getLocation();
			template.teleport(entity, loc);
			entity.setRotation(loc.getYaw(), loc.getPitch());
		}
	}

	protected void removeInternal() {
		if (entity != null) {
			entity.remove();
			entity = null;
		}
		entityUuid = null;
	}

	@Override
	protected void loadConfig(ConfigurationSection section) {
		super.loadConfig(section);
		try {
			if (entityUuid != null) {
				entity = Bukkit.getEntity(entityUuid);
			} else {
				entity = null;
			}
		} catch (IllegalArgumentException e) {
			TransportPlugin.LOGGER.warning("Invalid entity: " + entityUuid);
			entity = null;
		}
	}
}