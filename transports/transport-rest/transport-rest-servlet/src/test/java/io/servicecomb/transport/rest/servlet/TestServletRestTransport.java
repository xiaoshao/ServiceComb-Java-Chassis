/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.servlet;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.Endpoint;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.common.net.URIEndpointObject;
import io.servicecomb.serviceregistry.Features;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.api.Const;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.transport.rest.client.RestTransportClient;
import io.servicecomb.transport.rest.client.RestTransportClientManager;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestServletRestTransport {
  ServletRestTransport transport = new ServletRestTransport();

  @Test
  public void testInitNotPublish(@Mocked RestTransportClient restTransportClient) {
    new MockUp<RestTransportClientManager>() {
      @Mock
      public RestTransportClient getRestTransportClient(boolean sslEnabled) {
        return restTransportClient;
      }
    };

    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress();
        result = null;
      }
    };
    Assert.assertTrue(transport.init());
    Assert.assertNull(transport.getPublishEndpoint());
  }

  @Test
  public void testInitPublishNoUrlPrefix(@Mocked RestTransportClient restTransportClient) {
    new MockUp<RestTransportClientManager>() {
      @Mock
      public RestTransportClient getRestTransportClient(boolean sslEnabled) {
        return restTransportClient;
      }
    };

    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress();
        result = "1.1.1.1:1234";
      }
    };
    System.clearProperty(Const.URL_PREFIX);

    Assert.assertTrue(transport.init());
    Assert.assertEquals("rest://1.1.1.1:1234", transport.getPublishEndpoint().getEndpoint());
  }

  @Test
  public void testInitPublishWithUrlPrefix(@Mocked RestTransportClient restTransportClient,
      @Mocked ServiceRegistry serviceRegistry) {
    Features features = new Features();
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getServiceRegistry();
        result = serviceRegistry;
        serviceRegistry.getFeatures();
        result = features;
      }
    };

    new MockUp<RestTransportClientManager>() {
      @Mock
      public RestTransportClient getRestTransportClient(boolean sslEnabled) {
        return restTransportClient;
      }
    };

    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress();
        result = "1.1.1.1:1234";
      }
    };
    System.setProperty(Const.URL_PREFIX, "/root");

    Assert.assertTrue(transport.init());
    Assert.assertEquals("rest://1.1.1.1:1234?urlPrefix=/root", transport.getPublishEndpoint().getEndpoint());

    System.clearProperty(Const.URL_PREFIX);
  }

  @Test
  public void testSendException() {
    boolean status = true;
    Invocation invocation = Mockito.mock(Invocation.class);
    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
    URIEndpointObject endpoint = Mockito.mock(URIEndpointObject.class);
    Endpoint endpoint1 = Mockito.mock(Endpoint.class);
    Mockito.when(invocation.getEndpoint()).thenReturn(endpoint1);
    Mockito.when(invocation.getEndpoint().getAddress()).thenReturn(endpoint);
    try {
      transport.send(invocation, asyncResp);
    } catch (Exception exce) {
      Assert.assertNotNull(exce);
      status = false;
    }
    Assert.assertFalse(status);
  }

  @Test
  public void testGetOrder() {
    ServletRestTransport transport = new ServletRestTransport();
    Assert.assertEquals(0, transport.getOrder());
  }

  @Test
  public void testCanInitNullAddress() throws IOException {
    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress();
        result = null;
      }
    };

    ServletRestTransport transport = new ServletRestTransport();
    Assert.assertTrue(transport.canInit());
  }

  @Test
  public void testCanInitListened() throws IOException {
    ServerSocket ss = new ServerSocket(0);
    int port = ss.getLocalPort();

    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress();
        result = "0.0.0.0:" + port;
      }
    };

    ServletRestTransport transport = new ServletRestTransport();
    Assert.assertTrue(transport.canInit());

    ss.close();
  }

  @Test
  public void testCanInitNotListened() throws IOException {
    ServerSocket ss = new ServerSocket(0);
    int port = ss.getLocalPort();
    ss.close();

    new Expectations(ServletConfig.class) {
      {
        ServletConfig.getLocalServerAddress();
        result = "0.0.0.0:" + port;
      }
    };

    ServletRestTransport transport = new ServletRestTransport();
    Assert.assertFalse(transport.canInit());
  }
}
