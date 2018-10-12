package me.ijedi.jedipack.menu;

import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.List;

public class MenuManager {

    //Store Menu's
    private static HashMap<String, Menu> menuMap = new HashMap<>();

    //Add Menu
    public void addMenu(Menu menu){
        menuMap.put(menu.getName(), menu);
    }

    //Remove Menu
    public void removeMenu(String name){
        if(menuMap.containsKey(name)){
            menuMap.remove(name);
        }
    }

    //Open Menu by name
    public Inventory getMenu(String name) throws NullPointerException{
        if(menuMap.containsKey(name)){
            return menuMap.get(name).getFirstPage();
        }else{
            throw new NullPointerException("A Menu object with the name " + name + " does not exist.");
        }
    }

    //Get Menu pages
    public List<Inventory> getMenuPageList(String name) throws NullPointerException{
        if(menuMap.containsKey(name)){
            return menuMap.get(name).getPages();
        }else{
            throw new NullPointerException("A Menu object with the name " + name + " does not exist.");
        }

    }

    //Get menu by name
    protected Menu getMenuByName(String name){
        return menuMap.get(name);
    }

    //Check if menu exists
    protected boolean menuExists(String name){
        if(menuMap.containsKey(name)){
            return true;
        }else{
            return false;
        }
    }
}
