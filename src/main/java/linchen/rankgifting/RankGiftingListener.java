package linchen.rankgifting;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RankGiftingListener implements Listener {

    private final JavaPlugin plugin;

    public RankGiftingListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().contains("sentGifts." + event.getPlayer().getUniqueId().toString())) {
            plugin.getConfig().set("sentGifts." + event.getPlayer().getUniqueId().toString(), 0);
            plugin.saveConfig();
        }
    }
}