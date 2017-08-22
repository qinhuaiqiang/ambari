/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari.server.agent.stomp;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.events.AgentConfigsUpdateEvent;
import org.apache.ambari.server.state.Clusters;
import org.apache.ambari.server.state.ConfigHelper;
import org.apache.ambari.server.state.Host;
import org.apache.commons.collections.CollectionUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class AgentConfigsHolder extends AgentHostDataHolder<AgentConfigsUpdateEvent> {

  @Inject
  private ConfigHelper configHelper;

  @Inject
  private Provider<Clusters> clusters;

  @Override
  public AgentConfigsUpdateEvent getCurrentData(String hostName) throws AmbariException {
    return configHelper.getHostActualConfigs(hostName);
  }

  protected boolean handleUpdate(AgentConfigsUpdateEvent update) throws AmbariException {
    setData(update, update.getHostName());
    return true;
  }

  public void updateData(Long clusterId, List<String> hostNames) throws AmbariException {
    if (CollectionUtils.isEmpty(hostNames)) {
      // TODO cluster configs will be created before hosts assigning
      if (CollectionUtils.isEmpty(clusters.get().getCluster(clusterId).getHosts())) {
        hostNames = clusters.get().getHosts().stream().map(Host::getHostName).collect(Collectors.toList());
      } else {
        hostNames = clusters.get().getCluster(clusterId).getHosts().stream().map(Host::getHostName).collect(Collectors.toList());
      }
    }

    for (String hostName : hostNames) {
      AgentConfigsUpdateEvent agentConfigsUpdateEvent = configHelper.getHostActualConfigs(hostName);
      agentConfigsUpdateEvent.setHostName(hostName);
      updateData(agentConfigsUpdateEvent);
    }
  }

  @Override
  protected AgentConfigsUpdateEvent getEmptyData() {
    return AgentConfigsUpdateEvent.emptyUpdate();
  }
}