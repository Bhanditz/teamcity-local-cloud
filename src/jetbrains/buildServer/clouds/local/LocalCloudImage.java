/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.clouds.local;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.clouds.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LocalCloudImage implements CloudImage {
  @NotNull private final String myId;
  @NotNull private final String myName;
  @NotNull private final File myAgentHomeDir;
  @NotNull private final Map<String, LocalCloudInstance> myInstances = new HashMap<String, LocalCloudInstance>();
  @NotNull private final IdGenerator myInstanceIdGenerator = new IdGenerator();
  @Nullable private final CloudErrorInfo myErrorInfo;

  LocalCloudImage(@NotNull final String imageId,
                  @NotNull final String imageName,
                  @NotNull final String agentHomePath) {
    myId = imageId;
    myName = imageName;
    myAgentHomeDir = new File(agentHomePath);
    myErrorInfo = myAgentHomeDir.isDirectory() ? null : new CloudErrorInfo("\"" + agentHomePath + "\" is not a directory or does not exist.");
  }

  @NotNull
  public String getId() {
    return myId;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public String getDescription() {
    return getAgentHomeDir().getAbsolutePath();
  }

  @NotNull
  public File getAgentHomeDir() {
    return myAgentHomeDir;
  }

  @NotNull
  public Collection<? extends CloudInstance> getInstances() {
    return Collections.unmodifiableCollection(myInstances.values());
  }

  @Nullable
  public LocalCloudInstance findInstanceById(@NotNull final String instanceId) {
    return myInstances.get(instanceId);
  }

  @Nullable
  public CloudErrorInfo getErrorInfo() {
    return myErrorInfo;
  }

  @NotNull
  LocalCloudInstance startNewInstance(@NotNull final CloudInstanceUserData data) {
    final String instanceId = myInstanceIdGenerator.next();
    final LocalCloudInstance instance = new LocalCloudInstance(instanceId, this, data);
    myInstances.put(instanceId, instance);
    instance.start();
    return instance;
  }

  void forgetInstance(@NotNull final LocalCloudInstance instance) {
    myInstances.remove(instance.getInstanceId());
  }

  void dispose() {
    for (final LocalCloudInstance instance : myInstances.values()) {
      instance.terminate();
    }
    myInstances.clear();
  }
}
