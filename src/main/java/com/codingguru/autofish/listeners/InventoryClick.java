package com.codingguru.autofish.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.autofish.AntiAutoFish;
import com.codingguru.autofish.handlers.CaptchaHandler;
import com.codingguru.autofish.handlers.CaptchaHolder;
import com.codingguru.autofish.util.ColorUtil;

public class InventoryClick implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player))
            return;

        // Only act on the captcha GUI.
        if (!(e.getInventory().getHolder() instanceof CaptchaHolder))
            return;

        final Player player = (Player) e.getWhoClicked();

        // Lock down every click while the captcha is open (including the player's own
        // inventory).
        e.setCancelled(true);

        // Only a click inside the captcha inventory itself can solve it.
        if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof CaptchaHolder))
            return;

        final ItemStack clickedItem = e.getCurrentItem();
        final Material targetItem = CaptchaHandler.getInstance().getTargetItem();

        if (clickedItem != null && clickedItem.getType() == targetItem) {

            player.sendMessage(ColorUtil.format(AntiAutoFish.getInstance().getConfig()
                    .getString("fishing-captcha.success-message", "You have completed the captcha!")));
            CaptchaHandler.getInstance().completeCaptcha(player);

        }

    }

}
