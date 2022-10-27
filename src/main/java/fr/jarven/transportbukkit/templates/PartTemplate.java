package fr.jarven.transportbukkit.templates;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.utils.ItemTemplate;
import fr.jarven.transportbukkit.utils.LocationRollable;
import fr.jarven.transportbukkit.utils.MovementsVector;

public class PartTemplate extends BasePartTemplate {
	private final String name;
	private PartType type;
	private EntityType entityType;
	private Set<AnimationTemplate> animations;
	private Map<EquipmentSlot, ItemTemplate> inventory = new EnumMap<>(EquipmentSlot.class);
	private RotationType rotationType;
	private EnumMap<EntityPropertyType, Object> entityProperties;

	public enum PartType {
		ARMOR_STAND_HEAD,
		CUSTOM_ENTITY,
		UNKNOWN
	}

	public enum RotationType {
		FAKE_TELEPORT,
		TELEPORT,
		ROTATE_HEAD,
		ROTATE_HEAD_DELAYED,
	}

	public enum EntityPropertyType {
		NO_GRAVITY, // default true
		INVISIBLE, // default true
		NO_AI, // default true
		SIZE,
		HEALTH,
		NECK_HEIGH,
		NAME,
		COLLISION,
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

	public EntityType getEntityType() {
		return entityType;
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

		if (type == PartType.CUSTOM_ENTITY) {
			EntityType entityType = EntityType.valueOf(config.getString("entityType", "UNKNOWN"));
			if (entityType == null) {
				TransportPlugin.LOGGER.warning("Part template " + file.getName() + " has no entityType");
				return null;
			}
			partTemplate.entityType = entityType;
		}

		partTemplate.offset = MovementsVector.fromConfig(config.getConfigurationSection("offset"));
		if (config.contains("inventory")) {
			ItemTemplate.loadSharedItems(partTemplate.inventory, config.getList("inventory"));
		}
		partTemplate.rotationType = RotationType.valueOf(config.getString("rotationType", "TELEPORT"));
		if (config.contains("animation")) {
			partTemplate.animations = AnimationTemplate.loadAnimations(config.getConfigurationSection("animation"));
		} else {
			partTemplate.animations = null;
		}

		partTemplate.entityProperties = new EnumMap<>(EntityPropertyType.class);
		if (config.contains("entityProperties")) {
			for (String key : config.getConfigurationSection("entityProperties").getKeys(false)) {
				EntityPropertyType propType = EntityPropertyType.valueOf(key);
				if (propType == null) {
					TransportPlugin.LOGGER.warning("Part template " + file.getName() + " has unknown entity property " + key);
					continue;
				}
				partTemplate.entityProperties.put(propType, config.get("entityProperties." + key));
			}
		}

		return partTemplate;
	}

	protected void update(PartTemplate other) {
		super.update(other);
		this.type = other.type;
		this.entityType = other.entityType;
		this.animations = other.animations;
		this.inventory = other.inventory;
		this.rotationType = other.rotationType;
		this.entityProperties = other.entityProperties;
	}

	public boolean isAnimated() {
		return animations != null && !animations.isEmpty();
	}

	public LocationRollable getLocationIfEntity(LocationRollable location, MovementsVector animationOffset) {
		return getLocationIfEntity(location, animationOffset, getDouble(entityProperties.getOrDefault(EntityPropertyType.NECK_HEIGH, 0)));
	}

	public Entity spawnEntity(LocationRollable location) {
		switch (type) {
			case ARMOR_STAND_HEAD:
				return spawnArmorStand(location);
			case CUSTOM_ENTITY:
				return spawnEntity(location, entityType);
			case UNKNOWN:
				return null;
		}
		throw new IllegalStateException("Unknown part type " + type);
	}

	public void applyInventory(EntityEquipment equipment) {
		for (Map.Entry<EquipmentSlot, ItemTemplate> entry : inventory.entrySet()) {
			equipment.setItem(entry.getKey(), entry.getValue().createItem());
		}
	}

	public Set<AnimationTemplate> getAnimations() {
		return animations;
	}

	public void apply(Entity entity) {
		if (entity instanceof LivingEntity) {
			applyInventory(((LivingEntity) entity).getEquipment());
		}

		for (Map.Entry<EntityPropertyType, Object> entry : entityProperties.entrySet()) {
			applyProperty(entity, entry.getKey(), entry.getValue());
		}
	}

	private static double getDouble(Object object) {
		if (object instanceof Integer) {
			return (Integer) object;
		} else if (object instanceof Double) {
			return (Double) object;
		} else {
			return 0;
		}
	}

	@java.lang.SuppressWarnings("java:S3776")
	private void applyProperty(Entity entity, EntityPropertyType property, Object value) {
		switch (property) {
			case NO_GRAVITY:
				entity.setGravity(!(boolean) value);
				break;
			case INVISIBLE:
				if (entity instanceof LivingEntity) {
					((LivingEntity) entity).setInvisible((boolean) value);
				}
				break;
			case NO_AI:
				if (entity instanceof LivingEntity) {
					((LivingEntity) entity).setAI(!(boolean) value);
				}
				break;
			case HEALTH:
				if (entity instanceof LivingEntity) {
					double health = getDouble(value);
					if (health < 0 || 1024 < health) {
						throw new IllegalArgumentException("Health must be between 0 and 1024");
					}
					LivingEntity living = ((LivingEntity) entity);
					AttributeInstance attribute = living.getAttribute(Attribute.GENERIC_MAX_HEALTH);
					if (attribute.getValue() < health)
						attribute.setBaseValue(health);
					living.setHealth(health);
				}
				break;
			case SIZE:
				if (entity instanceof Slime) {
					((Slime) entity).setSize((int) value);
				}
				break;
			case NECK_HEIGH:
				break;
			case NAME:
				if (entity instanceof LivingEntity) {
					((LivingEntity) entity).setCustomName((String) value);
				}
				break;
			case COLLISION:
				if (entity instanceof LivingEntity) {
					((LivingEntity) entity).setCollidable((boolean) value);
				}
				break;
		}
	}

	public boolean hasProperty(EntityPropertyType propType) {
		return entityProperties.containsKey(propType);
	}
}
