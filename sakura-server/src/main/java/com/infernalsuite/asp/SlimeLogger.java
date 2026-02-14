package com.infernalsuite.asp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlimeLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("SlimeWorldManager");

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void debug(String message) {
        LOGGER.debug(message);
    }

}
