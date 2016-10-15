package com.sedmelluq.discord.lavaplayer.track;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioTrackExecutor;
import com.sedmelluq.discord.lavaplayer.track.playback.PrimordialAudioTrackExecutor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract base for all audio tracks with an executor
 */
public abstract class BaseAudioTrack implements InternalAudioTrack {
  private final PrimordialAudioTrackExecutor initialExecutor;
  private final AtomicBoolean executorAssigned;
  private volatile AudioTrackExecutor activeExecutor;
  protected final AudioTrackInfo trackInfo;
  protected final AtomicLong accurateDuration;

  /**
   * @param trackInfo Track info
   */
  public BaseAudioTrack(AudioTrackInfo trackInfo) {
    this.initialExecutor = new PrimordialAudioTrackExecutor(trackInfo);
    this.executorAssigned = new AtomicBoolean();
    this.activeExecutor = null;
    this.trackInfo = trackInfo;
    this.accurateDuration = new AtomicLong();
  }

  @Override
  public void assignExecutor(AudioTrackExecutor executor) {
    if (executorAssigned.compareAndSet(false, true)) {
      initialExecutor.applyStateToExecutor(executor);
      activeExecutor = executor;
    } else {
      throw new IllegalStateException("Cannot assign executors to a track twice.");
    }
  }

  @Override
  public AudioTrackExecutor getActiveExecutor() {
    AudioTrackExecutor executor = activeExecutor;
    return executor != null ? executor : initialExecutor;
  }

  @Override
  public void stop() {
    getActiveExecutor().stop();
  }

  @Override
  public AudioTrackState getState() {
    return getActiveExecutor().getState();
  }

  @Override
  public String getIdentifier() {
    return trackInfo.identifier;
  }

  @Override
  public long getPosition() {
    return getActiveExecutor().getPosition();
  }

  @Override
  public void setPosition(long position) {
    getActiveExecutor().setPosition(position);
  }

  @Override
  public void setLoop(AudioLoop loop) {
    getActiveExecutor().setLoop(loop);
  }

  @Override
  public AudioFrame provide() {
    return getActiveExecutor().provide();
  }

  @Override
  public AudioTrackInfo getInfo() {
    return trackInfo;
  }

  @Override
  public long getDuration() {
    long accurate = accurateDuration.get();

    if (accurate == 0) {
      return trackInfo.length * 1000L;
    } else {
      return accurate;
    }
  }

  @Override
  public AudioTrack makeClone() {
    throw new UnsupportedOperationException();
  }

  @Override
  public AudioSourceManager getSourceManager() {
    return null;
  }
}