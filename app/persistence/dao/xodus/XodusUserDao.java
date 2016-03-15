package persistence.dao.xodus;

import be.objectify.deadbolt.java.models.Subject;
import com.arangodb.ArangoException;
import de.qaware.heimdall.Password;
import de.qaware.heimdall.PasswordException;
import de.qaware.heimdall.PasswordFactory;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.StoreTransaction;
import jetbrains.exodus.entitystore.StoreTransactionalComputable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import persistence.dao.UserDao;
import persistence.db.Database;
import persistence.db.XodusDatabase;
import persistence.entity.AuthorisedUser;
import play.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Created by Leonard Daume on 15.03.2016.
 */
public class XodusUserDao implements UserDao {

  private final XodusDatabase db;

  public XodusUserDao(Database database) {
    db = (XodusDatabase) database;
  }

  @Override public Optional<Subject> findByEmail(final String email) {
    final Entity userEntity = db.getEntityStore()
                                .computeInReadonlyTransaction(new StoreTransactionalComputable<Entity>() {
                                  @Override
                                  public Entity compute(
                                    @NotNull
                                    final StoreTransaction txn) {
                                    return txn.find(Database.AUTHORIZED_USERS_COLLECTION, "email", email)
                                              .distinct()
                                              .getFirst();
                                  }
                                });
    final Optional<AuthorisedUser> authorisedUserOptional = db.userEntityToAuthorisedUser(userEntity);
    return asOptionalSubject(authorisedUserOptional);
  }

  @Override public Optional<Subject> findByEmailAndPassword(final String email, final String password) {
    final Entity userEntity = db.getEntityStore()
                                .computeInReadonlyTransaction(new StoreTransactionalComputable<Entity>() {
                                  @Override
                                  public Entity compute(
                                    @NotNull
                                    final StoreTransaction txn) {
                                    return txn.find(Database.AUTHORIZED_USERS_COLLECTION, "email", email)
                                              .distinct()
                                              .getFirst();
                                  }
                                });
    final Optional<AuthorisedUser> authorisedUser = db.userEntityToAuthorisedUser(userEntity);
    if ( authorisedUser.isPresent() ) {
      final Password passwordFactory = PasswordFactory.create();
      try {
        final String hash = passwordFactory.hash(password.toCharArray());
        if ( StringUtils.equals(hash, authorisedUser.get().getPassword()) ) {
          return asOptionalSubject(authorisedUser);
        }
      } catch (PasswordException e) {
        Logger.error("Could not hash password.", e);
      }
    }
    return Optional.empty();
  }

  private Optional<Subject> asOptionalSubject(final Optional<AuthorisedUser> authorisedUserOptional) {
    if ( authorisedUserOptional.isPresent() ) {
      return Optional.of(authorisedUserOptional.get());
    }
    return Optional.empty();
  }

  // TODO implement
  @Override public List getAll() {
    return null;
  }

  // TODO implement
  @Override public String upsert(final String searchExpressionKey,
                                 final String searchExpressionValue,
                                 final Object updateExpression,
                                 final Object insertExpression) throws ArangoException {
    return null;
  }

  // TODO implement
  @Override public String upsert(final Object objectToStore) throws ArangoException {
    return null;
  }

  // TODO implement
  @Override public String upsert(final Object objectToSearch, final Object objectToStore) throws ArangoException {
    return null;
  }
}
