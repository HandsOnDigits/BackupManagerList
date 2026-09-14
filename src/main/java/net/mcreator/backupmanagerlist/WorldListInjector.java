package net.mcreator.backupmanagerlist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelSummary;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.lang.reflect.Field;

@EventBusSubscriber(
        modid = "backup_manager_list",
        bus = EventBusSubscriber.Bus.GAME,
        value = Dist.CLIENT
)
public class WorldListInjector {

    private static Button backupButton;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof SelectWorldScreen selectWorldScreen) {

            // Create the Backups button placed in the bottom toolbar
            backupButton = Button.builder(
                    Component.literal("Backups"),
                    button -> openBackupScreenForSelected(selectWorldScreen)
            ).bounds(selectWorldScreen.width / 2 + 155, selectWorldScreen.height - 52, 70, 20).build();

            // Disabled by default until a world is selected
            backupButton.active = false;

            event.addListener(backupButton);
        }
    }

    @SubscribeEvent
    public static void onScreenTick(ScreenEvent.Render.Pre event) {
        // Continuously check if a world slot is selected to activate the button
        if (event.getScreen() instanceof SelectWorldScreen selectWorldScreen && backupButton != null) {
            selectWorldScreen.children().stream()
                    .filter(WorldSelectionList.class::isInstance)
                    .map(WorldSelectionList.class::cast)
                    .findFirst()
                    .ifPresent(worldList -> {
                        WorldSelectionList.Entry selected = worldList.getSelected();
                        backupButton.active = (selected instanceof WorldSelectionList.WorldListEntry);
                    });
        }
    }

    private static void openBackupScreenForSelected(SelectWorldScreen screen) {
        screen.children().stream()
                .filter(WorldSelectionList.class::isInstance)
                .map(WorldSelectionList.class::cast)
                .findFirst()
                .ifPresent(worldList -> {
                    WorldSelectionList.Entry selected = worldList.getSelected();
                    if (selected instanceof WorldSelectionList.WorldListEntry worldEntry) {
                        LevelSummary summary = extractSummary(worldEntry);
                        
                        String worldFolder = (summary != null) ? summary.getLevelId() : "unknown_world";
                        String worldDisplayName = (summary != null) ? summary.getLevelName() : worldFolder;

                        Minecraft.getInstance().setScreen(new BackupScreen(worldDisplayName));
                    }
                });
    }

    private static LevelSummary extractSummary(WorldSelectionList.WorldListEntry entry) {
        for (Field field : entry.getClass().getDeclaredFields()) {
            if (field.getType().equals(LevelSummary.class)) {
                try {
                    field.setAccessible(true);
                    return (LevelSummary) field.get(entry);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }
}