package fr.jarven.transportbukkit.templates;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.jarven.transportbukkit.TransportPlugin;

public class TemplateManager {
	final Map<String, PartTemplate> partsTemplates = new HashMap<>();
	final Map<String, VehicleTemplate> vehiclesTemplates = new HashMap<>();

	// Get template folder
	private File getTemplateFolder() {
		return new File(TransportPlugin.getInstance().getDataFolder(), "templates");
	}

	public File getTemplatePartFolder() {
		return new File(getTemplateFolder(), "parts");
	}

	public File getTemplateVehicleFolder() {
		return new File(getTemplateFolder(), "vehicles");
	}

	public Optional<PartTemplate> getPartTemplate(String name) {
		return Optional.ofNullable(partsTemplates.get(name));
	}

	public Collection<PartTemplate> getPartTemplates() {
		return partsTemplates.values();
	}

	public Optional<VehicleTemplate> getVehicleTemplate(String name) {
		return Optional.ofNullable(vehiclesTemplates.get(name));
	}

	public Collection<VehicleTemplate> getVehicleTemplates() {
		return vehiclesTemplates.values();
	}

	public void reload() {
		createTemplatesFolder();
		loadPartsTemplates();
		loadVehiclesTemplates();
	}

	private void createTemplatesFolder() {
		FileConfiguration config = TransportPlugin.getInstance().getConfig();
		boolean saveDefaultTemplates = config.getBoolean("save_default_templates", false);
		if (!getTemplatePartFolder().exists()) {
			getTemplatePartFolder().mkdirs();
		}
		if (!getTemplateVehicleFolder().exists()) {
			getTemplateVehicleFolder().mkdirs();
		}
		if (saveDefaultTemplates) {
			saveDefaultTemplates();
		}
	}

	private void loadPartsTemplates() {
		Map<String, PartTemplate> newPartsTemplates = new HashMap<>();
		String[] newPartsFiles = getTemplatePartFolder().list((dir, name) -> name.endsWith(".yml"));
		for (String fileName : newPartsFiles) {
			PartTemplate template = PartTemplate.fromFile(new File(getTemplatePartFolder(), fileName));
			if (template != null) {
				newPartsTemplates.put(template.getName(), template);
			}
		}
		// Remove old templates
		for (String templateName : partsTemplates.keySet()) {
			if (!newPartsTemplates.containsKey(templateName)) {
				partsTemplates.remove(templateName);
			}
		}
		// Add new templates and update old ones
		for (PartTemplate template : newPartsTemplates.values()) {
			if (partsTemplates.containsKey(template.getName())) {
				partsTemplates.get(template.getName()).update(template);
			} else {
				partsTemplates.put(template.getName(), template);
			}
		}

		if (partsTemplates.size() > 1) {
			TransportPlugin.LOGGER.info("Loaded " + partsTemplates.size() + " part templates");
		} else {
			TransportPlugin.LOGGER.info("Loaded " + partsTemplates.size() + " part template");
		}
	}

	private void loadVehiclesTemplates() {
		Map<String, VehicleTemplate> newVehiclesTemplates = new HashMap<>();
		String[] newVehiclesFiles = getTemplateVehicleFolder().list((dir, name) -> name.endsWith(".yml"));
		for (String fileName : newVehiclesFiles) {
			VehicleTemplate template = VehicleTemplate.fromFile(new File(getTemplateVehicleFolder(), fileName));
			if (template != null) {
				newVehiclesTemplates.put(template.getName(), template);
			}
		}
		// Remove old templates
		for (String templateName : vehiclesTemplates.keySet().toArray(new String[0])) {
			if (!newVehiclesTemplates.containsKey(templateName)) {
				vehiclesTemplates.remove(templateName);
			}
		}
		// Add new templates and update old ones
		for (VehicleTemplate template : newVehiclesTemplates.values()) {
			if (vehiclesTemplates.containsKey(template.getName())) {
				vehiclesTemplates.get(template.getName()).update(template);
			} else {
				vehiclesTemplates.put(template.getName(), template);
			}
		}

		if (vehiclesTemplates.size() > 1) {
			TransportPlugin.LOGGER.info("Loaded " + vehiclesTemplates.size() + " vehicle templates");
		} else {
			TransportPlugin.LOGGER.info("Loaded " + vehiclesTemplates.size() + " vehicle template");
		}
	}

	private void saveDefaultTemplates() {
		TransportPlugin.getInstance().saveResource("templates/vehicles/boat.yml", true);
		TransportPlugin.getInstance().saveResource("templates/parts/boat_front.yml", true);
		TransportPlugin.getInstance().saveResource("templates/parts/boat_back.yml", true);
		TransportPlugin.getInstance().saveResource("templates/parts/boat_motor.yml", true);
	}
}
