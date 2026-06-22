# AntiAutoFishing-OG

TrueOG Network's fork of AntiAutoFishing - prevent auto fishing mods.

## Changes from upstream

- **Catches bots that move around.** New timing checks flag inhuman catch intervals and lightning-fast, robotic reel-in reaction times, so auto-fishers are caught even when they shuffle position to dodge the original same-spot check.
- **The captcha actually gates fishing.** Flagged players must solve the captcha before they can keep fishing (toggle with `block-fishing-until-solved`).
- **Captcha can no longer be cheated** by clicking the target item in your own inventory.
- **New `autofish.bypass` permission** to exempt staff or trusted players from all checks.
- **Messages use MiniMessage formatting** (e.g. `<red>`, `<green>`) instead of old `&` color codes.
- **No more "phone home".** The SpigotMC update check (and its outbound web request) has been removed.
- **Cleaned-up config** with saner defaults.

Gradle Version: 8.14.3

Java Version: 17

Minecraft API: 1.19

API Jar: 1.19.4 Purpur

AntiAutoFishing-OG is released under the terms of the [GNU General Public License v3.0](https://github.com/true-og/AntiAutoFishing-OG/blob/main/LICENSE).
