package net.mcreator.backupmanagerlist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BackupScreen extends Screen {

    private final Screen lastScreen;
    private final String worldName;
    private final List<BackupEntry> backups;

    private BackupList backupList;
    private Button restoreButton;

    public BackupScreen(Screen lastScreen, String worldName) {
        super(Component.literal("Backups - " + worldName));
        this.lastScreen = lastScreen;
        this.worldName = worldName;
        this.backups = createTestBackups();
    }

    @Override
    public void onClose() {
        // Return to the previous screen (SelectWorldScreen) instead of kicking to main menu
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
        }
    }

    @Override
    protected void init() {
        super.init();

        int listWidth = Math.min(500, (int) (this.width * 0.9));
        int listTop = 50;
        int listBottom = this.height - 50;
        int listHeight = listBottom - listTop;

        this.backupList = new BackupList(
                this.minecraft,
                listWidth,
                listHeight,
                listTop,
                listBottom
        );
        this.backupList.setX((this.width - listWidth) / 2);

        this.addRenderableWidget(this.backupList);

        int buttonY = this.height - 35;
        int buttonWidth = 100;
        int buttonSpacing = 10;

        this.restoreButton = Button.builder(
                Component.literal("Restore"),
                button -> restoreSelectedBackup()
        ).bounds(
                this.width / 2 - buttonWidth - (buttonSpacing / 2),
                buttonY,
                buttonWidth,
                20
        ).build();

        this.restoreButton.active = false;
        this.addRenderableWidget(this.restoreButton);

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Cancel"),
                        button -> this.onClose()
                ).bounds(
                        this.width / 2 + (buttonSpacing / 2),
                        buttonY,
                        buttonWidth,
                        20
                ).build()
        );
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(
                this.font,
                Component.literal("Backups"),
                this.width / 2,
                15,
                0xFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.literal(this.worldName),
                this.width / 2,
                30,
                0xAAAAAA
        );
    }

    private void restoreSelectedBackup() {
        BackupEntry selected = this.backupList.getSelectedBackup();
        if (selected == null) {
            return;
        }
        System.out.println("Selected backup: " + selected.name());
    }

    private void selectBackup(BackupEntry backup) {
        this.restoreButton.active = backup != null;
    }

    private List<BackupEntry> createTestBackups() {
        List<BackupEntry> result = new ArrayList<>();
        result.add(new BackupEntry("September 14, 2026 — 10:30", "backup_2026-09-14_10-30-00.zip", "2.41 GB"));
        result.add(new BackupEntry("September 14, 2026 — 10:00", "backup_2026-09-14_10-00-00.zip", "2.40 GB"));
        result.add(new BackupEntry("September 14, 2026 — 09:30", "backup_2026-09-14_09-30-00.zip", "2.39 GB"));
        return result;
    }

    public record BackupEntry(String date, String name, String size) {}

    private class BackupList extends ObjectSelectionList<BackupList.Entry> {

        public BackupList(
                Minecraft minecraft,
                int width,
                int height,
                int top,
                int bottom
        ) {
            super(minecraft, width, height, top, bottom);
            for (BackupEntry backup : backups) {
                this.addEntry(new Entry(backup));
            }
        }

        public BackupEntry getSelectedBackup() {
            Entry selected = this.getSelected();
            return selected != null ? selected.backup : null;
        }

        @Override
        public int getRowWidth() {
            return this.width - 20;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getX() + this.width - 6;
        }

        private class Entry extends ObjectSelectionList.Entry<Entry> {

            private final BackupEntry backup;

            public Entry(BackupEntry backup) {
                this.backup = backup;
            }

            @Override
            public void render(
                    GuiGraphics guiGraphics,
                    int index,
                    int top,
                    int left,
                    int width,
                    int height,
                    int mouseX,
                    int mouseY,
                    boolean hovered,
                    float partialTick
            ) {
                if (hovered || BackupList.this.getSelected() == this) {
                    guiGraphics.fill(
                            left,
                            top,
                            left + width,
                            top + height,
                            0x803F3F3F
                    );
                }

                int textY1 = top + (height / 2) - 12;
                int textY2 = top + (height / 2) + 2;

                guiGraphics.drawString(
                        font,
                        backup.date(),
                        left + 5,
                        textY1,
                        0xFFFFFF
                );

                guiGraphics.drawString(
                        font,
                        backup.name(),
                        left + 5,
                        textY2,
                        0xAAAAAA
                );

                guiGraphics.drawString(
                        font,
                        backup.size(),
                        left + width - font.width(backup.size()) - 5,
                        textY1,
                        0xFFFFFF
                );
            }

            @Override
            public boolean mouseClicked(
                    double mouseX,
                    double mouseY,
                    int button
            ) {
                if (button == 0) {
                    BackupList.this.setSelected(this);
                    selectBackup(this.backup);
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public Component getNarration() {
                return Component.literal(backup.date() + ", " + backup.size());
            }
        }
    }
}