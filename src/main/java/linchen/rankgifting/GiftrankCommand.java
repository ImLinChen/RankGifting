package linchen.rankgifting;

import net.luckperms.api.model.data.TemporaryNodeMergeStrategy;
import net.luckperms.api.node.types.InheritanceNode;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class GiftrankCommand implements CommandExecutor {

    private RankGifting rankGifting;
    private Economy economy;
    private JavaPlugin plugin;

    public GiftrankCommand(Economy economy, RankGifting rankGifting, JavaPlugin plugin) {
        this.economy = economy;
        this.rankGifting = rankGifting;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player senderPlayer = (Player) sender;
        String senderUUID = senderPlayer.getUniqueId().toString();


        String targetPlayerName = args[0];
        String rank = args[1];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Target player is offline.");
            return true;
        }

        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(targetPlayer.getUniqueId());
        String targetPrefix = user.getCachedData().getMetaData().getPrefix();
        String senderPrefix = luckPerms.getUserManager().getUser(senderPlayer.getUniqueId()).getCachedData().getMetaData().getPrefix()+" ";
        String displayName = targetPrefix +" "+ targetPlayer.getDisplayName();

        if (targetPlayer.hasPermission("rank.special"))
        {
            sender.sendMessage(ChatColor.RED + "This player already has this rank or higher.");
            return true;
        }
        switch (rank.toLowerCase()) {
            case "vip":
                if (targetPlayer.hasPermission("rank.vip") || targetPlayer.hasPermission("rank.vip+") ||
                        targetPlayer.hasPermission("rank.mvp") || targetPlayer.hasPermission("rank.mvp+") ||
                        targetPlayer.hasPermission("rank.mvp++")) {
                    sender.sendMessage(ChatColor.RED + "This player already has this rank or higher.");
                } else {
                    double balance = economy.getBalance((Player) sender);
                    if (balance <= 6998) {
                        sender.sendMessage(ChatColor.RED + "You don't have enough Coins in your wallet!");
                    }
                    else if (targetPlayer.hasPermission("group.default")){
                        economy.withdrawPlayer(targetPlayer, 6999);

                        Bukkit.broadcast("§e§ke§c§ke§4§ke§r " + senderPrefix + sender.getName() + " §egifted the " + "§aVIP " +
                                "§erank to " + ChatColor.GREEN + targetPlayer.getName() + "§e. §4§ke§c§ke§e§ke§r","bedtwl.user");

                        int sentGifts = rankGifting.getGiftCounter(senderPlayer.getUniqueId().toString());
                        rankGifting.incrementGiftCounter(senderPlayer.getUniqueId().toString());
                        plugin.getConfig().set("sentGifts." + senderPlayer.getUniqueId().toString(), sentGifts + 1);
                        plugin.saveConfig();

                        sentGifts++;
                        Bukkit.broadcast(ChatColor.YELLOW + "They have gifted " + ChatColor.GOLD + sentGifts + ChatColor.YELLOW + " rank so far!","bedtwl.user");

                        InheritanceNode vipGroupNode = InheritanceNode.builder("vip").build();
                        user.data().add(vipGroupNode, TemporaryNodeMergeStrategy.ADD_NEW_DURATION_TO_EXISTING);

                        luckPerms.getUserManager().saveUser(user);
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "Target must have "+ChatColor.GRAY+"[Default] "+ChatColor.RED+" Rank to gift "+ ChatColor.GREEN+"[VIP]");
                    }
                }
                break;
            case "vip+":
                if (targetPlayer.hasPermission("rank.vip+") ||
                        targetPlayer.hasPermission("rank.mvp") || targetPlayer.hasPermission("rank.mvp+") ||
                        targetPlayer.hasPermission("rank.mvp++")) {
                    sender.sendMessage(ChatColor.RED + "This player already has this rank or higher.");
                } else {
                    double balance = economy.getBalance((Player) sender);
                    if (balance <= 14998) {
                        sender.sendMessage(ChatColor.RED + "You don't have enough Coins in your wallet!");
                    } else if (targetPlayer.hasPermission("group.vip")){
                        economy.withdrawPlayer(targetPlayer, 14999);

                        Bukkit.broadcast("§e§ke§c§ke§4§ke§r " + senderPrefix + sender.getName() + " §egifted the " + "§aVIP§6+ " +
                                "§erank to " + ChatColor.GREEN + targetPlayer.getName() + "§e. §4§ke§c§ke§e§ke§r","bedtwl.user");

                        int sentGifts = rankGifting.getGiftCounter(senderPlayer.getUniqueId().toString());
                        rankGifting.incrementGiftCounter(senderPlayer.getUniqueId().toString());
                        plugin.getConfig().set("sentGifts." + senderPlayer.getUniqueId().toString(), sentGifts + 1);
                        plugin.saveConfig();

                        sentGifts++;
                        Bukkit.broadcast(ChatColor.YELLOW + "They have gifted " + ChatColor.GOLD + sentGifts + ChatColor.YELLOW + " rank so far!","bedtwl.user");

                        InheritanceNode vipGroupNode = InheritanceNode.builder("vip+").build();
                        user.data().add(vipGroupNode);
                        luckPerms.getUserManager().saveUser(user);
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "Target must have "+ChatColor.GREEN+"[VIP]"+ChatColor.RED+" Rank to gift "+ ChatColor.GREEN+"[VIP"+ChatColor.GOLD+"+"+ChatColor.GREEN+"]");
                    }

                }

                break;
            case "mvp":
                if (targetPlayer.hasPermission("rank.mvp") || targetPlayer.hasPermission("rank.mvp+") ||
                        targetPlayer.hasPermission("rank.mvp++")) {
                    sender.sendMessage(ChatColor.RED + "This player already has this rank or higher.");
                } else {
                    double balance = economy.getBalance((Player) sender);
                    if (balance <= 29998) {
                        sender.sendMessage(ChatColor.RED + "You don't have enough Coins in your wallet!");
                    } else if (targetPlayer.hasPermission("group.vip+")){
                        economy.withdrawPlayer(targetPlayer, 29999);

                        Bukkit.broadcast("§e§ke§c§ke§4§ke§r " + senderPrefix + sender.getName() + " §egifted the " + "§bMVP " +
                                "§erank to " + ChatColor.AQUA + targetPlayer.getName() + "§e. §4§ke§c§ke§e§ke§r","bedtwl.user");

                        int sentGifts = rankGifting.getGiftCounter(senderPlayer.getUniqueId().toString());
                        rankGifting.incrementGiftCounter(senderPlayer.getUniqueId().toString());
                        plugin.getConfig().set("sentGifts." + senderPlayer.getUniqueId().toString(), sentGifts + 1);
                        plugin.saveConfig();

                        sentGifts++;
                        Bukkit.broadcast(ChatColor.YELLOW + "They have gifted " + ChatColor.GOLD + sentGifts + ChatColor.YELLOW + " rank so far!","bedtwl.user");

                        InheritanceNode vipGroupNode = InheritanceNode.builder("mvp").build();
                        user.data().add(vipGroupNode);
                        luckPerms.getUserManager().saveUser(user);
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "Target must have "+ChatColor.GREEN+"[VIP"+ChatColor.GOLD+"+"+ChatColor.GREEN+"]"+ChatColor.RED+" Rank to gift "+ ChatColor.AQUA+"[MVP]");
                    }
                }
                break;
            case "mvp+":
                if (targetPlayer.hasPermission("rank.mvp+") ||
                        targetPlayer.hasPermission("rank.mvp++")) {
                    sender.sendMessage(ChatColor.RED + "This player already has this rank or higher.");
                } else {
                    double balance = economy.getBalance((Player) sender);
                    if (balance <= 44998) {
                        sender.sendMessage(ChatColor.RED + "You don't have enough Coins in your wallet!");
                    } else if (targetPlayer.hasPermission("group.mvp")){
                        economy.withdrawPlayer(targetPlayer, 44999);

                        Bukkit.broadcast("§e§ke§c§ke§4§ke§r " + senderPrefix + sender.getName() + " §egifted the " + "§bMVP§c+ " +
                                "§erank to " + ChatColor.AQUA + targetPlayer.getName() + "§e. §4§ke§c§ke§e§ke§r","bedtwl.user");

                        int sentGifts = rankGifting.getGiftCounter(senderPlayer.getUniqueId().toString());
                        rankGifting.incrementGiftCounter(senderPlayer.getUniqueId().toString());
                        plugin.getConfig().set("sentGifts." + senderPlayer.getUniqueId().toString(), sentGifts + 1);
                        plugin.saveConfig();

                        sentGifts++;
                        Bukkit.broadcast(ChatColor.YELLOW + "They have gifted " + ChatColor.GOLD + sentGifts + ChatColor.YELLOW + " rank so far!","bedtwl.user");

                        InheritanceNode vipGroupNode = InheritanceNode.builder("mvp+").build();
                        user.data().add(vipGroupNode);
                        luckPerms.getUserManager().saveUser(user);
                    }
                      else
                    {
                        sender.sendMessage(ChatColor.RED + "Target must have "+ChatColor.AQUA+"[MVP]"+ChatColor.RED+" Rank to gift "+ ChatColor.AQUA+"[MVP"+ChatColor.RED+"+"+ChatColor.AQUA+"]");
                    }
                }
                break;
            case "mvp++":
                if (targetPlayer.hasPermission("rank.mvp++")) {
                    sender.sendMessage(ChatColor.RED + "This player already has this rank or higher.");
                } else {
                    double balance = economy.getBalance((Player) sender);
                    if (balance <= 59998) {
                        sender.sendMessage(ChatColor.RED + "You don't have enough Coins in your wallet!");
                    } else if (targetPlayer.hasPermission("group.mvp+")){
                        economy.withdrawPlayer(targetPlayer, 59999);

                        Bukkit.broadcast("§e§ke§c§ke§4§ke§r " + senderPrefix + sender.getName() + " §egifted the " + "§6MVP§c++ " +
                                "§erank 30 days to " + ChatColor.GOLD + targetPlayer.getName() + "§e. §4§ke§c§ke§e§ke§r","bedtwl.user");


                        int sentGifts = rankGifting.getGiftCounter(senderPlayer.getUniqueId().toString());
                        rankGifting.incrementGiftCounter(senderPlayer.getUniqueId().toString());
                        plugin.getConfig().set("sentGifts." + senderPlayer.getUniqueId().toString(), sentGifts + 1);
                        plugin.saveConfig();

                        sentGifts++;
                        Bukkit.broadcast(ChatColor.YELLOW + "They have gifted " + ChatColor.GOLD + sentGifts + ChatColor.YELLOW + " rank so far!","bedtwl.user");

                        InheritanceNode vipGroupNode = InheritanceNode.builder("mvp++").expiry(Duration.ofDays(30)).build();
                        user.data().add(vipGroupNode);
                        luckPerms.getUserManager().saveUser(user);
                    }
                      else
                    {
                        sender.sendMessage(ChatColor.RED + "Target must have "+ChatColor.AQUA+"[MVP"+ChatColor.RED+"+"+ChatColor.AQUA+"]"+ChatColor.RED+" Rank to gift "+ ChatColor.GOLD+"[MVP"+ChatColor.RED+"++"+ChatColor.GOLD+"]");
                    }
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid rank. Available ranks: VIP, VIP+, MVP, MVP+, MVP++");
                break;
        }
        return true;
    }
}