package com.vkbao.remotemm.model;

import android.app.Application;

import androidx.annotation.NonNull;

public class VolumeModel {
    private int speaker;
    private int recorder;

    public int getSpeaker() {
        return speaker;
    }

    public void setSpeaker(int speaker) {
        this.speaker = speaker;
    }

    public int getRecorder() {
        return recorder;
    }

    public void setRecorder(int recorder) {
        this.recorder = recorder;
    }

    public VolumeModel(int speaker, int recorder) {
        this.speaker = speaker;
        this.recorder = recorder;
    }
}
