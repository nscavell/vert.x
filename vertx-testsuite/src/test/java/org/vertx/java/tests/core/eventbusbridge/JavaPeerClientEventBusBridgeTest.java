/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.vertx.java.tests.core.eventbusbridge;

import org.junit.Test;
import org.vertx.java.testframework.TestBase;
import vertx.tests.core.eventbusbridge.BridgeClient;
import vertx.tests.core.eventbusbridge.BridgePeer;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class JavaPeerClientEventBusBridgeTest extends TestBase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    startApp(getPeerClassName());
    startApp(getClientClassName());
  }

  protected String getPeerClassName() {
    return BridgePeer.class.getName();
  }

  protected String getClientClassName() {
    return BridgeClient.class.getName();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  private void runPeerTest(String testName) {
    startTest(testName + "Initialize");
    startTest(testName);
  }
  @Test
  public void testSendJson() {
    runPeerTest(getMethodName());
  }

  @Test
  public void testSendJsonArray() {
    runPeerTest(getMethodName());
  }

  @Test
  public void testSendString() {
    runPeerTest(getMethodName());
  }

  @Test
  public void testSendBoolean() {
    runPeerTest(getMethodName());
  }

  @Test
  public void testSendInteger() {
    runPeerTest(getMethodName());
  }

  public void testSendLong() {
    runPeerTest(getMethodName());
  }

  public void testSendDouble() {
    runPeerTest(getMethodName());
  }

  public void testSendFloat() {
    runPeerTest(getMethodName());
  }

  public void testSendShort() {
    runPeerTest(getMethodName());
  }
}
