package net.mcreator.backupmanagerlist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(
        modid = "backup_manager_list",
        bus = EventBusSubscriber.Bus.GAME,
        value = Dist.CLIENT
)
public class WorldListInjector {

    private static final Map<WorldSelectionList.WorldListEntry, Button> entryButtons = new HashMap<>();

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof SelectWorldScreen) {
            entryButtons.clear();
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof SelectWorldScreen selectWorldScreen)) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int mouseX = event.getMouseX();
        int mouseY = event.getMouseY();

        selectWorldScreen.children().stream()
                .filter(WorldSelectionList.class::isInstance)
                .map(WorldSelectionList.class::cast)
                .findFirst()
                .ifPresent(worldList -> {

                    // Auto-refresh: Sync entry buttons if worlds were created, deleted, or re-loaded
                    syncButtonsWithList(selectWorldScreen, worldList);

                    int listTop = worldList.getY();
                    int listBottom = listTop + worldList.getHeight();
                    double scrollAmount = worldList.getScrollAmount();

                    int itemHeight = 36;
                    int rowTopOffset = listTop + 4 - (int) scrollAmount;

                    // Dynamically calculate X offset relative to the current right edge
                    int buttonX = worldList.getX() + worldList.getWidth() - 65;

                    for (int i = 0; i < worldList.children().size(); i++) {
                        WorldSelectionList.Entry entry = worldList.children().get(i);
                        if (entry instanceof WorldSelectionList.WorldListEntry worldEntry) {
                            Button button = entryButtons.get(worldEntry);

                            if (button != null) {
                                int entryTop = rowTopOffset + (i * itemHeight);
                                int buttonY = entryTop + 10;

                                if (buttonY >= listTop && (buttonY + 16) <= listBottom) {
                                    button.setX(buttonX);
                                    button.setY(buttonY);
                                    button.render(guiGraphics, mouseX, mouseY, event.getPartialTick());
                                }
                            }
                        }
                    }
                });
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof SelectWorldScreen && event.getButton() == 0) {
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();

            for (Button button : entryButtons.values()) {
                if (button.active && button.isMouseOver(mouseX, mouseY)) {
                    button.mouseClicked(mouseX, mouseY, event.getButton());
                    event.setCanceled(true);
                    break;
                }
            }
        }
    }

    private static void syncButtonsWithList(SelectWorldScreen selectWorldScreen, WorldSelectionList worldList) {
        entryButtons.keySet().removeIf(entry -> !worldList.children().contains(entry));

        for (WorldSelectionList.Entry entry : worldList.children()) {
            if (entry instanceof WorldSelectionList.WorldListEntry worldEntry && !entryButtons.containsKey(worldEntry)) {
                LevelSummary summary = extractSummary(worldEntry);
                
                // Extract both human-readable level name and system folder ID
                String worldDisplayName = (summary != null) ? summary.getLevelName() : "Unknown World";
                String worldFolderId = (summary != null) ? summary.getLevelId() : worldDisplayName;

                Button backupBtn = Button.builder(
                        Component.literal("Backups"),
                        button -> Minecraft.getInstance().setScreen(
                                new BackupScreen(selectWorldScreen, worldDisplayName, worldFolderId)
                        )
                ).bounds(0, 0, 50, 16).build();

                entryButtons.put(worldEntry, backupBtn);
            }
        }
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