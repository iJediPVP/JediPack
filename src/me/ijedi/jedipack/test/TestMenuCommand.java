package me.ijedi.jedipack.test;

import me.ijedi.menulibrary.MenuManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import me.ijedi.menulibrary.Menu;
import org.bukkit.inventory.meta.ItemMeta;

public class TestMenuCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if(commandSender instanceof Player){
            Player player = (Player)commandSender;
            PlayerInventory inv = player.getInventory();

            Menu menu = new Menu("Test menu");

            ItemStack[] items = inv.getContents();
            menu.setContents(items);

            ItemStack exitButton = new ItemStack(Material.STONE);
            ItemMeta exitMeta = exitButton.getItemMeta();
            exitMeta.setDisplayName("Exit");
            exitButton.setItemMeta(exitMeta);

            ItemStack nextButton = new ItemStack(Material.STONE);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName("Next");
            nextButton.setItemMeta(nextMeta);

            ItemStack prevButton = new ItemStack(Material.STONE);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName("Prev");
            prevButton.setItemMeta(prevMeta);

            menu.setButtons(exitButton, prevButton, nextButton);
            player.openInventory(new MenuManager().getMenu("Test menu"));
        }
        else{
            commandSender.sendMessage("This can only be ran by a player!");
        }

        return true;
    }
}
