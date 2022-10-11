package fr.jarven.transportbukkit.utils;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * An item template defines an item created for armor stands.
 * It stores the informations of the config file.
 */
public class ItemTemplate implements ConfigurationSerializable {
	private final EquipmentSlot slot;
	private final Material material;
	private final int customModelData;

	public ItemTemplate(EquipmentSlot slot, Material material, int customModelData) {
		this.slot = slot;
		this.material = material;
		this.customModelData = customModelData;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new java.util.HashMap<>();
		map.put("slot", slot.name());
		map.put("material", material.name());
		map.put("custom_model_data", customModelData);
		return map;
	}

	public static ItemTemplate deserialize(Map<String, Object> map) {
		EquipmentSlot slot = EquipmentSlot.valueOf((String) map.get("slot"));
		Material material = Material.valueOf((String) map.get("material"));
		int customModelData = (int) map.getOrDefault("custom_model_data", 0);
		return new ItemTemplate(slot, material, customModelData);
	}

	public static void loadSharedItems(Map<EquipmentSlot, ItemTemplate> items, List<?> objects) {
		for (Object object : objects) {
			if (object instanceof ItemTemplate) {
				ItemTemplate item = (ItemTemplate) object;
				items.put(item.slot, item);
			} else if (object instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) object;
				ItemTemplate item = deserialize(map);
				items.put(item.slot, item);
			}
		}
	}

	public ItemStack createItem() {
		ItemStack item = new ItemStack(material);
		item.setAmount(1);
		if (customModelData != 0) {
			ItemMeta meta = item.getItemMeta();
			meta.setCustomModelData(customModelData);
			item.setItemMeta(meta);
		}
		return item;
	}

	public static ItemStack createItem(ItemTemplate shared) {
		return shared == null ? null : shared.createItem();
	}

	public static void createArmor(EntityEquipment equipment, ItemTemplate[] items) {
		equipment.setHelmet(createItem(items[0]));
		equipment.setChestplate(createItem(items[1]));
		equipment.setLeggings(createItem(items[2]));
		equipment.setBoots(createItem(items[3]));
		if (items.length > 4) equipment.setItemInMainHand(createItem(items[4]));
		if (items.length > 5) equipment.setItemInOffHand(createItem(items[5]));
	}
}