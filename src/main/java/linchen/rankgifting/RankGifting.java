package linchen.rankgifting;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;

public class RankGifting extends JavaPlugin implements CommandExecutor, Listener {
    private Economy economy;
    private Player targetPlayer;
    private Map<String, Integer> giftCounterMap = new HashMap<>();
    private RankGifting plugin;

    @Override
    public void onEnable() {
        getCommand("gift").setExecutor(this);
        getLogger().info("RankGifting has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new RankGiftingListener(this), this);
        saveDefaultConfig();
        plugin = this;

        if (!setupEconomy()) {
            getLogger().severe("Vault economy plugin not found or not supported!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        GiftrankCommand giftrankCommand = new GiftrankCommand(economy, this, this);
        getCommand("giftrank").setExecutor(giftrankCommand);
    }

    @Override
    public void onDisable() {
        getLogger().info("RankGifting has been disabled!");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public int getGiftCounter(String playerName) {
        return plugin.getConfig().getInt("sentGifts." + playerName, 0);
    }

    public void incrementGiftCounter(String playerName) {
        giftCounterMap.put(playerName, getGiftCounter(playerName) + 1);

        plugin.getConfig().set("sentGifts." + playerName, giftCounterMap.get(playerName));
        plugin.saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /gift <player>");
            return true;
        }

        targetPlayer = Bukkit.getPlayer(args[0]);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage(ChatColor.RED + "You can't gift offline players.");
            return true;
        }

        if (targetPlayer.hasPermission("rank.special")) {
            player.sendMessage(ChatColor.RED + "The target player already has this Rank or higher!");
            return true;
        }

        openGiftSelectionMenu(player,Bukkit.getPlayer(args[0]));

        return true;
    }

    private void openGiftSelectionMenu(Player player,Player targetPlayer) {

        Inventory giftMenu = Bukkit.createInventory(null, 6 * 9, ChatColor.DARK_GRAY + "Select a Gift");

        ItemStack headItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta headMeta = (SkullMeta) headItem.getItemMeta();
        headMeta.setOwner(targetPlayer.getName());
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(targetPlayer.getUniqueId());
        String prefix = user.getCachedData().getMetaData().getPrefix();
        String displayName = prefix + " "+targetPlayer.getDisplayName();
        headMeta.setDisplayName(ChatColor.RESET + displayName);
        headMeta.setLore(Collections.singletonList(ChatColor.GRAY + "You are sending a gift to this player!"));
        headItem.setItemMeta(headMeta);
        giftMenu.setItem(13, headItem);
        double balance = economy.getBalance(player);
        //A Item
        ItemStack vipItem = new ItemStack(Material.EMERALD);
        ItemMeta vipMeta = vipItem.getItemMeta();
        ArrayList<String> Viplore = new ArrayList<String>();
        if (targetPlayer.hasPermission("rank.vip"))
        {

            vipMeta.setDisplayName(ChatColor.GREEN + "VIP Rank");
            Viplore.add(ChatColor.GRAY + "Cost: " + ChatColor.GRAY + "6,999 Coins");
            Viplore.add("");
            Viplore.add(displayName+ChatColor.AQUA+" Already have this rank!");
            vipMeta.addEnchant(Enchantment.FIRE_ASPECT,1,true);
        }
        else
        {
            if (balance <= 6998) {
                vipMeta.setDisplayName(ChatColor.RED + "VIP Rank");
                Viplore.add(ChatColor.GRAY + "Cost: " + ChatColor.RED + "6,999 Coins");
                Viplore.add("");
                Viplore.add(displayName+ChatColor.RED+" You dont have enough coins to buy!");

            }
            else
            {
                vipMeta.setDisplayName(ChatColor.GREEN + "VIP Rank");
                Viplore.add(ChatColor.GRAY + "Cost: " + ChatColor.AQUA + "6,999 Coins");
                Viplore.add("");
                Viplore.add(displayName+ChatColor.AQUA+" Click to gift!");
            }

        }
        vipMeta.setLore(Viplore);
        vipItem.setItemMeta(vipMeta);
        giftMenu.setItem(27, vipItem);

        //A Item
        ItemStack vipPlusItem = new ItemStack(Material.EMERALD);
        ItemMeta vipPlusMeta = vipPlusItem.getItemMeta();
        ArrayList<String> vipPluslore = new ArrayList<String>();
        if (targetPlayer.hasPermission("rank.vip+"))
        {

            vipPlusMeta.setDisplayName(ChatColor.GREEN + "VIP§6+ §aRank");
            vipPluslore.add(ChatColor.GRAY + "Cost: " + ChatColor.GRAY + "14,999 Coins");
            vipPluslore.add("");
            vipPluslore.add(displayName+ChatColor.AQUA+" Already have this rank!");
            vipPlusMeta.addEnchant(Enchantment.FIRE_ASPECT,1,true);
        }
        else if (targetPlayer.hasPermission("rank.vip"))
        {
            if (balance <= 14998) {
                vipPlusMeta.setDisplayName(ChatColor.RED + "VIP§6+ §aRank");
                vipPluslore.add(ChatColor.GRAY + "Cost: " + ChatColor.RED + "14,999 Coins");
                vipPluslore.add("");
                vipPluslore.add(displayName+ChatColor.RED+" You dont have enough coins to buy!");

            }
            else
            {
                vipPlusMeta.setDisplayName(ChatColor.GREEN + "VIP§6+ §aRank");
                vipPluslore.add(ChatColor.GRAY + "Cost: " + ChatColor.AQUA + "14,999 Coins");
                vipPluslore.add("");
                vipPluslore.add(displayName+ChatColor.AQUA+" Click to gift!");
            }

        }
        else
        {
            vipPlusMeta.setDisplayName(ChatColor.RED + "VIP§6+ §aRank");
            vipPluslore.add(ChatColor.GRAY + "Cost: " + ChatColor.RED + "14,999 Coins");
            vipPluslore.add("");
            vipPluslore.add(displayName+ChatColor.RED+" Target must have VIP rank!");
        }
        vipPlusMeta.setLore(vipPluslore);
        vipPlusItem.setItemMeta(vipPlusMeta);
        giftMenu.setItem(29, vipPlusItem);
        //
        //A Item
        ItemStack mvpItem = new ItemStack(Material.DIAMOND);
        ItemMeta mvpMeta = mvpItem.getItemMeta();
        ArrayList<String> Mvplore = new ArrayList<String>();
        if (targetPlayer.hasPermission("rank.mvp"))
        {
            mvpMeta.setDisplayName(ChatColor.AQUA + "MVP Rank");
            Mvplore.add(ChatColor.GRAY + "Cost: " + ChatColor.GRAY + "29,999 Coins");
            Mvplore.add("");
            Mvplore.add(displayName+ChatColor.AQUA+" Already have this rank!");
            mvpMeta.addEnchant(Enchantment.FIRE_ASPECT,1,true);

        }
        else if (targetPlayer.hasPermission("rank.vip+"))
        {

            if (balance <= 29998) {
                mvpMeta.setDisplayName(ChatColor.RED + "MVP Rank");
                Mvplore.add(ChatColor.GRAY + "Cost: " + ChatColor.RED + "29,999 Coins");
                Mvplore.add("");
                Mvplore.add(displayName+ChatColor.RED+" You dont have enough coins to buy!");

            }
            else
            {
                mvpMeta.setDisplayName(ChatColor.AQUA + "MVP Rank");
                Mvplore.add(ChatColor.GRAY + "Cost: " + ChatColor.AQUA + "29,999 Coins");
                Mvplore.add("");
                Mvplore.add(displayName+ChatColor.AQUA+" Click to gift!");
            }
        }
        else
        {
            mvpMeta.setDisplayName(ChatColor.RED + "MVP Rank");
            Mvplore.add(ChatColor.GRAY + "Cost: " + ChatColor.RED + "29,999 Coins");
            Mvplore.add("");
            Mvplore.add(displayName+ChatColor.RED+" Target must have VIP+ Rank!");
        }
        mvpMeta.setLore(Mvplore);
        mvpItem.setItemMeta(mvpMeta);
        giftMenu.setItem(31, mvpItem);
        //
        //A Item
        ItemStack mvpplusItem = new ItemStack(Material.DIAMOND);
        ItemMeta mvpplusMeta = mvpItem.getItemMeta();
        ArrayList<String> Mvppluslore = new ArrayList<String>();
        if (targetPlayer.hasPermission("rank.mvp+"))
        {
            mvpplusMeta.setDisplayName(ChatColor.AQUA + "MVP§c+ §bRank");
            Mvppluslore.add(ChatColor.GRAY + "Cost: " + ChatColor.GRAY + "49,999 Coins");
            Mvppluslore.add("");
            Mvppluslore.add(displayName+ChatColor.AQUA+" Already have this rank!");
            mvpplusMeta.addEnchant(Enchantment.FIRE_ASPECT,1,true);

        }
        else if (targetPlayer.hasPermission("rank.mvp"))
        {

            if (balance <= 49998) {
                mvpplusMeta.setDisplayName(ChatColor.RED + "MVP+ Rank");
                Mvppluslore.add(ChatColor.GRAY + "Cost: " + ChatColor.RED + "49,999 Coins");
                Mvppluslore.add("");
                Mvppluslore.add(displayName+ChatColor.RED+" You dont have enough coins to buy!");

            }
            else
            {
                mvpplusMeta.setDisplayName(ChatColor.AQUA + "MVP§c+ §bRank");
                Mvppluslore.add(ChatColor.GRAY + "Cost: " + ChatColor.AQUA + "49,999 Coins");
                Mvppluslore.add("");
                Mvppluslore.add(displayName+ChatColor.AQUA+" Click to gift!");
            }
        }
        else
        {
            mvpplusMeta.setDisplayName(ChatColor.RED + "MVP+ Rank");
            Mvppluslore.add(ChatColor.GRAY + "Cost: " + ChatColor.RED + "49,999 Coins");
            Mvppluslore.add("");
            Mvppluslore.add(displayName+ChatColor.RED+" Target must have MVP Rank!");
        }
        mvpplusMeta.setLore(Mvppluslore);
        mvpplusItem.setItemMeta(mvpplusMeta);
        giftMenu.setItem(33, mvpplusItem);
        //
        //A Item
        ItemStack mvpPlusPlusItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta mvpPlusPlusMeta = mvpPlusPlusItem.getItemMeta();
        ArrayList<String> Mvppluspluslore = new ArrayList<String>();
        if (targetPlayer.hasPermission("rank.mvp++"))
        {
            mvpPlusPlusMeta.setDisplayName(ChatColor.GOLD + "MVP§c++ §6Rank");
            Mvppluspluslore.add(ChatColor.GRAY + "Cost: " + ChatColor.GRAY + "59,999 Coins");
            Mvppluspluslore.add("");
            Mvppluspluslore.add(displayName+ChatColor.AQUA+" Already have this rank!");
            mvpPlusPlusMeta.addEnchant(Enchantment.FIRE_ASPECT,1,true);

        }
        else if (targetPlayer.hasPermission("rank.mvp+"))
        {

            if (balance <= 49998) {
                mvpPlusPlusMeta.setDisplayName(ChatColor.RED + "MVP++ Rank");
                Mvppluspluslore.add(ChatColor.GRAY + "Cost: " + ChatColor.RED + "59,999 Coins");
                Mvppluspluslore.add("");
                Mvppluspluslore.add(displayName+ChatColor.RED+" You dont have enough coins to buy!");

            }
            else
            {
                mvpPlusPlusMeta.setDisplayName(ChatColor.GOLD + "MVP§c++ §6Rank");
                Mvppluspluslore.add(ChatColor.GRAY + "Cost: " + ChatColor.AQUA + "59,999 Coins");
                Mvppluspluslore.add("");
                Mvppluspluslore.add(displayName+ChatColor.AQUA+" Click to gift!");
            }
        }
        else
        {
            mvpPlusPlusMeta.setDisplayName(ChatColor.RED + "MVP++ Rank");
            Mvppluspluslore.add(ChatColor.GRAY + "Cost: " + ChatColor.RED + "49,999 Coins");
            Mvppluspluslore.add("");
            Mvppluspluslore.add(displayName+ChatColor.RED+" Target must have MVP Rank!");
        }
        mvpPlusPlusMeta.setLore(Mvppluspluslore);
        mvpPlusPlusItem.setItemMeta(mvpPlusPlusMeta);
        giftMenu.setItem(35, mvpPlusPlusItem);
        //


        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Close");
        closeButton.setItemMeta(closeMeta);
        giftMenu.setItem(49, closeButton);

        ItemStack goldItem = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta goldMeta = goldItem.getItemMeta();
        goldMeta.setDisplayName(ChatColor.WHITE + "Coins Wallet: " + ChatColor.GOLD + "$" + economy.getBalance(player) + " Coins");
        goldMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Your current coins balance."));
        goldItem.setItemMeta(goldMeta);
        giftMenu.setItem(53, goldItem);

        player.openInventory(giftMenu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory != null && clickedInventory.getName().equals(ChatColor.DARK_GRAY + "Select a Gift")) {
            event.setCancelled(true);

            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                ItemMeta itemMeta = clickedItem.getItemMeta();
                if (itemMeta != null && itemMeta.hasDisplayName()) {
                    String displayName = ChatColor.stripColor(itemMeta.getDisplayName());

                    switch (displayName) {
                        case "VIP Rank":
                            Bukkit.dispatchCommand(player, "giftrank " + targetPlayer.getName() + " VIP");
                            player.closeInventory();
                            break;
                        case "VIP+ Rank":
                            Bukkit.dispatchCommand(player, "giftrank " + targetPlayer.getName() + " VIP+");
                            player.closeInventory();
                            break;
                        case "MVP Rank":
                            Bukkit.dispatchCommand(player, "giftrank " + targetPlayer.getName() + " MVP");
                            player.closeInventory();
                            break;
                        case "MVP+ Rank":
                            Bukkit.dispatchCommand(player, "giftrank " + targetPlayer.getName() + " MVP+");
                            player.closeInventory();
                            break;
                        case "MVP++ Rank":
                            Bukkit.dispatchCommand(player, "giftrank " + targetPlayer.getName() + " MVP++");
                            player.closeInventory();
                            break;
                        case "Close":
                            player.closeInventory();
                            break;
                    }
                }
            }
        }
    }
}