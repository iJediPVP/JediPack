package me.ijedi.jedipack.menu;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Menu {

    //Variables
    private String menuName;
    private List<Inventory> pageList = new ArrayList<>();
    private HashMap<Inventory, Inventory> parentMap = new HashMap<>();
    private HashMap<Inventory, Inventory> childMap = new HashMap<>();
    private ItemStack exitButton, backButton, nextButton;
    private HashMap<String, ItemStack> buttonMap = new HashMap<>();

    //Constructor
    public Menu(String name) throws NullPointerException{
        //Set name
        menuName = name;
    }

    //Get name
    public String getName(){
        return menuName;
    }


    //************** CONTENTS **************
    //Set contents
    public void setContents(ItemStack[] items) throws NullPointerException{
        //Figure out middle size (where items actually are) & inventory size
        int inventorySize = 54;
        int middleSize = 28;
        int itemCount = items.length;

        //Check item count
        if(itemCount == 0){ //No items
            throw new NullPointerException("Menu contents can not be empty.");

        }else if(itemCount > 0 && itemCount <= 7){ //7 or less items
            inventorySize = 27;
            middleSize = 7;

        }else if(itemCount > 7 && itemCount <= 14){ //8 - 14 items
            inventorySize = 36;
            middleSize = 14;

        }else if(itemCount > 14 && itemCount <= 21){ // 15 - 21 items
            inventorySize = 45;
            middleSize = 21;

        }//Else use default values

        //Get pageCount
        int pageCount;
        if(itemCount % middleSize == 0){
            pageCount = itemCount / middleSize;
        }else{
            pageCount = (int) Math.floor(itemCount / middleSize) + 1;
        }

        //Set page items
        for(int x = 1; x <= pageCount; x++){
            Inventory page = Bukkit.createInventory(null, inventorySize, menuName); //Checked for empty name in constructor

            //Pull middleSize amount items at a time
            int start = (x - 1) * middleSize;
            ItemStack[] currentItems = Arrays.copyOfRange(items, start, start + middleSize);

            //Add items starting at slot 10
            int slot = 10;
            for(ItemStack item : currentItems){
                slot = checkSlot(slot);
                page.setItem(slot, item);
                slot++;
            }

            //Store page
            pageList.add(page);

            //Set parent & child
            if(x == 1){
                parentMap.put(page, null);
            }else{
                parentMap.put(page, pageList.get(pageList.size() - 2));
            }
            if(x > 1){
                childMap.put(pageList.get(pageList.size() - 2), page);
            }

        }

        //Add to MenuManager
        new MenuManager().addMenu(this);

    }
    //Keep slot aligned
    private int checkSlot(int slot){
        if(slot == 8 || slot == 17 || slot == 26 || slot == 35 || slot == 44){
            return  slot += 2;
        }else{
            return slot;
        }
    }


    //************** BUTTONS **************
    //Set buttons
    public void setButtons(ItemStack exit, ItemStack back, ItemStack next) throws NullPointerException{
        if(pageList.size() > 0){

            //Try to put buttons in buttonMap
            if(exit.hasItemMeta()){
                exitButton = exit;
                buttonMap.put(exitButton.getItemMeta().getDisplayName(), exitButton);
            }else{
                throw new NullPointerException("Exit Button does not have an item name.");
            }
            if(back.hasItemMeta()){
                backButton = back;
                buttonMap.put(backButton.getItemMeta().getDisplayName(), backButton);
            }else{
                throw new NullPointerException("Back Button does not have an item name.");
            }
            if(next.hasItemMeta()){
                nextButton = next;
                buttonMap.put(nextButton.getItemMeta().getDisplayName(), nextButton);
            }else{
                throw new NullPointerException("Next Button does not have an item name.");
            }

            //Set buttons for pages
            for(int x = 0; x < pageList.size(); x++){
                if(pageList.size() > 1){ //More than one page
                    if(x == 0){ //Page #1..Exit & Next
                        int slot = pageList.get(x).getSize() - 6;
                        pageList.get(x).setItem(slot, exitButton);
                        pageList.get(x).setItem(slot + 2, nextButton);
                    }else{ //Page #2+
                        if((x + 1) == pageList.size()){ //Last page.. Only Back
                            int slot = pageList.get(x).getSize() - 5;
                            pageList.get(x).setItem(slot, backButton);
                        }else{ //Not the last page.. Back & Next
                            int slot = pageList.get(x).getSize() - 6;
                            pageList.get(x).setItem(slot, backButton);
                            pageList.get(x).setItem(slot + 2, nextButton);
                        }
                    }
                }else{ //Only one page..Exit
                    int slot = pageList.get(x).getSize() - 5;
                    pageList.get(x).setItem(slot, exitButton);
                }
            }

        }else{ //No pages
            throw new NullPointerException("This Menu does not contain any pages. Set the contents of the Menu before adding buttons.");
        }
    }
    //Check if button
    public boolean isButton(ItemStack itemStack){
        for(String key : buttonMap.keySet()){
            if(buttonMap.get(key).equals(itemStack)){
                return true;
            }
        }
        return false;
    }
    //Get buttons
    protected ItemStack getExitButton(){
        return exitButton;
    }
    protected ItemStack getBackButton(){
        return backButton;
    }
    protected ItemStack getNextButton(){
        return nextButton;
    }


    //************** PAGES **************
    //Get first page
    public Inventory getFirstPage() throws IndexOutOfBoundsException{
        if(pageList.size() > 0){
            return pageList.get(0);
        }else{
            throw new IndexOutOfBoundsException("This Menu does not contain any pages.");
        }

    }
    //Get all pages
    public  List<Inventory> getPages(){
        return pageList;
    }
    //Get parent page
    public Inventory getParent(Inventory inventory) throws NullPointerException{
        for(Inventory key : parentMap.keySet()){
            if(key.equals(inventory)){
                return parentMap.get(key);
            }
        }
        throw new NullPointerException("This Menu page does not have a parent.");
    }
    //Get child page
    public Inventory getChild(Inventory inventory) throws NullPointerException{
        for(Inventory key : childMap.keySet()){
            if(key.equals(inventory)){
                return childMap.get(key);
            }
        }
        throw new NullPointerException("This Menu page does not have a child.");
    }
}
