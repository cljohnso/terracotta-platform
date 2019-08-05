/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package com.terracottatech.nomad.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.terracottatech.nomad.server.NomadServerMode;

public class DiscoverResponse<T> {
  private final NomadServerMode mode;
  private final long mutativeMessageCount;
  private final String lastMutationHost;
  private final String lastMutationUser;
  private final long currentVersion;
  private final long highestVersion;
  private ChangeDetails<T> latestChange;

  @JsonCreator
  public DiscoverResponse(@JsonProperty("mode") NomadServerMode mode,
                          @JsonProperty("mutativeMessageCount") long mutativeMessageCount,
                          @JsonProperty("lastMutationHost") String lastMutationHost,
                          @JsonProperty("lastMutationUser") String lastMutationUser,
                          @JsonProperty("currentVersion") long currentVersion,
                          @JsonProperty("highestVersion") long highestVersion,
                          @JsonProperty("latestChange") ChangeDetails<T> latestChange) {
    this.mode = mode;
    this.mutativeMessageCount = mutativeMessageCount;
    this.lastMutationHost = lastMutationHost;
    this.lastMutationUser = lastMutationUser;
    this.currentVersion = currentVersion;
    this.highestVersion = highestVersion;
    this.latestChange = latestChange;
  }

  public NomadServerMode getMode() {
    return mode;
  }

  public long getMutativeMessageCount() {
    return mutativeMessageCount;
  }

  public String getLastMutationHost() {
    return lastMutationHost;
  }

  public String getLastMutationUser() {
    return lastMutationUser;
  }

  public long getCurrentVersion() {
    return currentVersion;
  }

  public long getHighestVersion() {
    return highestVersion;
  }

  public ChangeDetails<T> getLatestChange() {
    return latestChange;
  }
}
