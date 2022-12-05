package fr.jarven.transportbukkit.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.Calendar;
import java.util.Date;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.RotationArgument;
import dev.jorel.commandapi.wrappers.Rotation;
import fr.jarven.transportbukkit.commands.arguments.PartArgument;
import fr.jarven.transportbukkit.commands.arguments.PartTemplateArgument;
import fr.jarven.transportbukkit.commands.arguments.VehicleArgument;
import fr.jarven.transportbukkit.commands.arguments.VehicleTemplateArgument;
import fr.jarven.transportbukkit.utils.LocationRollable;
import fr.jarven.transportbukkit.utils.Messages.Resources;
import fr.jarven.transportbukkit.utils.MovementsConstraints;
import fr.jarven.transportbukkit.utils.MovementsVector;
import fr.jarven.transportbukkit.utils.TriFunction;

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

	public static ArgumentTree locationRollableArgument(int argIndex, TriFunction<CommandSender, Object[], LocationRollable, Integer> callback) {
		return new LocationArgument("destination")
			.executes((sender, args) -> {
				LocationRollable loc = new LocationRollable((Location) args[argIndex]);
				if (sender instanceof LivingEntity) {
					loc.setYaw(((LivingEntity) sender).getLocation().getYaw());
					loc.setPitch(((LivingEntity) sender).getLocation().getPitch());
				}
				return callback.apply(sender, args, loc);
			})
			.then(new RotationArgument("rotation")
					.executes((sender, args) -> {
						LocationRollable loc = new LocationRollable((Location) args[argIndex], (Rotation) args[argIndex + 1]);
						return callback.apply(sender, args, loc);
					})
					.then(new FloatArgument("roll")
							.executes((sender, args) -> {
								LocationRollable loc = new LocationRollable((Location) args[argIndex], (Rotation) args[argIndex + 1]);
								loc.setRoll((float) args[3]);
								return callback.apply(sender, args, loc);
							})));
	}

	public static String addDigits(int value, int digits) {
		return StringUtils.leftPad(String.valueOf(value), digits, '0');
	}

	public static String toReadbleString(Location location) {
		if (location == null) return "null";
		return String.format("(%.2f ; %.2f ; %.2f) in %s",
			location.getX(),
			location.getY(),
			location.getZ(),
			location.getWorld().getName());
	}

	public static String toReadbleString(LocationRollable location) {
		if (location == null) return "null";
		return String.format("(%7.2f,%7.2f,%7.2f ;%4.0f,%4.0f,%4.0f) in %s",
			location.getX(),
			location.getY(),
			location.getZ(),
			location.getYaw(),
			location.getPitch(),
			location.getRoll(),
			location.getWorld().getName());
	}

	public static String toReadbleString(double number) {
		return String.format("%9.2E", number);
	}

	public static ChatColor getSpeedColor(double speed) {
		if (speed < 0.001) return ChatColor.GRAY;
		if (speed < 0.01) return ChatColor.GREEN;
		if (speed < 0.1) return ChatColor.DARK_GREEN;
		if (speed < 0.3) return ChatColor.YELLOW;
		if (speed < 0.5) return ChatColor.GOLD;
		if (speed < 1) return ChatColor.RED;
		return ChatColor.DARK_RED;
	}

	public static String toReadbleStringWithColor(double number, double colorFactor) {
		return getSpeedColor(Math.abs(number * colorFactor)) + String.format("%9.2E", number) + ChatColor.RESET;
	}

	public static String toReadbleString(MovementsVector vector) {
		if (vector == null) return "null";
		return String.format("(%s,%s,%s ; %s,%s,%s)",
			toReadbleStringWithColor(vector.getForwardBackward(), 1),
			toReadbleStringWithColor(vector.getLeftRight(), 1),
			toReadbleStringWithColor(vector.getUpDown(), 1),
			toReadbleStringWithColor(vector.getYaw(), 0.2),
			toReadbleStringWithColor(vector.getPitch(), 0.2),
			toReadbleStringWithColor(vector.getRoll(), 0.2));
	}

	public static String toReadbleString(Vector vector) {
		if (vector == null) return "null";
		return String.format("(%s,%s,%s)",
			toReadbleStringWithColor(vector.getX(), 1),
			toReadbleStringWithColor(vector.getY(), 1),
			toReadbleStringWithColor(vector.getZ(), 1));
	}

	public static String toReadbleString(MovementsConstraints vector) {
		if (vector == null) return "null";
		return String.format("(%9.2E/%9.2E ; %9.2E/%9.2E ; %9.2E/%9.2E)",
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
