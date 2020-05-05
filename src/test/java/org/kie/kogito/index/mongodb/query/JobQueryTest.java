package org.kie.kogito.index.mongodb.query;

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
import org.kie.kogito.index.model.Job;
import org.kie.kogito.index.mongodb.MongoDBServerTestResource;
import org.kie.kogito.index.mongodb.TestUtils;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.kie.kogito.index.mongodb.query.QueryTestBase.testQuery;
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

@QuarkusTest
@QuarkusTestResource(MongoDBServerTestResource.class)
public class JobQueryTest {

    @Inject
    CacheService cacheService;

    Cache<String, Job> cache;

    @BeforeEach
    void setUp() {
        this.cache = cacheService.getJobsCache();
    }

    @AfterEach
    void tearDown() {
        cache.clear();
        Assert.assertTrue(cache.isEmpty());
    }

    @Test
    void test() {
        String jobId1 = UUID.randomUUID().toString() + "_job1";
        String processInstanceId1 = UUID.randomUUID().toString();
        String jobId2 = UUID.randomUUID().toString();
        String processInstanceId2 = UUID.randomUUID().toString();

        Job job1 = TestUtils.createJob(jobId1, processInstanceId1, RandomStringUtils.randomAlphabetic(5), UUID.randomUUID().toString(), RandomStringUtils.randomAlphabetic(10), "EXPECTED");
        QueryTestBase.testSleep();
        Job job2 = TestUtils.createJob(jobId2, processInstanceId2, RandomStringUtils.randomAlphabetic(5), null, null, "SCHEDULED");
        cache.put(jobId1, job1);
        cache.put(jobId2, job2);

        testQuery(cache, singletonList(in("status", asList("EXPECTED", "SCHEDULED"))), null, null, null, jobId1, jobId2);
        testQuery(cache, singletonList(equalTo("status", "EXPECTED")), null, null, null, jobId1);
        testQuery(cache, singletonList(greaterThan("priority", 1)), null, null, null);
        testQuery(cache, singletonList(greaterThanEqual("priority", 1)), null, null, null, jobId1, jobId2);
        testQuery(cache, singletonList(lessThan("priority", 1)), null, null, null);
        testQuery(cache, singletonList(lessThanEqual("priority", 1)), null, null, null, jobId1, jobId2);
        testQuery(cache, singletonList(between("priority", 0, 3)), null, null, null, jobId1, jobId2);
        testQuery(cache, singletonList(isNull("rootProcessInstanceId")), null, null, null, jobId2);
        testQuery(cache, singletonList(notNull("rootProcessInstanceId")), null, null, null, jobId1);
        testQuery(cache, singletonList(contains("id", jobId1)), null, null, null, jobId1);
        testQuery(cache, singletonList(containsAny("processInstanceId", asList(processInstanceId1, processInstanceId2))), null, null, null, jobId1, jobId2);
        testQuery(cache, singletonList(containsAll("processInstanceId", asList(processInstanceId1, processInstanceId2))), null, null, null);
        testQuery(cache, singletonList(like("id", "*_job1")), null, null, null, jobId1);
        testQuery(cache, singletonList(and(asList(lessThan("retries", 11), greaterThanEqual("retries", 10)))), null, null, null, jobId1, jobId2);
        testQuery(cache, singletonList(or(asList(equalTo("id", jobId1), equalTo("id", jobId2)))), null, null, null, jobId1, jobId2);
        testQuery(cache, asList(equalTo("id", jobId1), equalTo("processInstanceId", processInstanceId2)), null, null, null);

//        testQuery(cache, asList(in("id", asList(jobId1, jobId2)), in("processInstanceId", asList(processInstanceId1, processInstanceId2))), singletonList(orderBy("status", SortDirection.ASC)), 1, 1, jobId2);
//        testQuery(cache, null, singletonList(orderBy("status", SortDirection.DESC)), null, null, jobId2, jobId1);
        testQuery(cache, null, null, 1, 1, jobId2);
//        testQuery(cache, null, singletonList(orderBy("status", SortDirection.ASC)), 1, 1, jobId2);
    }
}