/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package com.terracottatech.dynamic_config.model;

import com.terracottatech.dynamic_config.util.IParameterSubstitutor;
import com.terracottatech.dynamic_config.util.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.toMap;

/**
 * Parses CLI or config file into a validated cluster object
 */
public class ClusterFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClusterFactory.class);

  private final IParameterSubstitutor parameterSubstitutor;

  public ClusterFactory(IParameterSubstitutor parameterSubstitutor) {
    this.parameterSubstitutor = parameterSubstitutor;
  }

  /**
   * Creates a {@code Cluster} object from a config properties file
   *
   * @param configFile  the path to the config properties file, non-null
   * @param clusterName the intended name of the cluster, may be null
   * @return a {@code Cluster} object
   */
  public Cluster create(Path configFile, String clusterName) {
    return create(Props.load(configFile), clusterName);
  }

  public Cluster create(Properties properties, String clusterName) {
    Collection<Configuration> defaultsAdded = new TreeSet<>(Comparator.comparing(Configuration::toString));
    Cluster cluster = ConfigurationParser.parsePropertyConfiguration(parameterSubstitutor, properties, defaultsAdded::add);

    if (clusterName != null) {
      cluster.setName(clusterName);
    }

    LOGGER.info(
        String.format(
            "%sRead the following configurations: %s%sAdded the following defaults: %s",
            lineSeparator(),
            toDisplayParams(properties),
            lineSeparator(),
            toDisplayParams(defaultsAdded)
        )
    );

    return validated(cluster);
  }

  /**
   * Creates a {@code Cluster} object from a parameter-value mapping constructed from user input.
   *
   * @param paramValueMap parameter-value mapping
   * @return a {@code Cluster} object
   */
  public Cluster create(Map<Setting, String> paramValueMap) {
    // safe copy
    paramValueMap = new HashMap<>(paramValueMap);

    Collection<Configuration> defaultsAdded = new TreeSet<>(Comparator.comparing(Configuration::toString));
    Cluster cluster = ConfigurationParser.parseCommandLineParameters(parameterSubstitutor, paramValueMap, defaultsAdded::add);

    LOGGER.info(
        String.format(
            "%sRead the following parameters: %s%sAdded the following defaults: %s",
            lineSeparator(),
            toDisplayParams("--", paramValueMap),
            lineSeparator(),
            toDisplayParams("--", defaultsAdded.stream()
                .filter(configuration -> configuration.getValue() != null)
                .collect(toMap(Configuration::getSetting, Configuration::getValue)))
        )
    );

    return validated(cluster);
  }

  private Cluster validated(Cluster cluster) {
    new ClusterValidator(cluster).validate();
    return cluster;
  }

  private String toDisplayParams(String prefix, Map<Setting, String> supplied) {
    String suppliedParameters = supplied.entrySet()
        .stream()
        .filter(e -> e.getValue() != null)
        .sorted(Comparator.comparing(Map.Entry::getKey))
        .map(entry -> prefix + entry.getKey() + "=" + parameterSubstitutor.substitute(entry.getValue()))
        .collect(Collectors.joining(lineSeparator() + "    ", "    ", ""));
    if (suppliedParameters.trim().isEmpty()) {
      suppliedParameters = "[]";
    } else {
      suppliedParameters = lineSeparator() + suppliedParameters;
    }
    return suppliedParameters;
  }

  private String toDisplayParams(Properties properties) {
    String suppliedParameters = properties.entrySet()
        .stream()
        .filter(e -> e.getValue() != null && !e.getValue().equals(""))
        .sorted(Comparator.comparing(e -> e.getKey().toString()))
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining(lineSeparator() + "    ", "    ", ""));
    if (suppliedParameters.trim().isEmpty()) {
      suppliedParameters = "[]";
    } else {
      suppliedParameters = lineSeparator() + suppliedParameters;
    }
    return suppliedParameters;
  }

  private String toDisplayParams(Collection<Configuration> configurations) {
    String suppliedParameters = configurations.stream()
        .filter(c -> c.getValue() != null)
        .map(Configuration::toString)
        .sorted()
        .collect(Collectors.joining(lineSeparator() + "    ", "    ", ""));
    if (suppliedParameters.trim().isEmpty()) {
      suppliedParameters = "[]";
    } else {
      suppliedParameters = lineSeparator() + suppliedParameters;
    }
    return suppliedParameters;
  }
}
