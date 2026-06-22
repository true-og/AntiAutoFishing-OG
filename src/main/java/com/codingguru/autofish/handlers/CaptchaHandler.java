package com.codingguru.autofish.handlers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.codingguru.autofish.AntiAutoFish;
import com.codingguru.autofish.util.ColorUtil;
import com.codingguru.autofish.util.ConsoleUtil;

import net.kyori.adventure.text.Component;

public class CaptchaHandler {

    private final static CaptchaHandler INSTANCE = new CaptchaHandler();
    private static final Set<UUID> activeCaptchas = new HashSet<>();

    public void openCaptcha(Player player) {

        // Skip if the captcha GUI is already open for this player.
        if (activeCaptchas.contains(player.getUniqueId())
                && player.getOpenInventory().getTopInventory().getHolder() instanceof CaptchaHolder)
            return;

        AntiAutoFish plugin = AntiAutoFish.getInstance();
        Component title = ColorUtil.format(plugin.getConfig().getString("fishing-captcha.inventory-name", "Verify!"));

        Material targetItem = getTargetItem();
        int size = plugin.getConfig().getInt("fishing-captcha.inventory-size", 36);
        Inventory inv = Bukkit.createInventory(new CaptchaHolder(), size, title);

        int correctSlot = ThreadLocalRandom.current().nextInt(size);

        for (int i = 0; i < size; i++) {

            inv.setItem(i, new ItemStack(i == correctSlot ? targetItem : Material.AIR));

        }

        player.openInventory(inv);

        // Mark active only after the GUI is actually open so a failed open cannot brick
        // the player's fishing.
        activeCaptchas.add(player.getUniqueId());
        // Reset the timing sample so a fresh, post-verification window is collected
        // regardless of how the captcha ends.
        TimingHandler.getInstance().clear(player.getUniqueId());

    }

    public Material getTargetItem() {

        String itemName = AntiAutoFish.getInstance().getConfig().getString("fishing-captcha.captcha-item", "EMERALD");
        Material targetItem;

        try {

            targetItem = Material.valueOf(itemName.toUpperCase());

        } catch (IllegalArgumentException e) {

            ConsoleUtil.warning("Invalid item type in config: " + itemName + ". Defaulting to EMERALD.");
            targetItem = Material.EMERALD;

        }

        return targetItem;

    }

    public boolean hasPendingCaptcha(Player player) {

        return activeCaptchas.contains(player.getUniqueId());

    }

    public void removeCaptcha(Player player) {

        activeCaptchas.remove(player.getUniqueId());

    }

    public void completeCaptcha(Player player) {

        activeCaptchas.remove(player.getUniqueId());
        PlayerHandler.getInstance().removeFishingData(player.getUniqueId());
        TimingHandler.getInstance().clear(player.getUniqueId());
        player.closeInventory();

    }

    public static CaptchaHandler getInstance() {

        return INSTANCE;

    }

}