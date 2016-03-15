package modules;

import com.google.inject.AbstractModule;
import org.apache.commons.lang3.StringUtils;
import persistence.dao.PasswordDao;
import persistence.dao.UserDao;
import persistence.dao.arango.ArangoPasswordDao;
import persistence.dao.arango.ArangoUserDao;
import persistence.db.ArangoDatabase;
import persistence.db.XodusDatabase;
import play.Configuration;
import play.Environment;
import utils.PlayUtils;

/**
 * Created by leonard on 09.03.16.
 */
public class ApplicationModule extends AbstractModule {

  private final Environment environment;
  private final Configuration configuration;

  public ApplicationModule(Environment environment, Configuration configuration) {
    this.environment = environment;
    this.configuration = configuration;
  }

  @Override protected void configure() {
    bind(PlayUtils.class).asEagerSingleton();
    final String dbType = configuration.getString("db.type", "arango");
    if ( StringUtils.equals(dbType, "arango") ) {
      bind(ArangoDatabase.class).asEagerSingleton();
      bind(UserDao.class).to(ArangoUserDao.class);
      bind(PasswordDao.class).to(ArangoPasswordDao.class);
    } else if ( StringUtils.equals(dbType, "xodus") ) {
      bind(XodusDatabase.class).asEagerSingleton();
    }
  }
}
