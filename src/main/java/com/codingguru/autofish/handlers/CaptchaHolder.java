package com.codingguru.autofish.handlers;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Marker {@link InventoryHolder} used to identify the captcha GUI without
 * comparing (deprecated) inventory title strings. Any inventory whose holder is
 * an instance of this class is a captcha.
 */
public class CaptchaHolder implements InventoryHolder {

    @Override
    public Inventory getInventory() {

        return null;

    }

}
