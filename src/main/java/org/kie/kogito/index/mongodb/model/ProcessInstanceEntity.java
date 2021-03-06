/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.index.mongodb.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonId;
import org.kie.kogito.index.model.NodeInstance;
import org.kie.kogito.index.model.ProcessInstance;
import org.kie.kogito.index.model.ProcessInstanceError;

import static org.kie.kogito.index.mongodb.utils.ModelUtils.documentToJsonNode;
import static org.kie.kogito.index.mongodb.utils.ModelUtils.instantToZonedDateTime;
import static org.kie.kogito.index.mongodb.utils.ModelUtils.jsonNodeToDocument;
import static org.kie.kogito.index.mongodb.utils.ModelUtils.zonedDateTimeToInstant;

@MongoEntity(collection = "processinstances")
public class ProcessInstanceEntity extends PanacheMongoEntityBase {

    @BsonId
    public String id;

    public String processId;

    public Set<String> roles;

    public Document variables;

    public String endpoint;

    public List<NodeInstanceEntity> nodes;

    public Integer state;

    public Long start;

    public Long end;

    public String rootProcessInstanceId;

    public String rootProcessId;

    public String parentProcessInstanceId;

    public String processName;

    public ProcessInstanceErrorEntity error;

    public Set<String> addons;

    public Long lastUpdate;

    public String businessKey;

    public static ProcessInstance toProcessInstance(ProcessInstanceEntity entity) {
        if (entity == null) {
            return null;
        }

        ProcessInstance instance = new ProcessInstance();
        instance.setId(entity.id);
        instance.setProcessId(entity.processId);
        instance.setRoles(entity.roles);
        instance.setVariables(documentToJsonNode(entity.variables, JsonNode.class));
        instance.setEndpoint(entity.endpoint);
        instance.setNodes(Optional.ofNullable(entity.nodes).map(nodes -> nodes.stream().map(NodeInstanceEntity::toNodeInstance).collect(Collectors.toList())).orElse(null));
        instance.setState(entity.state);
        instance.setStart(instantToZonedDateTime(entity.start));
        instance.setEnd(instantToZonedDateTime(entity.end));
        instance.setRootProcessId(entity.rootProcessId);
        instance.setRootProcessInstanceId(entity.rootProcessInstanceId);
        instance.setParentProcessInstanceId(entity.parentProcessInstanceId);
        instance.setProcessName(entity.processName);
        instance.setError(Optional.ofNullable(entity.error).map(ProcessInstanceErrorEntity::toProcessInstanceError).orElse(null));
        instance.setAddons(entity.addons);
        instance.setLastUpdate(instantToZonedDateTime(entity.lastUpdate));
        instance.setBusinessKey(entity.businessKey);
        return instance;
    }

    public static ProcessInstanceEntity fromProcessInstance(ProcessInstance instance) {
        if (instance == null) {
            return null;
        }

        ProcessInstanceEntity entity = new ProcessInstanceEntity();
        entity.id = instance.getId();
        entity.processId = instance.getProcessId();
        entity.roles = instance.getRoles();
        entity.variables = jsonNodeToDocument(instance.getVariables());
        entity.endpoint = instance.getEndpoint();
        entity.nodes = Optional.ofNullable(instance.getNodes()).map(nodes -> nodes.stream().map(NodeInstanceEntity::fromNodeInstance).collect(Collectors.toList())).orElse(null);
        entity.state = instance.getState();
        entity.start = zonedDateTimeToInstant(instance.getStart());
        entity.end = zonedDateTimeToInstant(instance.getEnd());
        entity.rootProcessId = instance.getRootProcessId();
        entity.rootProcessInstanceId = instance.getRootProcessInstanceId();
        entity.parentProcessInstanceId = instance.getParentProcessInstanceId();
        entity.processName = instance.getProcessName();
        entity.error = Optional.ofNullable(instance.getError()).map(ProcessInstanceErrorEntity::fromProcessInstanceError).orElse(null);
        entity.addons = instance.getAddons();
        entity.lastUpdate = zonedDateTimeToInstant(instance.getLastUpdate());
        entity.businessKey = instance.getBusinessKey();
        return entity;
    }

    public static class NodeInstanceEntity extends PanacheMongoEntity {

        public String _id;

        public String id;

        public String name;

        public String nodeId;

        public String type;

        public Long enter;

        public Long exit;

        public String definitionId;

        static NodeInstance toNodeInstance(NodeInstanceEntity entity) {
            if (entity == null) {
                return null;
            }

            NodeInstance instance = new NodeInstance();
            instance.setId(entity.id);
            instance.setName(entity.name);
            instance.setNodeId(entity.nodeId);
            instance.setType(entity.type);
            instance.setEnter(instantToZonedDateTime(entity.enter));
            instance.setExit(instantToZonedDateTime(entity.exit));
            instance.setDefinitionId(entity.definitionId);
            return instance;
        }

        static NodeInstanceEntity fromNodeInstance(NodeInstance instance) {
            if (instance == null) {
                return null;
            }

            NodeInstanceEntity entity = new NodeInstanceEntity();
            entity.id = instance.getId();
            entity.name = instance.getName();
            entity.nodeId = instance.getNodeId();
            entity.type = instance.getType();
            entity.enter = zonedDateTimeToInstant(instance.getEnter());
            entity.exit = zonedDateTimeToInstant(instance.getExit());
            entity.definitionId = instance.getDefinitionId();
            return entity;
        }
    }

    public static class ProcessInstanceErrorEntity extends PanacheMongoEntity {

        public String _id;

        public String nodeDefinitionId;

        public String message;

        static ProcessInstanceError toProcessInstanceError(ProcessInstanceErrorEntity entity) {
            if (entity == null) {
                return null;
            }

            ProcessInstanceError error = new ProcessInstanceError();
            error.setNodeDefinitionId(entity.nodeDefinitionId);
            error.setMessage(entity.message);
            return error;
        }

        static ProcessInstanceErrorEntity fromProcessInstanceError(ProcessInstanceError error) {
            if (error == null) {
                return null;
            }

            ProcessInstanceErrorEntity entity = new ProcessInstanceErrorEntity();
            entity.nodeDefinitionId = error.getNodeDefinitionId();
            entity.message = error.getMessage();
            return entity;
        }
    }
}
