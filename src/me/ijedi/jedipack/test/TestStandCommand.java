package me.ijedi.jedipack.test;

import me.ijedi.jedipack.parkour.ParkourPoint;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestStandCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        // Only players can run this..
        if(commandSender instanceof Player){

            // Spawn a ParkourPoint object
            Player player = (Player)commandSender;
            ParkourPoint point = new ParkourPoint(true, false, 0, "First Point");
            point.Spawn(player.getLocation());

        }
        else{
            commandSender.sendMessage("Only players can run this command!");
        }

        return false;
    }
}
