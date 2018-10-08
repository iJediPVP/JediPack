package me.ijedi.jedipack.parkour;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;

import java.util.UUID;

public class ParkourStand {

    public UUID SpawnStand(Location location, String standName){

        World world = location.getWorld();

        // Center the X and Z
        double origX = location.getX();
        double newX = origX - (origX % 1); // Get the whole number
        newX = origX < 0 ? newX - .5 : newX + .5; // Add or subtract .5

        double origZ = location.getZ();
        double newZ = origZ - (origZ % 1); // Get the whole number
        newZ = origZ < 0 ? newZ - .5 : newZ + .5; // Add or subtract .5

        // Raise up the stand so it doesn't interfere with the pressure plate below it.
        double newY = location.getY() + .25;

        Location centeredLocation = new Location(world, newX, newY, newZ);

        // Spawn the armor stand
        ArmorStand armorStand = world.spawn(centeredLocation, ArmorStand.class);
        armorStand.setCustomName(formatString(standName, true));
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false);
        armorStand.setVisible(false);
        return armorStand.getUniqueId();
    }

    public static String formatString(String str, boolean isHeader){
        if(isHeader){
            str = ChatColor.GOLD + "" + ChatColor.BOLD + str;
        }
        else{
            str = ChatColor.AQUA + "" + ChatColor.BOLD + str;
        }
        return str;
    }

}
