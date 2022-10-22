package fr.jarven.transportbukkit.vehicles;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;

import java.util.UUID;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.templates.PartTemplate;
import fr.jarven.transportbukkit.utils.LocationRollable;

public class VehiclePartArmorStandHead extends VehiclePart {
	private static final String NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(23);
	private static final int NMS_NUMBER_VERSION = Integer.parseInt(NMS_VERSION.split("_")[1]);
	private static final int NMS_ARMOR_STAND_ROTATE_PACKET_ID = NMS_NUMBER_VERSION >= 17 ? 16 : 15;
	private ArmorStand entity;
	private UUID entityUuid;
	private long lastEntityGetTimestamp = 0;

	protected VehiclePartArmorStandHead(Vehicle vehicle, PartTemplate properties) {
		super(vehicle, properties);
	}

	void equipEntity() {
		if (entity == null || !entity.isValid()) return;
		template.applyInventory(entity.getEquipment());
	}

	void spawn() {
		if (entity == null && entityUuid == null) {
			entity = template.spawnArmorStand(getLocation());
			entity.setCustomName(getVehicle().getName() + " " + template.getName());
			entity.addScoreboardTag("TransportBukkit_Part");
			entityUuid = entity.getUniqueId();

			equipEntity();
			getVehicle().makeDirty();
		}
	}

	@Override
	protected void respawn() {
		if (entityUuid != null) {
			// Load the chunk if needed
			getVehicle().getLocation().getChunk();
			entity = (ArmorStand) Bukkit.getEntity(entityUuid);
			if (entity == null) {
				entityUuid = null;
				spawn();
			} else {
				equipEntity();
				updateRealLocation();
			}
		} else {
			spawn();
		}
	}

	private boolean isEntityValid() {
		if (entity != null && entity.isValid()) {
			return true;
		}
		if (entityUuid == null) {
			spawn();
			return true;
		}
		long now = System.currentTimeMillis();
		long timeSinceLastGet = now - lastEntityGetTimestamp;
		if (timeSinceLastGet < 5000) {
			return false;
		}
		entity = (ArmorStand) Bukkit.getEntity(entityUuid);
		lastEntityGetTimestamp = now;
		if (entity == null || !entity.isValid()) {
			entity = null;
		}
		return entity != null;
	}

	@Override
	public LocationRollable getLocation() {
		return template.getLocationIfEntity(getVehicle().getLocationWithOffset(), getOffsetAnimation(), 1.4);
	}

	public void updateFakeLocation() {
		if (isEntityValid()) {
			if (template.getRotationType() == PartTemplate.RotationType.TELEPORT) {
				updateRealLocation();
				return;
			}
			LocationRollable loc = getLocation();
			float yaw = loc.getYaw();
			float pitch = loc.getPitch();
			float roll = loc.getRoll();

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
					.write(1, (byte) 0);

				entity.setHeadPose(entity.getHeadPose()
							   .setX(Math.toRadians(-pitch))
							   .setY(Math.toRadians(0))
							   .setZ(Math.toRadians(roll)));
			} else {
				// Yaw, pitch and roll sent by head rotation
				final PacketContainer packetRotate = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
				packetRotate.getModifier().writeDefaults();
				packetRotate.getIntegers().write(0, entity.getEntityId());
				WrappedDataWatcher dataWatcher = new WrappedDataWatcher(packetRotate.getWatchableCollectionModifier().read(0));
				dataWatcher.setObject(NMS_ARMOR_STAND_ROTATE_PACKET_ID, Registry.getVectorSerializer(), new Vector3F(-pitch, yaw, roll));
				packetRotate.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());

				fakeTp.getBytes()
					.write(0, (byte) 0)
					.write(1, (byte) 0);

				if (template.getRotationType() == PartTemplate.RotationType.ROTATE_HEAD_DELAYED) {
					// Send the packet differed
					Bukkit.getScheduler().runTaskLater(TransportPlugin.getInstance(), () -> {
						entity.setHeadPose(entity.getHeadPose()
									   .setX(Math.toRadians(-pitch))
									   .setY(Math.toRadians(yaw))
									   .setZ(Math.toRadians(roll)));

						protocolManager.broadcastServerPacket(packetRotate);
					}, 2L);
				} else {
					entity.setHeadPose(entity.getHeadPose()
								   .setX(Math.toRadians(-pitch))
								   .setY(Math.toRadians(yaw))
								   .setZ(Math.toRadians(roll)));

					protocolManager.broadcastServerPacket(packetRotate);
				}
			}

			protocolManager.broadcastServerPacket(fakeTp);
		}
	}

	@Override
	public void updateRealLocation() {
		if (isEntityValid()) {
			LocationRollable loc = getLocation();
			float yaw = loc.getYaw();
			float pitch = loc.getPitch();
			float roll = loc.getRoll();

			if (template.getRotationType() == PartTemplate.RotationType.TELEPORT || template.getRotationType() == PartTemplate.RotationType.FAKE_TELEPORT) {
				// Yaw sent by teleport packet
				// Pitch and roll sent by head rotation
				loc.setPitch(0);
				entity.setHeadPose(entity.getHeadPose()
							   .setX(Math.toRadians(-pitch))
							   .setY(Math.toRadians(0))
							   .setZ(Math.toRadians(roll)));
			} else {
				// Yaw, pitch and roll sent by head rotation
				loc.setYaw(0);
				loc.setPitch(0);

				entity.setHeadPose(entity.getHeadPose()
							   .setX(Math.toRadians(-pitch))
							   .setY(Math.toRadians(yaw))
							   .setZ(Math.toRadians(roll)));
			}

			template.teleport(entity, loc);
		}
	}

	protected void update() {
		if (entityUuid != null) {
			entity = (ArmorStand) Bukkit.getEntity(entityUuid);
			if (entity != null) {
				equipEntity();
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

	protected void removeInternal() {
		if (entity != null) {
			entity.remove();
			entity = null;
		}
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
				entity = (ArmorStand) Bukkit.getEntity(entityUuid);
			}
		} catch (IllegalArgumentException e) {
			TransportPlugin.LOGGER.warning("Invalid entity UUID: " + section.getString("entityUuid", ""));
			entityUuid = null;
			entity = null;
		}
	}
}