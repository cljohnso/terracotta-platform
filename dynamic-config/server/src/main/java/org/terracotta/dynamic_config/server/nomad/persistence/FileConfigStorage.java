/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.dynamic_config.server.nomad.persistence;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.dynamic_config.api.model.NodeContext;
import org.terracotta.dynamic_config.api.service.ConfigRepositoryMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class FileConfigStorage implements ConfigStorage<NodeContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileConfigStorage.class);

  private final Path root;
  private final String nodeName;
  private final ConfigRepositoryMapper configRepositoryMapper;

  public FileConfigStorage(Path root, String nodeName, ConfigRepositoryMapper configRepositoryMapper) {
    this.root = requireNonNull(root);
    this.nodeName = requireNonNull(nodeName);
    this.configRepositoryMapper = requireNonNull(configRepositoryMapper);
  }

  @Override
  public NodeContext getConfig(long version) throws ConfigStorageException {
    Path file = toPath(version);
    LOGGER.debug("Loading version: {} from file: {}", version, file);

    try {
      return configRepositoryMapper.fromXml(nodeName, new String(Files.readAllBytes(file), UTF_8));
    } catch (IOException e) {
      throw new ConfigStorageException(e);
    }
  }

  @Override
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public void saveConfig(long version, NodeContext config) throws ConfigStorageException {
    Path file = toPath(version);
    LOGGER.debug("Saving topology: {} with version: {} to file: {}", config, version, file);
    try {
      if (file.getParent() != null) {
        Files.createDirectories(file.getParent());
      }
      Files.write(file, configRepositoryMapper.toXml(config).getBytes(UTF_8));
    } catch (IOException e) {
      throw new ConfigStorageException(e);
    }
  }

  @Override
  public void reset() throws ConfigStorageException {
    String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss"));
    AtomicReference<ConfigStorageException> error = new AtomicReference<>();
    try (Stream<Path> stream = Files.list(root)) {
      stream.filter(Files::isRegularFile).forEach(config -> {
        String filename = config.getFileName().toString();
        ClusterConfigFilename.from(filename).ifPresent(ccf -> {
          Path backup = config.resolveSibling("backup-" + filename + "-" + time);
          try {
            Files.move(config, backup);
          } catch (IOException ioe) {
            if (error.get() == null) {
              error.set(new ConfigStorageException(ioe));
            } else {
              error.get().addSuppressed(ioe);
            }
          }
        });
      });
    } catch (IOException e) {
      throw new ConfigStorageException(e);
    }
    if (error.get() != null) {
      throw error.get();
    }
  }

  private Path toPath(long version) {
    String filename = ClusterConfigFilename.with(nodeName, version).getFilename();
    return root.resolve(filename);
  }
}