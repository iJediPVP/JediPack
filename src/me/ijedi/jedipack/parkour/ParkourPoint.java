package me.ijedi.jedipack.parkour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class ParkourPoint {

    public boolean IsStartingPoint = false;
    public boolean IsFinishingPoint = false;
    public int PointNumber = 0;
    public String PointName = "Point Name";

    public ParkourPoint(boolean isStartingPoint, boolean isFinishingPoint, int pointNumber, String pointName){
        IsStartingPoint = isStartingPoint;
        IsFinishingPoint = isFinishingPoint;
        PointNumber = pointNumber;
        PointName = pointName;
    }

    public void Spawn(Location location)
    {
        // Get the location's block to a pressure plate.
        World world = Bukkit.getWorld(location.getWorld().getUID());
        Location pointLocation = new Location(world, location.getX(), location.getY(), location.getZ());
        pointLocation.getBlock().setType(IsStartingPoint || IsFinishingPoint ? Material.GOLD_PLATE : Material.IRON_PLATE);

        // TODO: Make block unbreakable. Maybe event driven? Maybe propertyh driven?

        // Spawn the corresponding armor stand.
        ParkourStand stand = new ParkourStand();
        stand.SpawnStand(pointLocation, PointName);

        // TODO: Write block info to config file.
    }
}
