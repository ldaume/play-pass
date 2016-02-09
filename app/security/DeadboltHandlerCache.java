package security;

import be.objectify.deadbolt.java.ConfigKeys;
import be.objectify.deadbolt.java.cache.HandlerCache;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

public class DeadboltHandlerCache implements HandlerCache {
  @Inject private DeadboltHandler defaultHandler;

  @Override public be.objectify.deadbolt.java.DeadboltHandler apply(final String key) {
    return ImmutableMap.of(ConfigKeys.DEFAULT_HANDLER_KEY, defaultHandler).get(key);
  }

  @Override public be.objectify.deadbolt.java.DeadboltHandler get() {
    return defaultHandler;
  }
}
