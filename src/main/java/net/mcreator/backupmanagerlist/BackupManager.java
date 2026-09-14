package net.mcreator.backupmanagerlist;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BackupManager {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy — HH:mm");

    public record BackupEntry(
            String date,
            String name,
            String size,
            Path path
    ) {}

    public static List<BackupEntry> loadBackups(String worldName, String worldFolderId) {
        List<BackupEntry> backups = new ArrayList<>();

        File runDir = Minecraft.getInstance().gameDirectory;
        BackupConfig.ConfigData config = BackupConfig.getConfig();

        for (String pattern : config.searchPaths) {
            String resolvedRelativePath = pattern
                    .replace("{world}", worldName)
                    .replace("{world_id}", worldFolderId != null ? worldFolderId : worldName);

            Path targetFolder = Paths.get(runDir.getAbsolutePath(), resolvedRelativePath);

            if (Files.exists(targetFolder)) {
                scanFolderForZips(targetFolder, backups);
            }
        }

        backups.sort((b1, b2) -> Long.compare(b2.path().toFile().lastModified(), b1.path().toFile().lastModified()));

        return backups;
    }

    private static void scanFolderForZips(Path folder, List<BackupEntry> backups) {
        try (Stream<Path> stream = Files.list(folder)) {
            stream.filter(path -> path.toString().endsWith(".zip"))
                    .forEach(path -> {
                        if (backups.stream().noneMatch(e -> e.path().equals(path))) {
                            try {
                                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

                                LocalDateTime date = LocalDateTime.ofInstant(
                                        attr.lastModifiedTime().toInstant(),
                                        ZoneId.systemDefault()
                                );

                                String formattedDate = date.format(DATE_FORMATTER);
                                String fileName = path.getFileName().toString();
                                String fileSize = formatFileSize(attr.size());

                                backups.add(new BackupEntry(formattedDate, fileName, fileSize, path));
                            } catch (IOException ignored) {
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(bytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}