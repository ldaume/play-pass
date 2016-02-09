package modules;

import be.objectify.deadbolt.java.cache.HandlerCache;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;
import security.DeadboltHandler;
import security.DeadboltHandlerCache;

import javax.inject.Singleton;

/**
 * Creates a binding for deadbolt2.
 */
public class DeadboltModule extends Module {
  @Override public Seq<Binding<?>> bindings(final Environment environment, final Configuration configuration) {
    return seq(bind(DeadboltHandler.class).toSelf(),
               bind(HandlerCache.class).to(DeadboltHandlerCache.class).in(Singleton.class));
  }
}
