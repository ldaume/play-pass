package services.security;

import be.objectify.deadbolt.java.models.Subject;
import com.arangodb.ArangoException;
import com.google.inject.Inject;
import de.qaware.heimdall.Password;
import de.qaware.heimdall.PasswordException;
import de.qaware.heimdall.PasswordFactory;
import exceptions.AuthException;
import persistence.dao.UserDao;
import persistence.entity.AuthorisedUser;
import play.Configuration;
import play.cache.CacheApi;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by leonard on 16.02.16.
 */
public class AuthService {

  @Inject private UserDao userDao;

  @Inject private CacheApi cacheApi;


  @Inject
  private Configuration configuration;

  public UUID authorise(final AuthorisedUser user) throws AuthException, ArangoException, PasswordException {
    final Optional<Subject> byEmail = userDao.findByEmail(user.getEmail());
    if ( byEmail.isPresent() ) {
      final AuthorisedUser authorisedUser = (AuthorisedUser) byEmail.get();
      final Password password = PasswordFactory.create();
      if ( password.verify(user.getPassword().toCharArray(), authorisedUser.getPassword()) ) {
        // Check if the hash uses an old hash
        if ( password.needsRehash(authorisedUser.getPassword()) ) {
          // algorithm, insecure parameters, etc.
          String newHash = password.hash(user.getPassword().toCharArray());
          authorisedUser.setPassword(newHash);
          userDao.upsert("email", authorisedUser.getEmail(), authorisedUser, authorisedUser);
        }
        final UUID uuid = UUID.randomUUID();
        cacheApi.set(uuid.toString(),
                     authorisedUser,
                     Long.valueOf(Duration.ofMinutes(configuration.getInt("session.timeout"))
                                          .getSeconds()).intValue());
        return uuid;
      } else {
        throw new AuthException("No User found.");
      }
    } else {
      throw new AuthException("No User found.");
    }
  }
}
