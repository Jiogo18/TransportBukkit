package fr.jarven.transportbukkit.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Calendar;
import java.util.Date;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.transportbukkit.commands.arguments.PartArgument;
import fr.jarven.transportbukkit.commands.arguments.PartTemplateArgument;
import fr.jarven.transportbukkit.commands.arguments.VehicleArgument;
import fr.jarven.transportbukkit.commands.arguments.VehicleTemplateArgument;
import fr.jarven.transportbukkit.utils.Messages.Resources;
import fr.jarven.transportbukkit.utils.MovementsConstraints;
import fr.jarven.transportbukkit.utils.MovementsVector;

public class CommandTools {
	protected CommandTools() {}

	public static LiteralArgument literal(String name) {
		return new LiteralArgument(name);
	}

	public static PartTemplateArgument partTemplateArgument(String name) {
		return new PartTemplateArgument(name);
	}

	public static VehicleTemplateArgument vehicleTemplateArgument(String name) {
		return new VehicleTemplateArgument(name);
	}

	public static VehicleArgument vehicleArgument(String name) {
		return new VehicleArgument(name);
	}

	public static PartArgument partArgument(String name) {
		return new PartArgument(name);
	}

	public static double round(double value, int decimals) {
		double factor = Math.pow(10, decimals);
		return Math.round(value * factor) / factor;
	}

	public static String addDigits(int value, int digits) {
		return StringUtils.leftPad(String.valueOf(value), digits, '0');
	}

	public static String toReadbleString(Location location) {
		if (location == null) return "null";
		return String.format("(%s ; %s ; %s) in %s",
			round(location.getX(), 3),
			round(location.getY(), 3),
			round(location.getZ(), 3),
			location.getWorld().getName());
	}

	public static String toReadbleString(MovementsVector vector) {
		if (vector == null) return "null";
		return String.format("(%s,%s,%s ; %s,%s,%s)",
			vector.getForwardBackward(),
			vector.getLeftRight(),
			vector.getUpDown(),
			vector.getYaw(),
			vector.getPitch(),
			vector.getRoll());
	}

	public static String toReadbleString(MovementsConstraints vector) {
		if (vector == null) return "null";
		return String.format("(%s/%s ; %s/%s ; %s/%s)",
			vector.getForward(),
			-vector.getBackward(),
			vector.getLeft(),
			-vector.getRight(),
			vector.getUp(),
			-vector.getDown());
	}
	public static String toReadbleString(Date date, CommandSender sender) {
		if (date == null) return "null";
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.get(Calendar.YEAR);
		return Resources.DATE_FORMAT
			.replace("yyyy", String.valueOf(calendar.get(Calendar.YEAR)))
			.replace("MM", addDigits(calendar.get(Calendar.MONTH), 2))
			.replace("dd", addDigits(calendar.get(Calendar.DAY_OF_MONTH), 2))
			.replace("HH", addDigits(calendar.get(Calendar.HOUR_OF_DAY), 2))
			.replace("mm", addDigits(calendar.get(Calendar.MINUTE), 2))
			.replace("ss", addDigits(calendar.get(Calendar.SECOND), 2))
			.build(sender);
	}
}
