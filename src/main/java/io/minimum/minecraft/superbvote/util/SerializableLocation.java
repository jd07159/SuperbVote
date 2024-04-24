package io.minimum.minecraft.superbvote.util;

import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@Value
public class SerializableLocation {
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public Location getBukkitLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public static SerializableLocation fromLocation(final @NotNull Location location) {
        return new SerializableLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
