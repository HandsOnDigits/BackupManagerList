package net.mcreator.backupmanagerlist;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BackupManager {

    public record BackupEntry(String name, String date, String size, Path path) {}

    public static List<BackupEntry> loadBackups(String worldFolderId) {
        List<BackupEntry> backups = new ArrayList<>();
        File runDir = Minecraft.getInstance().gameDirectory;
        BackupConfig.ConfigData config = BackupConfig.getConfig();

        for (String pattern : config.searchPaths) {
            String resolvedPath = pattern.replace("{world_id}", worldFolderId);
            Path targetFolder = Paths.get(runDir.getAbsolutePath(), resolvedPath);

            if (Files.exists(targetFolder)) {
                scanFolderForZips(targetFolder, worldFolderId, backups);
            }
        }

        backups.sort((b1, b2) -> Long.compare(b2.path().toFile().lastModified(), b1.path().toFile().lastModified()));

        return backups;
    }

    private static void scanFolderForZips(Path folder, String worldFolderId, List<BackupEntry> backups) {
        try (var stream = Files.list(folder)) {
            stream.filter(path -> path.toString().endsWith(".zip"))
                    .filter(path -> isBackupForWorld(path, worldFolderId))
                    .forEach(path -> {
                        if (backups.stream().noneMatch(e -> e.path().equals(path))) {
                            try {
                                File file = path.toFile();
                                String name = file.getName();
                                String size = formatFileSize(file.length());
                                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm")
                                        .format(new Date(file.lastModified()));

                                backups.add(new BackupEntry(name, date, size, path));
                            } catch (Exception ignored) {
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace(); // Fixed: Added missing ()
        }
    }

    private static boolean isBackupForWorld(Path zipPath, String worldFolderId) {
        String fileName = zipPath.getFileName().toString().toLowerCase();
        String targetWorldId = worldFolderId.toLowerCase();

        // 1. Native MC zip name pattern check: <worldFolderId>-yyyy-MM-dd-HH-mm-ss.zip
        if (fileName.startsWith(targetWorldId + "-") || fileName.startsWith(targetWorldId + "_") || fileName.equals(targetWorldId + ".zip")) {
            return true;
        }

        // 2. Inspect ZIP entries for root level vs nested folder structure
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                // Nested folder check: e.g., "world_id/level.dat"
                if (entryName.startsWith(worldFolderId + "/") || entryName.startsWith(worldFolderId + "\\")) {
                    return true;
                }

                // Root-level check: level.dat exists directly at root, verify via filename match
                if (entryName.equals("level.dat")) {
                    return fileName.contains(targetWorldId);
                }
            }
        } catch (IOException ignored) {
        }

        return false;
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }
}