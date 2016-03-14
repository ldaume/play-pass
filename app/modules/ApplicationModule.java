package modules;

import com.google.inject.AbstractModule;
import persistence.db.ArangoDB;
import utils.PlayUtils;

/**
 * Created by leonard on 09.03.16.
 */
public class ApplicationModule extends AbstractModule {


  @Override protected void configure() {
    bind(PlayUtils.class).asEagerSingleton();
    bind(ArangoDB.class).asEagerSingleton();
  }
}
