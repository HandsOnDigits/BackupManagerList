package net.mcreator.backupmanagerlist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class UI {

    private static boolean openedOnStartup = false;

    @EventBusSubscriber(
            modid = "backup_manager_list",
            bus = EventBusSubscriber.Bus.GAME,
            value = Dist.CLIENT
    )
    public static class GameEvents {

        @SubscribeEvent
        public static void onScreenInit(ScreenEvent.Init.Post event) {
            // Check if the screen opening is the main title screen and we haven't opened yet
            if (!openedOnStartup && event.getScreen() instanceof TitleScreen) {
                openedOnStartup = true;

                // Deferred execution ensures Minecraft finishes initializing the title screen before replacing it
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().setScreen(new BackupScreen("TEST WORLD"));
                });
            }
        }
    }
}