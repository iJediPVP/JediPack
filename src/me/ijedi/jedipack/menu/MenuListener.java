package me.ijedi.jedipack.menu;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class MenuListener implements Listener {

    //Constructor
    public MenuListener(JavaPlugin plugin){
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    //Event
    @EventHandler
    public void menuClick(InventoryClickEvent event){
        //Check for player
        if(event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();

            //Check if this is a menu
            String invName = event.getInventory().getName();
            MenuManager menuManager = new MenuManager();
            if (menuManager.menuExists(invName)) {
                Menu menu = menuManager.getMenuByName(invName);

                //Check itemName
                try{
                    ItemStack item = event.getCurrentItem();
                    String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

                    //Check if a button
                    if(menu.isButton(item)){
                        //Check which button
                        if(menu.getExitButton().equals(item)){
                            //Close
                            player.closeInventory();

                        }else if(menu.getBackButton().equals(item)){
                            //Get parent inventory
                            player.openInventory(menu.getParent(event.getInventory()));

                        }else if(menu.getNextButton().equals(item)){
                            //Get child inventory
                            player.openInventory(menu.getChild(event.getInventory()));
                        }
                    }

                }catch(NullPointerException npe){} //Not an item or doesn't have a display name.

                event.setCancelled(true);
                return;
            }
        }
    }
}
