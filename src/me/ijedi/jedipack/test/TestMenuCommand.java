package me.ijedi.jedipack.test;

import me.ijedi.jedipack.menu.Menu;
import me.ijedi.jedipack.menu.MenuManager;
import me.ijedi.jedipack.parkour.ParkourCourse;
import me.ijedi.jedipack.parkour.ParkourManager;
import me.ijedi.jedipack.parkour.ParkourMenuEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestMenuCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if(commandSender instanceof Player){
            Player player = (Player)commandSender;
            ParkourCourse course = ParkourManager.getCourse("123");
            Menu menu = ParkourMenuEvent.getMenuFromCourse("123", course);
            player.openInventory(new MenuManager().getMenu(menu.getName()));

        }
        else{
            commandSender.sendMessage("This can only be ran by a player!");
        }

        return true;
    }
}
