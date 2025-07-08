package com.example.langep;

public class Lang {
    private String name;
    private String code;

    public Lang(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    // For Spinner display
    @Override
    public String toString() {
        return name;
    }
}
