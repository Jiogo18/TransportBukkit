package fr.jarven.transportbukkit.vehicles;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.UUID;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.templates.PartTemplate;
import fr.jarven.transportbukkit.templates.PartTemplate.EntityPropertyType;
import fr.jarven.transportbukkit.utils.LocationRollable;

public abstract class VehiclePartEntity extends VehiclePart {
	UUID entityUuid;
	private long lastEntityGetTimestamp = 0;

	protected VehiclePartEntity(Vehicle vehicle, PartTemplate properties) {
		super(vehicle, properties);
	}

	abstract Entity getEntity();
	abstract Entity getEntityForce();
	abstract Entity spawnEntity();

	void spawn() {
		if (entityUuid == null) {
			Entity entity = spawnEntity();
			if (entity == null) return;
			if (!template.hasProperty(EntityPropertyType.NAME))
				entity.setCustomName(getVehicle().getName() + " " + template.getName());
			entity.addScoreboardTag("TransportBukkit_Part");
			entityUuid = entity.getUniqueId();

			template.apply(entity);
			getVehicle().makeDirty();
		}
	}

	@Override
	protected void respawn() {
		if (entityUuid != null) {
			// Load the chunk if needed
			getVehicle().getLocation().getChunk();
			Entity entity = getEntityForce();
			if (entity == null) {
				entityUuid = null;
				spawn();
			} else {
				template.apply(entity);
				updateRealLocation();
			}
		} else {
			spawn();
		}
	}

	boolean isEntityValid() {
		Entity entity = getEntity();
		if (entity != null && entity.isValid()) {
			return true;
		}
		if (entityUuid == null) {
			return false;
		}
		long now = System.currentTimeMillis();
		long timeSinceLastGet = now - lastEntityGetTimestamp;
		if (timeSinceLastGet < 5000) {
			return false;
		}
		entity = getEntityForce();
		lastEntityGetTimestamp = now;
		if (entity == null || !entity.isValid()) {
			entity = null;
		}
		return entity != null;
	}

	public void updateFakeLocation() {
		if (isEntityValid()) {
			Entity entity = getEntity();

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
				throw new IllegalStateException("This part can only have the type TELEPORT or FAKE_TELEPORT");
			}

			protocolManager.broadcastServerPacket(fakeTp);
		}
	}

	@Override
	public void updateRealLocation() {
		if (isEntityValid()) {
			Entity entity = getEntity();
			LocationRollable loc = getLocation();
			template.teleport(entity, loc);
			entity.setRotation(loc.getYaw(), loc.getPitch());
		}
	}

	protected void update() {
		if (entityUuid != null) {
			Entity entity = getEntityForce();
			if (entity != null) {
				template.apply(entity);
				updateRealLocation();
			}
		}
		if (template.isAnimated()) {
			TransportPlugin.getAnimationManager().updateAnimation(this);
		}
	}

	public UUID getEntityUUID() {
		return entityUuid;
	}

	@Override
	protected void saveConfig(ConfigurationSection section) {
		super.saveConfig(section);
		section.set("entityUuid", entityUuid != null ? entityUuid.toString() : "");
	}

	@Override
	protected void loadConfig(ConfigurationSection section) {
		super.loadConfig(section);
		try {
			if (section.isString("entityUuid") && !section.getString("entityUuid").isEmpty()) {
				entityUuid = UUID.fromString(section.getString("entityUuid"));
			}
		} catch (IllegalArgumentException e) {
			TransportPlugin.LOGGER.warning("Invalid entity UUID: " + section.getString("entityUuid", ""));
			entityUuid = null;
		}
	}
}