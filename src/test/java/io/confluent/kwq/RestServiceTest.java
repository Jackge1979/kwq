/**
 * Copyright 2018 Confluent Inc.
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
 **/
package io.confluent.kwq;

import io.confluent.kwq.utils.IntegrationTestHarness;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class RestServiceTest {


  private IntegrationTestHarness testHarness;

  @Before
  public void before() throws Exception {
    testHarness = new IntegrationTestHarness();
    testHarness.start();

    System.setProperty("bootstrap.servers", testHarness.embeddedKafkaCluster.bootstrapServers());

    Thread.sleep( 500);

    RestServerMain.initialize();
    RestServerMain.start();


  }

  @After
  public void after() {
    RestServerMain.stop();
    RestServerMain.destroy();
    testHarness.stop();
  }


  @Test
  public void runServerForAbit() throws Exception {
    Thread.sleep(600 * 1000);
  }

  @Test
  public void testGetTask() throws Exception {

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://localhost:8080").path("api/kwq");
    Task response = target.request(MediaType.APPLICATION_JSON_TYPE).get(Task.class);

    Assert.assertNotNull("Should have created KSWQ instance", response);
  }


  @Test
  public void testSubmitTask() throws Exception {

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://localhost:8080").path("api/kwq");


    Task task = TaskDataProvider.data.values().iterator().next();

    Task result = target.request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(task, MediaType.APPLICATION_JSON_TYPE),
                    Task.class);

    Assert.assertEquals("Should have created KSWQ instance", task, result);
  }
}
