package com.hughbone.creepertag;

public interface CreeperAccessor {
    boolean getIsTagger();
    void setSpawnSource(String sourcePlayerName);
    String getSpawnSource();
}
