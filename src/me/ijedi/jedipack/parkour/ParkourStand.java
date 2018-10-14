package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.common.Util;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;

import java.util.UUID;

public class ParkourStand {

    private UUID footerStandId;
    private UUID headerStandId;

    public ParkourStand(UUID footerStandId, UUID headerStandId){
        this.footerStandId = footerStandId;
        this.headerStandId = headerStandId;
    }

    public UUID getHeaderStandId() {
        return headerStandId;
    }

    public UUID getFooterStandId() {
        return footerStandId;
    }

    public static ParkourStand SpawnStand(Location location, boolean isStartingPoint, boolean isFinishingPoint, int pointNumber){

        // Get the stand name
        String standName;
        if(isStartingPoint){
            standName = "Start";
        }else if(isFinishingPoint){
            standName = "Finish";
        }else {
            standName = "Checkpont #" + pointNumber;
        }

        World world = location.getWorld();

        // Spawn the pressure plate
        location.getBlock().setType(isStartingPoint || isFinishingPoint ? Material.GOLD_PLATE : Material.IRON_PLATE);

        // Center the location, and raise up the stand so it doesn't interfere with the pressure plate below it.
        Location centeredLocation = Util.centerLocation(location);
        centeredLocation.setY(centeredLocation.getY() + .25);

        // Spawn the footer stand
        ArmorStand armorStand = world.spawn(centeredLocation, ArmorStand.class);
        armorStand.setCustomName(formatString(standName, false));
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false);
        armorStand.setVisible(false);

        // Spawn the header stand
        centeredLocation.setY(centeredLocation.getY() + .25);
        ArmorStand headerStand = world.spawn(centeredLocation, ArmorStand.class);
        headerStand.setCustomName(formatString("Parkour", true));
        headerStand.setCustomNameVisible(true);
        headerStand.setGravity(false);
        headerStand.setVisible(false);

        return new ParkourStand(armorStand.getUniqueId(), headerStand.getUniqueId());
    }

    public static String formatString(String str, boolean isHeader){
        if(isHeader){
            str = ChatColor.AQUA + "" + ChatColor.BOLD + str;
        }
        else{
            str = ChatColor.GOLD + "" + ChatColor.BOLD + str;
        }
        return str;
    }

}
