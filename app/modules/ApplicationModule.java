package modules;

import com.google.inject.AbstractModule;
import persistence.db.ArangoDB;
import utils.StartInterceptor;

/**
 * Created by leonard on 09.03.16.
 */
public class ApplicationModule extends AbstractModule {


  @Override protected void configure() {
    bind(StartInterceptor.class).asEagerSingleton();
    bind(ArangoDB.class).asEagerSingleton();
  }
}
