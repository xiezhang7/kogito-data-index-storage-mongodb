/*
 *
 *  * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.kie.kogito.index.mongodb.query;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import javax.inject.Inject;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.index.cache.Cache;
import org.kie.kogito.index.cache.CacheService;
import org.kie.kogito.index.model.UserTaskInstance;
import org.kie.kogito.index.mongodb.MongoDBServerTestResource;
import org.kie.kogito.index.mongodb.TestUtils;
import org.kie.kogito.index.query.SortDirection;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.kie.kogito.index.mongodb.query.QueryTestBase.assertWithId;
import static org.kie.kogito.index.mongodb.query.QueryTestBase.assertWithIdInOrder;
import static org.kie.kogito.index.mongodb.query.QueryTestBase.queryAndAssert;
import static org.kie.kogito.index.query.QueryFilterFactory.and;
import static org.kie.kogito.index.query.QueryFilterFactory.between;
import static org.kie.kogito.index.query.QueryFilterFactory.contains;
import static org.kie.kogito.index.query.QueryFilterFactory.containsAll;
import static org.kie.kogito.index.query.QueryFilterFactory.containsAny;
import static org.kie.kogito.index.query.QueryFilterFactory.equalTo;
import static org.kie.kogito.index.query.QueryFilterFactory.greaterThan;
import static org.kie.kogito.index.query.QueryFilterFactory.greaterThanEqual;
import static org.kie.kogito.index.query.QueryFilterFactory.in;
import static org.kie.kogito.index.query.QueryFilterFactory.isNull;
import static org.kie.kogito.index.query.QueryFilterFactory.lessThan;
import static org.kie.kogito.index.query.QueryFilterFactory.lessThanEqual;
import static org.kie.kogito.index.query.QueryFilterFactory.like;
import static org.kie.kogito.index.query.QueryFilterFactory.notNull;
import static org.kie.kogito.index.query.QueryFilterFactory.or;
import static org.kie.kogito.index.query.QueryFilterFactory.orderBy;

@QuarkusTest
@QuarkusTestResource(MongoDBServerTestResource.class)
public class UserTaskInstanceQueryTest {

    @Inject
    CacheService cacheService;

    Cache<String, UserTaskInstance> cache;

    @BeforeEach
    void setUp() {
        this.cache = cacheService.getUserTaskInstancesCache();
    }

    @AfterEach
    void tearDown() {
        cache.clear();
        Assert.assertTrue(cache.isEmpty());
    }

    @Test
    void test() {
        String taskId1 = UUID.randomUUID().toString() + "_task1";
        String processInstanceId1 = UUID.randomUUID().toString();
        String taskId2 = UUID.randomUUID().toString();
        String processInstanceId2 = UUID.randomUUID().toString();

        UserTaskInstance userTaskInstance1 = TestUtils.createUserTaskInstance(taskId1, processInstanceId1, RandomStringUtils.randomAlphabetic(5), UUID.randomUUID().toString(), RandomStringUtils.randomAlphabetic(10), "InProgress");
        QueryTestBase.testSleep();
        UserTaskInstance userTaskInstance2 = TestUtils.createUserTaskInstance(taskId2, processInstanceId2, RandomStringUtils.randomAlphabetic(5), null, null, "Completed");
        cache.put(taskId1, userTaskInstance1);
        cache.put(taskId2, userTaskInstance2);

        queryAndAssert(assertWithId(), cache, singletonList(in("state", asList("InProgress", "Completed"))), null, null, null, taskId1, taskId2);
        queryAndAssert(assertWithId(), cache, singletonList(equalTo("state", "InProgress")), null, null, null, taskId1);
        queryAndAssert(assertWithId(), cache, singletonList(greaterThan("started", Instant.now().toEpochMilli())), null, null, null);
        queryAndAssert(assertWithId(), cache, singletonList(greaterThanEqual("completed", Instant.now().toEpochMilli())), null, null, null, taskId1, taskId2);
        queryAndAssert(assertWithId(), cache, singletonList(lessThan("completed", Instant.now().toEpochMilli())), null, null, null);
        queryAndAssert(assertWithId(), cache, singletonList(lessThanEqual("started", Instant.now().toEpochMilli())), null, null, null, taskId1, taskId2);
        queryAndAssert(assertWithId(), cache, singletonList(between("completed", Instant.now().toEpochMilli(), Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli())), null, null, null, taskId1, taskId2);
        queryAndAssert(assertWithId(), cache, singletonList(isNull("rootProcessInstanceId")), null, null, null, taskId2);
        queryAndAssert(assertWithId(), cache, singletonList(notNull("rootProcessInstanceId")), null, null, null, taskId1);
        queryAndAssert(assertWithId(), cache, singletonList(contains("id", taskId1)), null, null, null, taskId1);
        queryAndAssert(assertWithId(), cache, singletonList(containsAny("processInstanceId", asList(processInstanceId1, processInstanceId2))), null, null, null, taskId1, taskId2);
        queryAndAssert(assertWithId(), cache, singletonList(containsAll("processInstanceId", asList(processInstanceId1, processInstanceId2))), null, null, null);
        queryAndAssert(assertWithId(), cache, singletonList(like("id", "*_task1")), null, null, null, taskId1);
        queryAndAssert(assertWithId(), cache, singletonList(and(asList(equalTo("id", taskId1), equalTo("processInstanceId", processInstanceId1)))), null, null, null, taskId1);
        queryAndAssert(assertWithId(), cache, singletonList(or(asList(equalTo("id", taskId1), equalTo("id", taskId2)))), null, null, null, taskId1, taskId2);
        queryAndAssert(assertWithId(), cache, asList(equalTo("id", taskId1), equalTo("processInstanceId", processInstanceId2)), null, null, null);

        queryAndAssert(assertWithIdInOrder(), cache, asList(in("id", asList(taskId1, taskId2)), in("processInstanceId", asList(processInstanceId1, processInstanceId2))), singletonList(orderBy("state", SortDirection.ASC)), 1, 1, taskId1);
        queryAndAssert(assertWithIdInOrder(), cache, null, singletonList(orderBy("state", SortDirection.DESC)), null, null, taskId1, taskId2);
        queryAndAssert(assertWithIdInOrder(), cache, null, null, 1, 1, taskId2);
        queryAndAssert(assertWithIdInOrder(), cache, null, asList(orderBy("state", SortDirection.ASC), orderBy("completed", SortDirection.ASC)), 1, 1, taskId1);
    }
}
