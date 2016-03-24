package modules;

import com.google.inject.AbstractModule;
import org.apache.commons.lang3.StringUtils;
import persistence.dao.PasswordDao;
import persistence.dao.UserDao;
import persistence.dao.arango.ArangoPasswordDao;
import persistence.dao.arango.ArangoUserDao;
import persistence.dao.xodus.XodusPasswordDao;
import persistence.dao.xodus.XodusUserDao;
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

    @Override
    protected void configure() {
        bind(PlayUtils.class).asEagerSingleton();
        final String dbType = configuration.getString("db.type", "xodus");
        if (StringUtils.equals(dbType, "arango")) {
            bind(ArangoDatabase.class).asEagerSingleton();
            bind(UserDao.class).to(ArangoUserDao.class);
            bind(PasswordDao.class).to(ArangoPasswordDao.class);
        } else if (StringUtils.equals(dbType, "xodus")) {
            bind(UserDao.class).to(XodusUserDao.class);
            bind(PasswordDao.class).to(XodusPasswordDao.class);
            bind(XodusDatabase.class).asEagerSingleton();
        }
    }
}
