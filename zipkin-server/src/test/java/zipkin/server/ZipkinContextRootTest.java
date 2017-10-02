/**
 * Copyright 2015-2017 The OpenZipkin Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package zipkin.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import zipkin.internal.v2.storage.InMemoryStorage;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
  classes = ZipkinServer.class,
  webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {"zipkin.store.type=mem", "spring.config.name=zipkin-server", "server.port=6060",
  "server.contextPath=/foo/bar/"})
public class ZipkinContextRootTest {

  @Autowired
  ConfigurableWebApplicationContext context;
  @Autowired
  InMemoryStorage storage;
  @Autowired
  ActuateCollectorMetrics metrics;
  @LocalServerPort
  int zipkinPort;

  MockMvc mockMvc;

  //  @Component
  //  public class AppContainerCustomizer implements EmbeddedServletContainerCustomizer {
  //
  //    @Override
  //    public void customize(ConfigurableEmbeddedServletContainer container) {
  //
  //      container.setPort(8080);
  //      container.setContextPath("/home");
  //
  //    }
  //  }

  @Before
  public void init() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .build();
    storage.clear();
    metrics.forTransport("http").reset();
  }

  @Test
  public void writeSpans_noContentTypeIsJson() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get("/foo/bar/zipkin/")).andReturn();

    String string = mvcResult.getResponse()
                             .getContentAsString();

    System.out.println("OUTPUT: " + string);

    Thread.sleep(100000);
  }

  ResultActions performAsync(MockHttpServletRequestBuilder request) throws Exception {
    return mockMvc.perform(asyncDispatch(mockMvc.perform(request).andReturn()));
  }
}
