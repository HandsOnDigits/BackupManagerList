package net.mcreator.backupmanagerlist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class BackupScreen extends Screen {

    private final Screen lastScreen;
    private final String worldDisplayName;
    private final String worldFolderId;
    private final List<BackupManager.BackupEntry> backups;

    private BackupList backupList;
    private Button restoreButton;

    public BackupScreen(Screen lastScreen, String worldDisplayName, String worldFolderId) {
        super(Component.literal("Backups"));
        this.lastScreen = lastScreen;
        this.worldDisplayName = worldDisplayName;
        this.worldFolderId = worldFolderId;
        
        this.backups = BackupManager.loadBackups(worldFolderId);
    }

    @Override
    public void onClose() {
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

        // Pass explicit item height of 30px
        this.backupList = new BackupList(
                this.minecraft,
                listWidth,
                listHeight,
                listTop,
                30
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
                Component.literal(this.worldDisplayName),
                this.width / 2,
                30,
                0xAAAAAA
        );
    }

    private void restoreSelectedBackup() {
        BackupManager.BackupEntry selected = this.backupList.getSelectedBackup();
        if (selected == null || this.minecraft == null) {
            return;
        }

        this.minecraft.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        boolean success = BackupRestorer.restoreBackup(this.worldFolderId, selected.path());
                        
                        if (success) {
                            SystemToast.add(
                                    this.minecraft.getToasts(),
                                    SystemToast.SystemToastId.NARRATOR_TOGGLE,
                                    Component.literal("Backup Restored"),
                                    Component.literal("Successfully loaded §l" + selected.name())
                            );
                        } else {
                            SystemToast.add(
                                    this.minecraft.getToasts(),
                                    SystemToast.SystemToastId.NARRATOR_TOGGLE,
                                    Component.literal("Restore Failed"),
                                    Component.literal("Could not unpack §l" + selected.name())
                            );
                        }
                    }
                    this.minecraft.setScreen(this);
                },
                Component.literal("Restore Backup?"),
                Component.literal("Are you sure you want to overwrite world folder §l'" + this.worldFolderId + "'§r with §l'" + selected.name() + "'§r?\n Current world progress will be lost."),
                Component.literal("Restore"),
                Component.literal("Cancel")
        ));
    }

    private void selectBackup(BackupManager.BackupEntry backup) {
        this.restoreButton.active = backup != null;
    }

    private class BackupList extends ObjectSelectionList<BackupList.Entry> {

        public BackupList(
                Minecraft minecraft,
                int width,
                int height,
                int top,
                int itemHeight
        ) {
            super(minecraft, width, height, top, itemHeight);
            for (BackupManager.BackupEntry backup : backups) {
                this.addEntry(new Entry(backup));
            }
        }

        public BackupManager.BackupEntry getSelectedBackup() {
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

            private final BackupManager.BackupEntry backup;

            public Entry(BackupManager.BackupEntry backup) {
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

                // Line 1 (Top): Date on left, Size on right
                int line1Y = top + 3;
                guiGraphics.drawString(
                        font,
                        backup.date(),
                        left + 5,
                        line1Y,
                        0xFFFFFF
                );

                guiGraphics.drawString(
                        font,
                        backup.size(),
                        left + width - font.width(backup.size()) - 5,
                        line1Y,
                        0x888888
                );

                // Line 2 (Bottom): Zip filename
                int line2Y = top + 15;
                guiGraphics.drawString(
                        font,
                        backup.name(),
                        left + 5,
                        line2Y,
                        0xAAAAAA
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