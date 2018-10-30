package me.ijedi.jedipack.common;

import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagString;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;

import java.util.HashMap;

public class Util {

    // I need some of these because C# has spoiled me.

    public static boolean isNullOrEmpty(String str){
        if(str == null || str.trim().isEmpty()){
            return true;
        }
        return false;
    }

    public static String safeTrim(String str){
        if(str == null){
            str = "";
        }
        str = str.trim();
        return str;
    }

    public static String toLower(String str){
        str = str.trim();
        str = str.toLowerCase();
        return str;
    }

    public static boolean isInteger(String str){
        try{
            int returnInt = Integer.parseInt(str);
            return true;
        } catch(NumberFormatException nfe){
            return false;
        }
    }

    public static String[] hashMapToKeyArray(HashMap<String,?> hashSet){
        String[] returnArray = new String[hashSet.size()];
        int x = 0;
        for (String str : hashSet.keySet()){
            returnArray[x] = str;
            x++;
        }

        return returnArray;
    }

    // The checkBelowLoc1 parameter subtracts 1 from loc1 Y before comparing.
    public static boolean doLocationsEqual(Location loc1, Location loc2, boolean checkBelowLoc1, boolean checkAboveBlockLoc1){
        if(loc1 != null || loc2 != null){

            // Compare world ids, x, y and z
            if(loc1.getWorld().getUID().equals(loc2.getWorld().getUID())
            && loc1.getX() == loc2.getX() && loc1.getZ() == loc2.getZ()) {

                // See if we should check above or below location 1.
                double y1 = loc1.getBlockY();
                if(checkBelowLoc1){
                    y1 = loc1.getBlockY() - 1;
                } else if(checkAboveBlockLoc1){
                    y1 = loc1.getBlockY() + 1;
                }

                if(y1 == loc2.getY()){
                    return true;
                }

            }

        }

        return false;
    }



    // Convert world ticks to 12 or 24 hour time
    public static String convertWorldTicksToTimeString(long ticks, boolean is24Hour){

        // Convert ticks to IRL time
        // Offset by 6000 so that the time aligns more with a IRL time
        ticks += 6000;
        ticks %= 24000;

        int hours = (int)Math.floor(ticks / 1000);
        int minutes = (int) ((ticks % 1000) / 1000.0 * 60);

        String amPM = "";
        if(!is24Hour) {

            // Set AM/PM
            if(hours > 11){
                amPM = "PM";
            } else {
                amPM = "AM";
            }

            // Convert to 12 hours
            if(hours > 12){
                hours -= 12;
            }
        }

        // Format and return
        String output = String.format("%2s:%2s", Integer.toString(hours), Integer.toString(minutes)).replace(' ', '0');
        if(!is24Hour){
            output += " " + amPM;
        }
        return output;
    }

    // Returns the block that the given sign was placed on.
    public static Block getBlockFromPlacedSign(Block signBlock){
        Sign sign = (Sign)signBlock.getState().getData();
        Block block = signBlock.getRelative(sign.getAttachedFace());
        return block;
    }

    public static Location getCenteredBlockLocation(Location location){
        Location newLoc = new Location(location.getWorld(), location.getX() + .5, location.getY(), location.getZ() + .5);
        return newLoc;
    }


    public static boolean hasNoPerms(Player player, String permission, MessageTypeEnum messageTypeEnum){
        if(!player.hasPermission(permission)){
            messageTypeEnum.sendMessage("You need permission to use this command!", player, true);
            return true;
        }
        return false;
    }

    public static String getNBTTagString(ItemStack item, String tagName){

        // Cast to NMS & get the tag compound
        net.minecraft.server.v1_13_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbtTag = nmsItem.getTag();
        String currentValue = "";
        if(nbtTag != null){
            currentValue = nbtTag.getString(tagName);
        }
        return currentValue;
    }

    public static ItemStack setNBTTagString(ItemStack item, String tagName, String tagValue){

        // Cast to NMS & get the tag compound
        net.minecraft.server.v1_13_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbtCompound = nmsItem.getOrCreateTag();
        nbtCompound.set(tagName, new NBTTagString(tagValue));
        item = CraftItemStack.asBukkitCopy(nmsItem);
        return item;
    }
}
