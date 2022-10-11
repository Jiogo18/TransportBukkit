package fr.jarven.transportbukkit.templates;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.utils.ItemTemplate;
import fr.jarven.transportbukkit.utils.MovementsVector;

public class PartTemplate extends BasePartTemplate {
	private final String name;
	private PartType type;
	private boolean animated;
	private Map<EquipmentSlot, ItemTemplate> inventory = new EnumMap<>(EquipmentSlot.class);
	private RotationType rotationType;

	public enum PartType {
		ARMOR_STAND_HEAD,
		UNKNOWN
	}

	public enum RotationType {
		TELEPORT,
		ROTATE_HEAD,
		ROTATE_HEAD_DELAYED,
	}

	private PartTemplate(String name, PartType type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public PartType getType() {
		return type;
	}

	public RotationType getRotationType() {
		return rotationType;
	}

	protected static PartTemplate fromFile(File file) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		String name = config.getString("name");
		if (name == null) {
			TransportPlugin.LOGGER.warning("Part template " + file.getName() + " has no name");
			return null;
		}
		PartType type = PartType.valueOf(config.getString("type", "UNKNOWN"));
		if (type == null) {
			TransportPlugin.LOGGER.warning("Part template " + file.getName() + " has no type");
			return null;
		}

		PartTemplate partTemplate = new PartTemplate(name, type);

		partTemplate.offset = MovementsVector.fromConfig(config.getConfigurationSection("offset"));
		partTemplate.animated = config.getBoolean("animated", false);
		ItemTemplate.loadSharedItems(partTemplate.inventory, config.getList("inventory"));
		partTemplate.rotationType = RotationType.valueOf(config.getString("rotationType", "TELEPORT"));
		return partTemplate;
	}

	protected void update(PartTemplate other) {
		super.update(other);
		this.animated = other.animated;
		this.inventory = other.inventory;
		this.rotationType = other.rotationType;
	}

	public boolean isAnimated() {
		return animated;
	}

	public void applyInventory(EntityEquipment equipment) {
		for (Map.Entry<EquipmentSlot, ItemTemplate> entry : inventory.entrySet()) {
			equipment.setItem(entry.getKey(), entry.getValue().createItem());
		}
	}
}
