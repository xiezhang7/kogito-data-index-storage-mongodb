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

import javax.inject.Inject;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.index.cache.Cache;
import org.kie.kogito.index.cache.CacheService;
import org.kie.kogito.index.mongodb.MongoDBServerTestResource;
import org.kie.kogito.index.query.SortDirection;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.kie.kogito.index.mongodb.query.QueryTestBase.assertWithString;
import static org.kie.kogito.index.mongodb.query.QueryTestBase.assertWithStringInOrder;
import static org.kie.kogito.index.mongodb.query.QueryTestBase.queryAndAssert;
import static org.kie.kogito.index.query.QueryFilterFactory.and;
import static org.kie.kogito.index.query.QueryFilterFactory.contains;
import static org.kie.kogito.index.query.QueryFilterFactory.containsAll;
import static org.kie.kogito.index.query.QueryFilterFactory.containsAny;
import static org.kie.kogito.index.query.QueryFilterFactory.equalTo;
import static org.kie.kogito.index.query.QueryFilterFactory.in;
import static org.kie.kogito.index.query.QueryFilterFactory.like;
import static org.kie.kogito.index.query.QueryFilterFactory.notNull;
import static org.kie.kogito.index.query.QueryFilterFactory.or;
import static org.kie.kogito.index.query.QueryFilterFactory.orderBy;

@QuarkusTest
@QuarkusTestResource(MongoDBServerTestResource.class)
public class ProcessIdQueryTest {

    @Inject
    CacheService cacheService;

    Cache<String, String> cache;

    @BeforeEach
    void setUp() {
        this.cache = cacheService.getProcessIdModelCache();
    }

    @AfterEach
    void tearDown() {
        cache.clear();
        Assert.assertTrue(cache.isEmpty());
    }

    @Test
    void test() {
        String processId = "travels";
        String subProcessId = "travels_sub";
        String type1 = "org.acme.travels.travels";
        String type2 = "org.acme.travels";
        cache.put(processId, type1);
        cache.put(subProcessId, type2);

        queryAndAssert(assertWithString(), cache, singletonList(in("processId", asList(processId, subProcessId))), null, null, null, type1, type2);
        queryAndAssert(assertWithString(), cache, singletonList(equalTo("processId", processId)), null, null, null, type1);
        queryAndAssert(assertWithString(), cache, singletonList(notNull("processId")), null, null, null, type1, type2);
        queryAndAssert(assertWithString(), cache, singletonList(contains("fullTypeName", type1)), null, null, null, type1);
        queryAndAssert(assertWithString(), cache, singletonList(containsAny("fullTypeName", asList(type1, type2))), null, null, null, type1, type2);
        queryAndAssert(assertWithString(), cache, singletonList(containsAll("processId", asList(processId, subProcessId))), null, null, null);
        queryAndAssert(assertWithString(), cache, singletonList(like("processId", "*_sub")), null, null, null, type2);
        queryAndAssert(assertWithString(), cache, singletonList(and(asList(equalTo("processId", processId), equalTo("fullTypeName", type1)))), null, null, null, type1);
        queryAndAssert(assertWithString(), cache, singletonList(or(asList(equalTo("processId", processId), equalTo("fullTypeName", type2)))), null, null, null, type1, type2);
        queryAndAssert(assertWithString(), cache, asList(equalTo("processId", processId), equalTo("fullTypeName", type2)), null, null, null);

        queryAndAssert(assertWithStringInOrder(), cache, singletonList(in("processId", asList(processId, subProcessId))), singletonList(orderBy("fullTypeName", SortDirection.DESC)), 1, 1, type2);
        queryAndAssert(assertWithStringInOrder(), cache, null, singletonList(orderBy("fullTypeName", SortDirection.DESC)), 1, 1, type2);
        queryAndAssert(assertWithStringInOrder(), cache, null, asList(orderBy("fullTypeName", SortDirection.DESC), orderBy("processId", SortDirection.DESC)), null, null, type1, type2);
        queryAndAssert(assertWithStringInOrder(), cache, null, null, 1, 1, type2);
    }
}
