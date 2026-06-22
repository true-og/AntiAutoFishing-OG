package com.codingguru.autofish.util;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import com.codingguru.autofish.AntiAutoFish;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ConsoleUtil {

    private final static ConsoleCommandSender CONSOLE = Bukkit.getServer().getConsoleSender();

    private static final String SEPARATOR = "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=";

    public static void sendPluginSetup() {

        CONSOLE.sendMessage(Component.text(SEPARATOR, NamedTextColor.GREEN));
        CONSOLE.sendMessage(entry("Plugin Name: ", "AntiAutoFishing"));
        CONSOLE.sendMessage(entry("Plugin Version: ", AntiAutoFish.getInstance().getPluginMeta().getVersion()));
        CONSOLE.sendMessage(entry("Server Version: ", Bukkit.getBukkitVersion()));
        CONSOLE.sendMessage(entry("Author: ", "CodingGuru"));
        CONSOLE.sendMessage(entry("Discord: ", "https://discord.gg/CbJxH5NPvX"));
        CONSOLE.sendMessage(Component.text(SEPARATOR, NamedTextColor.GREEN));

    }

    private static Component entry(String label, String value) {

        return Component.text(label, NamedTextColor.GREEN).append(Component.text(value, NamedTextColor.YELLOW));

    }

    public static void info(String message) {

        CONSOLE.sendMessage(Component.text(message));

    }

    public static void warning(String message) {

        CONSOLE.sendMessage(Component.text("[WARNING] " + message, NamedTextColor.YELLOW));

    }

}
