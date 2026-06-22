package com.codingguru.autofish.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.codingguru.autofish.handlers.CaptchaHandler;
import com.codingguru.autofish.handlers.PlayerHandler;
import com.codingguru.autofish.handlers.TimingHandler;

public class PlayerQuit implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {

        CaptchaHandler.getInstance().removeCaptcha(e.getPlayer());
        PlayerHandler.getInstance().removeFishingData(e.getPlayer().getUniqueId());
        TimingHandler.getInstance().clear(e.getPlayer().getUniqueId());

    }

}