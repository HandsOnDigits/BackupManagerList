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

    private final String worldName;
    private final List<BackupEntry> backups;

    private BackupList backupList;
    private Button restoreButton;

    public BackupScreen(String worldName) {
        super(Component.literal("Backups"));

        this.worldName = worldName;
        this.backups = createTestBackups();
    }

    @Override
    protected void init() {
        super.init();

        int listWidth = 500;
        int listHeight = this.height - 160;

        int listLeft = (this.width - listWidth) / 2;
        int listTop = 70;

        this.backupList = new BackupList(
                this.minecraft,
                listWidth,
                listHeight,
                listTop,
                listTop + listHeight
        );

        this.addRenderableWidget(this.backupList);

        this.restoreButton = Button.builder(
                Component.literal("Restore"),
                button -> restoreSelectedBackup()
        ).bounds(
                this.width / 2 - 105,
                this.height - 60,
                100,
                20
        ).build();

        this.restoreButton.active = false;

        this.addRenderableWidget(this.restoreButton);

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Cancel"),
                        button -> this.onClose()
                ).bounds(
                        this.width / 2 + 5,
                        this.height - 60,
                        100,
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

        guiGraphics.drawCenteredString(
                this.font,
                Component.literal("Backups"),
                this.width / 2,
                20,
                0xFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.literal(this.worldName),
                this.width / 2,
                42,
                0xAAAAAA
        );

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void restoreSelectedBackup() {
        BackupEntry selected = this.backupList.getSelectedBackup();

        if (selected == null) {
            return;
        }

        /*
         * Restore logic will go here later.
         *
         * For now we only have the UI.
         */
        System.out.println("Selected backup: " + selected.name());
    }

    private void selectBackup(BackupEntry backup) {
        this.restoreButton.active = backup != null;
    }

    private List<BackupEntry> createTestBackups() {
        List<BackupEntry> result = new ArrayList<>();

        result.add(new BackupEntry(
                "September 14, 2026 — 10:30",
                "backup_2026-09-14_10-30-00.zip",
                "2.41 GB"
        ));

        result.add(new BackupEntry(
                "September 14, 2026 — 10:00",
                "backup_2026-09-14_10-00-00.zip",
                "2.40 GB"
        ));

        result.add(new BackupEntry(
                "September 14, 2026 — 09:30",
                "backup_2026-09-14_09-30-00.zip",
                "2.39 GB"
        ));

        result.add(new BackupEntry(
                "September 14, 2026 — 09:00",
                "backup_2026-09-14_09-00-00.zip",
                "2.38 GB"
        ));

        result.add(new BackupEntry(
                "September 14, 2026 — 08:30",
                "backup_2026-09-14_08-30-00.zip",
                "2.37 GB"
        ));

        result.add(new BackupEntry(
                "September 14, 2026 — 08:00",
                "backup_2026-09-14_08-00-00.zip",
                "2.36 GB"
        ));

        return result;
    }

    public record BackupEntry(
            String date,
            String name,
            String size
    ) {
    }

    private class BackupList extends ObjectSelectionList<BackupList.Entry> {

	    public BackupList(
	            Minecraft minecraft,
	            int width,
	            int height,
	            int top,
	            int bottom
	    ) {
	        super(
	                minecraft,
	                width,
	                height,
	                top,
	                bottom
	        );
	
	        for (BackupEntry backup : backups) {
	            this.addEntry(new Entry(backup));
	        }
	    }
	
	    public BackupEntry getSelectedBackup() {
	        Entry selected = this.getSelected();
	
	        if (selected == null) {
	            return null;
	        }
	
	        return selected.backup;
	    }
	
	    @Override
	    public int getRowWidth() {
	        return 500;
	    }
	
	    @Override
	    protected int getScrollbarPosition() {
	        return this.getX() + this.width - 6;
	    }
	
	    private class Entry
	            extends ObjectSelectionList.Entry<Entry> {
	
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
	
	            guiGraphics.drawString(
	                    font,
	                    backup.date(),
	                    left + 10,
	                    top + 6,
	                    0xFFFFFF
	            );
	
	            guiGraphics.drawString(
	                    font,
	                    backup.name(),
	                    left + 10,
	                    top + 22,
	                    0xAAAAAA
	            );
	
	            guiGraphics.drawString(
	                    font,
	                    backup.size(),
	                    left + width - font.width(backup.size()) - 15,
	                    top + 6,
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
	            return Component.literal(
	                    backup.date() + ", " + backup.size()
	            );
	        }
	    }
	}
}
