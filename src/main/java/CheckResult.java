package com.example;

public class CheckResult {
    public final String name;
    public final Level level;
    public final String message;

    public CheckResult(String name, Level level, String message) {
        this.name = name;
        this.level = level;
        this.message = message;
    }

    public boolean isOk() {
        return level == Level.OK;
    }
}
