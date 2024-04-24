package io.minimum.minecraft.superbvote.signboard;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.minimum.minecraft.superbvote.util.SerializableLocation;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TopPlayerSignStorage {
    private final Map<SerializableLocation, TopPlayerSign> signList = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public List<TopPlayerSign> getSignList() {
        return ImmutableList.copyOf(signList.values());
    }

    @Nullable
    public TopPlayerSign getSign(Location location) {
        return signList.get(SerializableLocation.fromLocation(location));
    }

    public void addSign(TopPlayerSign sign) {
        signList.put(sign.getSign(), sign);
    }

    public void removeSign(TopPlayerSign sign) {
        signList.remove(sign.getSign());
    }

    public void load(File file) throws IOException {
        if (file.exists()) {
            try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                List<TopPlayerSign> list = gson.fromJson(reader, new TypeToken<List<TopPlayerSign>>() {}.getType());
                list.forEach(this::addSign);
            }
        }
    }

    public void save(File file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(signList.values(), writer);
        }
    }
}
