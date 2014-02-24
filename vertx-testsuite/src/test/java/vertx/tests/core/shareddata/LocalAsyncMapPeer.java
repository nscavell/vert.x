/*
 * Copyright (c) 2011-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package vertx.tests.core.shareddata;

import org.vertx.java.core.ConcurrentAsyncMap;
import org.vertx.java.core.buffer.Buffer;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class LocalAsyncMapPeer extends SharedDataAppBase {

  @Override
  protected boolean isLocal() {
    return true;
  }

  public void testSharedMapInit() {
    ConcurrentAsyncMap<String, String> map = sharedData.getAsyncMap("foo");
    assertThat(map).put("bar", "baz").completed(true);
  }

  public void testSharedMap_NoKeyInit() {
    // Nothing to prepare
    tu.testComplete();
  }

  public void testSharedMap_NewMapInit() {
    // Nothing to prepare
    tu.testComplete();
  }

  public void testSharedMap_ImmutabilityInit() {
    ConcurrentAsyncMap<String, Buffer> map = sharedData.getAsyncMap("immutability");
    assertThat(map).put("foo", new Buffer("12345")).completed(true);
  }

  public void testSharedMap_ReplaceValueInit() {
    ConcurrentAsyncMap<String, Integer> map = sharedData.getAsyncMap("replace");
    assertThat(map).put("abc", 123).completed(true);
  }
}
