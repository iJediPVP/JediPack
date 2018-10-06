package me.ijedi.jedipack.common;

import java.util.HashMap;
import java.util.HashSet;

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

    public static String[] HashSetToKeyArray(HashMap<String,?> hashSet){
        String[] returnArray = new String[hashSet.size()];
        int x = 0;
        for (String str : hashSet.keySet()){
            returnArray[x] = str;
            x++;
        }

        return returnArray;
    }
}
