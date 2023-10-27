package ru.seliselev.investor.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyLogger {
    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    public static void info(String msg) {
        logger.info(msg);
    }
}
