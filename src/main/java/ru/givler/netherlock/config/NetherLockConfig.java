package ru.givler.netherlock.config;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class NetherLockConfig {
    public static boolean canCreatePortal = false;
    public static boolean canTeleport = false;

    public static void load(File file) {
        Configuration config = new Configuration(file);
        config.load();
        canCreatePortal = config.getBoolean("canCreatePortal", "general", true, "Allow portal creation");
        canTeleport = config.getBoolean("canTeleport", "general", true, "Allow portal teleportation");
        if (config.hasChanged()) config.save();
    }
}

