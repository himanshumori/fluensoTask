package com.fluencsotest.models;

import java.io.File;

public abstract class ConvEntityItem {

    private String voiceText;
    private String voiceFilePath;
    private boolean showText;
    private int conversationState = 1; // default State
    private int playerState = 1; // default State Stopped

    public String getVoiceText() {
        return voiceText;
    }

    public void setVoiceText(String voiceText) {
        this.voiceText = voiceText;
    }

    public String getVoiceFilePath() {
        return voiceFilePath;
    }

    public void setVoiceFilePath(String voiceFilePath) {
        this.voiceFilePath = voiceFilePath;
    }

    public boolean isShowText() {
        return showText;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    public int getConversationState() {
        return conversationState;
    }

    public void setConversationState(int conversationState) {
        this.conversationState = conversationState;
    }

    public int getPlayerState() {
        return playerState;
    }

    public void setPlayerState(int playerState) {
        this.playerState = playerState;
    }
}
