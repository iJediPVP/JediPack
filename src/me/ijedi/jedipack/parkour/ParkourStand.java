package me.ijedi.jedipack.parkour;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;

public class ParkourStand {



    public void SpawnStand(Location location, String standName){

        World world = location.getWorld();

        // Center the X and Z
        double origX = location.getX();
        double newX = origX - (origX % 1); // Get the whole number
        newX = origX < 0 ? newX - .5 : newX + .5; // Add or subtract .5

        double origZ = location.getZ();
        double newZ = origZ - (origZ % 1); // Get the whole number
        newZ = origZ < 0 ? newZ - .5 : newZ + .5; // Add or subtract .5

        Location centeredLocation = new Location(world, newX, location.getY(), newZ);

        // Spawn the armor stand
        ArmorStand armorStand = world.spawn(centeredLocation, ArmorStand.class);
        armorStand.setCustomName(standName);
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false);
        armorStand.setVisible(false);
    }

}
