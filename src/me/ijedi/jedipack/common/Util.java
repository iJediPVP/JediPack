package me.ijedi.jedipack.common;

import me.ijedi.jedipack.JediPackMain;
import org.bukkit.Location;

import java.util.HashMap;

public class Util {

    // I need some of these because C# has spoiled me.

    public static boolean IsNullOrEmpty(String str){
        if(str == null || str.trim().isEmpty()){
            return true;
        }
        return false;
    }

    public static String SafeTrim(String str){
        if(str == null){
            str = "";
        }
        str = str.trim();
        return str;
    }

    public static String ToLower(String str){
        str = str.trim();
        str = str.toLowerCase();
        return str;
    }

    public static boolean IsInteger(String str){
        try{
            int returnInt = Integer.parseInt(str);
            return true;
        } catch(NumberFormatException nfe){
            return false;
        }
    }

    public static String[] HashMapToKeyArray(HashMap<String,?> hashSet){
        String[] returnArray = new String[hashSet.size()];
        int x = 0;
        for (String str : hashSet.keySet()){
            returnArray[x] = str;
            x++;
        }

        return returnArray;
    }

    // The checkBelowLoc1 parameter subtracts 1 from loc1 Y before comparing.
    public static boolean DoLocationsEqual(Location loc1, Location loc2, boolean checkBelowLoc1){
        if(loc1 != null || loc2 != null){

            // Compare world ids, x, y and z
            if(loc1.getWorld().getUID().equals(loc2.getWorld().getUID())
            && loc1.getX() == loc2.getX() && loc1.getZ() == loc2.getZ()) {

                JediPackMain.getThisPlugin().getLogger().info("loc 1 Y:" + loc1.getY());
                JediPackMain.getThisPlugin().getLogger().info("loc 2 Y:" + loc2.getY());

                // See if we should check above location 1.
                double y1 = loc1.getY();
                if(checkBelowLoc1){
                    y1 = loc1.getY() - 1;
                }

                if(y1 == loc2.getY()){
                    return true;
                }

            }

        }

        return false;
    }
}
