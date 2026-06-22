package com.codingguru.autofish.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import com.codingguru.autofish.AntiAutoFish;
import com.codingguru.autofish.handlers.CaptchaHandler;
import com.codingguru.autofish.handlers.PlayerHandler;
import com.codingguru.autofish.handlers.TimingHandler;
import com.codingguru.autofish.model.PlayerFishingData;

public class PlayerFish implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerFish(PlayerFishEvent e) {

        if (e.isCancelled())
            return;

        final Player player = e.getPlayer();

        if (player.hasPermission("autofish.bypass"))
            return;

        final UUID uuid = player.getUniqueId();

        // Stamp the moment the fish bit, so the reel-in reaction time can be measured
        // on the catch.
        if (e.getState() == PlayerFishEvent.State.BITE) {

            TimingHandler.getInstance().recordBite(uuid, System.nanoTime());
            return;

        }

        // Everything below only concerns a successful fish catch, not reeled-in
        // entities/items.
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH)
            return;

        // Block all catches while an unsolved captcha is pending so the gate cannot be
        // ignored.
        if (CaptchaHandler.getInstance().hasPendingCaptcha(player)) {

            if (AntiAutoFish.getInstance().getConfig().getBoolean("fishing-captcha.block-fishing-until-solved", true))
                e.setCancelled(true);

            return;

        }

        // Record the catch timing for position-independent bot detection.
        final long now = System.nanoTime();
        TimingHandler.getInstance().recordCatch(uuid, now);
        TimingHandler.getInstance().recordReaction(uuid, now);

        final PlayerFishingData cachedData = PlayerHandler.getInstance().getFishingData(uuid, player.getLocation());
        boolean cancelled = cachedData.isFishCancelled(player);

        // Inhuman catch intervals or reel-in reaction times trip the captcha gate, even
        // if the bot moves around.
        if (TimingHandler.getInstance().isSuspicious(uuid)) {

            CaptchaHandler.getInstance().openCaptcha(player);
            cancelled = true;

        }

        e.setCancelled(cancelled);

    }

}
