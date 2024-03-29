package fr.jarven.transportbukkit.vehicles;

import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.templates.PartTemplate;
import fr.jarven.transportbukkit.utils.LocationRollable;
import fr.jarven.transportbukkit.utils.MovementsVector;

public abstract class VehiclePart {
	private Vehicle vehicle;
	protected final PartTemplate template;
	private final MovementsVector offsetAnimation;

	protected VehiclePart(Vehicle vehicle, PartTemplate properties) {
		this.vehicle = vehicle;
		this.template = properties;
		if (this.template.isAnimated()) {
			this.offsetAnimation = new MovementsVector();
		} else {
			this.offsetAnimation = null;
		}
	}

	protected void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public PartTemplate getTemplate() {
		return template;
	}

	protected abstract void respawn();
	public abstract LocationRollable getLocation();

	public abstract void updateFakeLocation();
	public abstract void updateRealLocation();
	protected abstract void update();
	public abstract UUID getEntityUUID();
	protected abstract void removeInternal();
	public boolean isEntityUUID(UUID uuid) {
		return uuid != null && uuid.equals(getEntityUUID());
	}

	public static VehiclePart createPart(Vehicle vehicle, PartTemplate partTemplate) {
		switch (partTemplate.getType()) {
			case ARMOR_STAND_HEAD:
				return new VehiclePartArmorStandHead(vehicle, partTemplate);
			case CUSTOM_ENTITY:
				return new VehiclePartCustomEntity(vehicle, partTemplate);
			case CUSTOM_ENTITY_ON_ARMOR_STAND:
				return new VehiclePartCustomEntityOnAS(vehicle, partTemplate);
			case UNKNOWN:
				throw new IllegalArgumentException("Unknown part type: " + partTemplate.getType() + " for part " + partTemplate.getName());
		}
		throw new IllegalArgumentException("Unknown part type: " + partTemplate.getType() + " for part " + partTemplate.getName());
	}

	protected void saveConfig(ConfigurationSection section) {
		section.set("template", template.getName());
	}

	protected void loadConfig(ConfigurationSection section) {
		String templateName = section.getString("template");
		if (!templateName.equals(this.template.getName())) {
			TransportPlugin.LOGGER.warning("Part " + this.template.getName() + " has a different template name than the one saved in config. Need restart to change.");
		}
	}

	public MovementsVector getOffsetAnimation() {
		return offsetAnimation;
	}
}