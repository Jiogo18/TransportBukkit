package fr.jarven.transportbukkit.commands.transport;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import fr.jarven.transportbukkit.TransportPlugin;
import fr.jarven.transportbukkit.commands.CommandTools;
import fr.jarven.transportbukkit.templates.PartTemplate;
import fr.jarven.transportbukkit.templates.VehicleTemplate;
import fr.jarven.transportbukkit.utils.Messages.Resources;
import fr.jarven.transportbukkit.vehicles.Vehicle;

public class CommandTransportList extends CommandTools {
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("list")
			.then(literal("vehicles").executes(CommandTransportList::listVehicles))
			.then(literal("vehicle_templates").executes(CommandTransportList::listVehicleTemplates))
			.then(literal("part_templates").executes(CommandTransportList::listPartTemplates));
	}

	private static int listVehicles(CommandSender sender, CommandArguments args) {
		List<String> vehiclesName = TransportPlugin.getVehicleManager().getVehicles().stream().map(Vehicle::getName).collect(Collectors.toList());
		String vehicleList = String.join(", ", vehiclesName);
		Resources.VEHICLE_LIST
			.replace("%vehicles%", vehicleList)
			.send(sender);
		return vehiclesName.size();
	}

	private static int listVehicleTemplates(CommandSender sender, CommandArguments args) {
		List<String> templatesName = TransportPlugin.getTemplateManager().getVehicleTemplates().stream().map(VehicleTemplate::getName).collect(Collectors.toList());
		String templateList = String.join(", ", templatesName);
		Resources.TEMPLATE_VEHICLE_LIST
			.replace("%vehicle_templates%", templateList)
			.send(sender);
		return templatesName.size();
	}

	private static int listPartTemplates(CommandSender sender, CommandArguments args) {
		List<String> templatesName = TransportPlugin.getTemplateManager().getPartTemplates().stream().map(PartTemplate::getName).collect(Collectors.toList());
		String templateList = String.join(", ", templatesName);
		Resources.TEMPLATE_PART_LIST
			.replace("%part_templates%", templateList)
			.send(sender);
		return templatesName.size();
	}
}
