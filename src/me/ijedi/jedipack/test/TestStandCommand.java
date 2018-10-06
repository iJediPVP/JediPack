package me.ijedi.jedipack.test;

import me.ijedi.jedipack.parkour.ParkourStand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class TestStandCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        // Only players can run this..
        if(commandSender instanceof Player){

            // Spawn simple entity
            Player player = (Player)commandSender;
            //player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);

            ParkourStand stand = new ParkourStand();
            stand.SpawnStand(player.getLocation(), "Armor Stand");

        }
        else{
            commandSender.sendMessage("Only players can run this command!");
        }

        return false;
    }
}
