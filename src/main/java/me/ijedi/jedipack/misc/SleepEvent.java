package me.ijedi.jedipack.misc;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;


public class SleepEvent implements Listener {

    private static String SLEEP_PERM = "jp.sleep";
    private static Player triggerPlayer;

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event){

        // Check for perms
        Player player = event.getPlayer();
        if(player.hasPermission(SLEEP_PERM)){

            // Prevent two player's from trigger this timer
            if(triggerPlayer != null){
                return;
            }
            triggerPlayer = player;

            // Wait 3 seconds
            new BukkitRunnable(){
                @Override
                public void run(){
                    if(triggerPlayer != null && triggerPlayer.isSleeping()){
                        // Set the time to day
                        World world = player.getWorld();
                        world.setTime(0);

                        // Turn off thunder
                        if(world.isThundering()){
                            world.setThundering(false);
                        }

                        if(world.hasStorm()){
                            world.setStorm(false);
                        }

                        String msg = player.getName() + " went to bed early!";
                        for(Player onlinePlayer : JediPackMain.getThisPlugin().getServer().getOnlinePlayers()){
                            MessageTypeEnum.SleepMessage.sendMessage(msg, onlinePlayer, false);
                        }

                        triggerPlayer = null;
                    }
                }
            }.runTaskLaterAsynchronously(JediPackMain.getThisPlugin(), 3 * 20L); // 3 seconds
        }

    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent event){

        if(triggerPlayer != null && triggerPlayer.getUniqueId().equals(event.getPlayer().getUniqueId())){
            // Set the trigger player to the next sleeping player.
            for(Player player : Bukkit.getOnlinePlayers()){
                if(!player.getUniqueId().equals(triggerPlayer.getUniqueId()) && player.isSleeping()){
                    triggerPlayer = player;
                    return;
                }
            }
        }

        // No one is sleeping
        triggerPlayer = null;
    }
}
