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

package org.kie.kogito.index.mongodb.protobuf;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.arc.profile.IfBuildProfile;
import org.kie.kogito.index.protobuf.ProtobufCacheService;

@ApplicationScoped
@IfBuildProfile("mongodb")
public class MongoDBProtobufCacheService implements ProtobufCacheService {

    Map<String, String> protobufCache = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> getProtobufCache() {
        return protobufCache;
    }
}