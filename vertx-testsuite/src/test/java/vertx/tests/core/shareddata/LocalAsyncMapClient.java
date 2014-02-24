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
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class LocalAsyncMapClient extends SharedDataAppBase {

  @Override
  protected boolean isLocal() {
    return true;
  }

  public void testSharedMap() {
    ConcurrentAsyncMap<String, String> map = sharedData.getAsyncMap("foo");
    assertThat(map).get("bar").eq("baz", true);
  }

  public void testSharedMap_NoKey() {
    ConcurrentAsyncMap<String, String> map = sharedData.getAsyncMap("foo");
    assertThat(map).get("asdf").eq(null, true);
  }

  public void testSharedMap_NewMap() {
    ConcurrentAsyncMap<String, String> map = sharedData.getAsyncMap("empty");
    assertThat(map).get("bar").eq(null, true);
  }

  public void testSharedMap_Immutability() {
    final ConcurrentAsyncMap<String, Buffer> map = sharedData.getAsyncMap("immutability");
    get(map, "foo", new Handler<Buffer>() {
      @Override
      public void handle(Buffer buffer) {
        // Modify the buffer
        buffer.appendString("boom");
        assertThat(map).get("foo").eq(new Buffer("12345"), true);
      }
    });
  }

  public void testSharedMap_ReplaceValue() {
    final ConcurrentAsyncMap<String, Integer> map = sharedData.getAsyncMap("replace");
    get(map, "abc", new Handler<Integer>() {
      @Override
      public void handle(Integer value) {
        tu.azzert(value.equals(123));
        put(map, "abc", 456, new Handler<Integer>() {
          @Override
          public void handle(Integer prev) {
            tu.azzert(prev.equals(123));
            assertThat(map).get("abc").eq(456, true);
          }
        });
      }
    });
  }
}
