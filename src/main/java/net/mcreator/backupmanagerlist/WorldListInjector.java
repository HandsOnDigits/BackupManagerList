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
        if (event.getScreen() instanceof SelectWorldScreen selectWorldScreen) {
            entryButtons.clear();

            selectWorldScreen.children().stream()
                    .filter(WorldSelectionList.class::isInstance)
                    .map(WorldSelectionList.class::cast)
                    .findFirst()
                    .ifPresent(worldList -> {
                        for (WorldSelectionList.Entry entry : worldList.children()) {
                            if (entry instanceof WorldSelectionList.WorldListEntry worldEntry) {

                                LevelSummary summary = extractSummary(worldEntry);
                                String worldName = (summary != null) ? summary.getLevelName() : "Unknown World";

                                Button backupBtn = Button.builder(
                                        Component.literal("Backups"),
                                        button -> Minecraft.getInstance().setScreen(new BackupScreen(worldName))
                                ).bounds(0, 0, 55, 16).build();

                                entryButtons.put(worldEntry, backupBtn);
                            }
                        }
                    });
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

                    int listTop = worldList.getY();
                    int listBottom = listTop + worldList.getHeight();
                    double scrollAmount = worldList.getScrollAmount();

                    // Fixed: World slot height in Minecraft is 36 pixels
                    int itemHeight = 36;
                    int rowTopOffset = listTop + 4 - (int) scrollAmount;

                    for (int i = 0; i < worldList.children().size(); i++) {
                        WorldSelectionList.Entry entry = worldList.children().get(i);
                        if (entry instanceof WorldSelectionList.WorldListEntry worldEntry) {
                            Button button = entryButtons.get(worldEntry);

                            if (button != null) {
                                int entryTop = rowTopOffset + (i * itemHeight);
                                int entryLeft = worldList.getRowLeft();
                                int entryWidth = worldList.getRowWidth();

                                int buttonX = entryLeft + entryWidth - 60;
                                int buttonY = entryTop + 10;

                                // Render button only when visible within scroll boundaries
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