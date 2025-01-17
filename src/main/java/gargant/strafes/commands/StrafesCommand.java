package gargant.strafes.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import gargant.strafes.classes.Items;
import gargant.strafes.containers.CooldownsContainer;
import gargant.strafes.containers.VelocityContainer;
import gargant.strafes.services.DatabaseService;
import gargant.strafes.services.DatabaseService.DatabaseType;
import masecla.mlib.annotations.RegisterableInfo;
import masecla.mlib.annotations.SubcommandInfo;
import masecla.mlib.classes.Registerable;
import masecla.mlib.main.MLib;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

@RegisterableInfo(command = "strafes")
public class StrafesCommand extends Registerable {

	private Items items;
	private DatabaseService databaseService;

	public StrafesCommand(MLib lib, Items items, DatabaseService databaseService) {
		super(lib);
		this.items = items;
		this.databaseService = databaseService;
	}

	@SubcommandInfo(subcommand = "velocity", permission = "strafes.velocity", aliases = { "vel" })
	public void velocityContainer(Player p) {
		lib.getContainerAPI().openFor(p, VelocityContainer.class);
	}

	@SubcommandInfo(subcommand = "velocity strafe", permission = "strafes.velocity")
	public void velocityStrafeEdit(Player p, String velocityString, String verticalVelocityString) {
		double velocity = 0;
		double verticalVelocity = 0;

		try {
			velocity = Double.parseDouble(velocityString);
			verticalVelocity = Double.parseDouble(verticalVelocityString);
		} catch (NumberFormatException e) {
			lib.getMessagesAPI().sendMessage("invalid-number", p);
			return;
		}

		databaseService.setVelocity(DatabaseType.STRAFES, velocity);
		databaseService.setVerticalVelocity(DatabaseType.STRAFES, verticalVelocity);
		lib.getMessagesAPI().sendMessage("velocity-set", p);
	}

	@SubcommandInfo(subcommand = "velocity leap", permission = "strafes.velocity")
	public void velocityLeapEdit(Player p, String velocityString, String verticalVelocityString) {
		double velocity = 0;
		double verticalVelocity = 0;

		try {
			velocity = Double.parseDouble(velocityString);
			verticalVelocity = Double.parseDouble(verticalVelocityString);
		} catch (NumberFormatException e) {
			lib.getMessagesAPI().sendMessage("invalid-number", p);
			return;
		}

		databaseService.setVelocity(DatabaseType.LEAP, velocity);
		databaseService.setVerticalVelocity(DatabaseType.LEAP, verticalVelocity);
		lib.getMessagesAPI().sendMessage("velocity-set", p);
	}

	@SubcommandInfo(subcommand = "cooldowns", permission = "strafes.cooldowns", aliases = { "cds", "cooldown" })
	public void cooldownContainer(Player p) {
		lib.getContainerAPI().openFor(p, CooldownsContainer.class);
	}

	@SuppressWarnings("deprecation")
	@SubcommandInfo(subcommand = "help", permission = "strafes.help")
	public void helpCommand(Player p) {
		p.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&a&lStrafes &2v" + lib.getPlugin().getDescription().getVersion() + " &7- &fby Gargant"));
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/strafes &7- &fAdd strafes to the inventory."));
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/leap &7- &fAdd the leap to your inventory."));
		p.sendMessage(
				ChatColor.translateAlternateColorCodes('&', "&a/strafes cooldowns &7- &fOpen the Settings container."));
		p.sendMessage(
				ChatColor.translateAlternateColorCodes('&', "&a/strafes velocity &7- &fOpen the Velocity container."));
		p.sendMessage(
				ChatColor.translateAlternateColorCodes('&', "&a/strafes velocity &7<&fstrafe&7/&fleap&7> &7<&fvalue 1&7> <&fvalue 2&7> - &fOpen the Velocity container."));
		ComponentBuilder b = new ComponentBuilder(ChatColor.WHITE + "Enjoy strafing? Rate this plugin!");
		b.event(new ClickEvent(Action.OPEN_URL,
				"https://www.spigotmc.org/resources/%E2%9C%A8-restrafes-strafe-around-your-world-%E2%9C%A8.96036/"));
		b.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				TextComponent.fromLegacyText(ChatColor.WHITE + "Click to open!")));
		p.spigot().sendMessage(b.create());
	}

	@Override
	public void fallbackCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			return;
		Player p = (Player) sender;

		if (!p.hasPermission("strafes.strafes")) {
			lib.getMessagesAPI().sendMessage("no-permission", sender);
			return;
		}
		if (!this.hasSlots(p, 3)) {
			lib.getMessagesAPI().sendMessage("no-space", sender);
			return;
		}
		if (!this.setItem(items.getBackStrafe(), 4, p)) {
			lib.getMessagesAPI().sendMessage("no-space", sender);
			return;
		}
		if (!this.setItem(items.getLeftStrafe(), 3, p)) {
			lib.getMessagesAPI().sendMessage("no-space", sender);
			return;
		}
		if (!this.setItem(items.getRightStrafe(), 5, p)) {
			lib.getMessagesAPI().sendMessage("no-space", sender);
			return;
		}
		lib.getMessagesAPI().sendMessage("strafes-activated", sender);
	}

	private boolean hasSlots(Player player, int slots) {
		for (int i = 0; i < player.getInventory().getSize(); i++) {
			if (player.getInventory().getItem(i) == null
					|| player.getInventory().getItem(i).getType().equals(Material.AIR))
				slots--;
			if (slots == 0)
				return true;
		}
		return false;
	}

	private boolean setItem(ItemStack is, int slot, Player p) {
		PlayerInventory inv = p.getInventory();
		if (inv.getItem(slot) == null || inv.getItem(slot).getType().equals(Material.AIR)) {
			inv.setItem(slot, is);
			return true;
		}
		if (p.getInventory().firstEmpty() == -1)
			return false;

		ItemStack previousItem = inv.getItem(slot).clone();
		boolean moved = false;
		for (int i = 0; i <= inv.getSize(); i++) {
			if (inv.getItem(i) == null || inv.getItem(i).getType().equals(Material.AIR)) {
				moved = true;
				p.getInventory().setItem(i, previousItem);
				p.getInventory().setItem(slot, is);
				break;
			}
		}
		if (!moved) {
			p.getInventory().addItem(is);
		}
		return true;
	}

}
