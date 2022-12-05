package fr.jarven.transportbukkit.vehicles;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.util.UUID;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.templates.PartTemplate;
import fr.jarven.transportbukkit.templates.PartTemplate.EntityPropertyType;
import fr.jarven.transportbukkit.utils.LocationRollable;

// An entity riding a small armor stand
public class VehiclePartCustomEntityOnAS extends VehiclePartArmorStandHead {
	private Entity customEntity;
	private UUID customEntityUuid;
	private long lastCustomEntityGetTimestamp = 0;

	protected VehiclePartCustomEntityOnAS(Vehicle vehicle, PartTemplate properties) {
		super(vehicle, properties);
	}

	@Override
	Entity getEntity() {
		return customEntity;
	}

	@Override
	public UUID getEntityUUID() {
		return customEntityUuid;
	}

	Entity getCustomEntityForce() {
		if (customEntityUuid != null && (customEntity == null || !customEntity.isValid())) {
			customEntity = Bukkit.getEntity(customEntityUuid);
		}
		return customEntity;
	}

	@Override
	Entity spawnEntity() {
		ArmorStand entity = (ArmorStand) super.spawnEntity();
		entity.setSmall(true);
		if (customEntityUuid == null && !isCustomEntityValid()) {
			customEntity = template.spawnEntity(getLocation(), getVehicle().getName());
			customEntityUuid = customEntity.getUniqueId();
			if (!template.hasProperty(EntityPropertyType.NAME))
				entity.setCustomName(getVehicle().getName() + " " + template.getName());
			entity.addScoreboardTag("TransportBukkit_Part");
			template.apply(customEntity);
			entity.addPassenger(customEntity);
			getVehicle().makeDirty();
		}
		return entity;
	}

	@Override
	protected void respawn() {
		if (entityUuid != null && customEntityUuid != null) {
			// Load the chunk if needed
			getVehicle().getLocation().getChunk();
			getCustomEntityForce();
			getEntityForce();
			if (getEntity() == null || customEntity == null) {
				if (getEntity() == null) {
					entityUuid = null;
				}
				if (customEntity == null) {
					customEntityUuid = null;
				}
				spawnEntity();
			} else {
				template.apply(customEntity);
				updateRealLocation();
			}
		} else {
			spawnEntity();
		}
	}

	@Override
	public void updateFakeLocation() {
		// Move the vehicle but rotate the custom entity
		if (isEntityValid()) {
			ArmorStand armorStand = (ArmorStand) super.getEntity();
			if (template.getRotationType() == PartTemplate.RotationType.TELEPORT) {
				updateRealLocation();
				return;
			}
			LocationRollable loc = getLocation();

			ProtocolManager protocolManager = TransportPlugin.getProtocolManager();

			PacketContainer fakeTp = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
			fakeTp.getIntegers().write(0, armorStand.getEntityId());
			fakeTp.getDoubles()
				.write(0, loc.getX())
				.write(1, loc.getY())
				.write(2, loc.getZ());
			fakeTp.getBytes()
				.write(0, (byte) (loc.getYaw() * 256 / 360))
				.write(1, (byte) (loc.getPitch() * 256 / 360));

			protocolManager.broadcastServerPacket(fakeTp);
		}

		if (isCustomEntityValid()) {
			if (template.getRotationType() == PartTemplate.RotationType.TELEPORT) {
				updateRealLocation();
				return;
			}
			LocationRollable loc = getLocation();
			float yaw = loc.getYaw();
			float pitch = loc.getPitch();
			customEntity.setRotation(yaw, pitch);
		}
	}

	@Override
	public void updateRealLocation() {
		LocationRollable loc = getLocation();
		if (isEntityValid()) {
			template.teleport(super.getEntity(), loc);
		}
		if (isCustomEntityValid()) {
			customEntity.setRotation(loc.getYaw(), loc.getPitch());
		}
	}

	@Override
	protected void removeInternal() {
		if (getCustomEntityForce() != null) {
			customEntity.remove();
			customEntity = null;
		}
		customEntityUuid = null;
		super.removeInternal();
	}

	@Override
	protected void loadConfig(ConfigurationSection section) {
		super.loadConfig(section);
		try {
			if (section.isString("customEntityUuid") && !section.getString("customEntityUuid").isEmpty()) {
				customEntityUuid = UUID.fromString(section.getString("customEntityUuid"));
			}
		} catch (IllegalArgumentException e) {
			TransportPlugin.LOGGER.warning("Invalid custom entity UUID: " + section.getString("customEntityUuid", ""));
			customEntityUuid = null;
		}
		try {
			if (customEntityUuid != null) {
				customEntity = Bukkit.getEntity(customEntityUuid);
			} else {
				customEntity = null;
			}
		} catch (IllegalArgumentException e) {
			TransportPlugin.LOGGER.warning("Invalid custom entity: " + customEntityUuid);
			customEntity = null;
		}
	}

	boolean isCustomEntityValid() {
		if (customEntity != null && customEntity.isValid()) {
			return true;
		}
		if (customEntityUuid == null) {
			return false;
		}
		long now = System.currentTimeMillis();
		long timeSinceLastGet = now - lastCustomEntityGetTimestamp;
		if (timeSinceLastGet < 5000) {
			return false;
		}
		customEntity = getCustomEntityForce();
		lastCustomEntityGetTimestamp = now;
		if (customEntity == null || !customEntity.isValid()) {
			customEntity = null;
		}
		return customEntity != null;
	}

	@Override
	protected void saveConfig(ConfigurationSection section) {
		super.saveConfig(section);
		section.set("customEntityUuid", customEntityUuid != null ? customEntityUuid.toString() : "");
	}
}