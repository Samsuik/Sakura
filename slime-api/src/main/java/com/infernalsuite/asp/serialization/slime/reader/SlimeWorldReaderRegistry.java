package com.infernalsuite.asp.serialization.slime.reader;

import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SlimeWorldReaderRegistry {

    private final Map<String, Object> readers = new ConcurrentHashMap<>();

    public void registerReader(String key, Object reader) {
        readers.put(key, reader);
    }

    public Object getReader(String key) {
        return readers.get(key);
    }

    public static SlimeWorld readWorld(SlimeLoader loader, String worldName, byte[] serializedWorld, SlimePropertyMap propertyMap, boolean readOnly) {
        // Implementation: deserialize the world
        // This should delegate to the actual deserializer
        return null; // Placeholder
    }
}
