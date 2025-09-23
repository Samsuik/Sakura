package me.samsuik.sakura.configuration.local;

import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public interface Container<K, V> {
    Map<K, V> contents();

    Container<K, V> open();

    Container<K, V> seal();
}
