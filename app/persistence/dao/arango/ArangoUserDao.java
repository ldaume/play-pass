package persistence.dao.arango;

import be.objectify.deadbolt.java.models.Subject;
import com.arangodb.ArangoException;
import com.arangodb.DocumentCursor;
import com.arangodb.entity.DocumentEntity;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import de.qaware.heimdall.Password;
import de.qaware.heimdall.PasswordException;
import de.qaware.heimdall.PasswordFactory;
import persistence.dao.UserDao;
import persistence.dao.generic.arango.GenericArangoDao;
import persistence.db.ArangoDatabase;
import persistence.entity.AuthorisedUser;
import play.Logger;

import java.util.Map;
import java.util.Optional;

/**
 * Created by Leonard Daume on 22.11.2015.
 */
public class ArangoUserDao extends GenericArangoDao<AuthorisedUser> implements UserDao {
  @Inject ArangoDatabase arangoDB;

  @Override public Optional<Subject> findByEmail(final String email) {
    String query = "FOR u IN " + ArangoDatabase.AUTHORIZED_USERS_COLLECTION + " FILTER u.email == @email RETURN u";
    Map<String, Object> bindVars = ImmutableMap.of("email", email);

    DocumentCursor<AuthorisedUser> documentCursor = null;
    try {
      documentCursor = arangoDB.getArangoDriver()
                               .executeDocumentQuery(query,
                                                     bindVars,
                                                     arangoDB.getArangoDriver().getDefaultAqlQueryOptions(),
                                                     AuthorisedUser.class);
    } catch (ArangoException e) {
      Logger.error("Could not execute query.", e);
      return Optional.empty();
    }

    final Optional<DocumentEntity<AuthorisedUser>> entityOptional = documentCursor.asList().stream().findFirst();
    if ( entityOptional.isPresent() ) {
      return Optional.ofNullable(entityOptional.get().getEntity());
    }
    return Optional.empty();
  }

  @Override public Optional<Subject> findByEmailAndPassword(final String email, final String password) {
    try {
      final Password passwordFactory = PasswordFactory.create();
      final String hash = passwordFactory.hash(password.toCharArray());
      String query = "FOR u IN "
                     + ArangoDatabase.AUTHORIZED_USERS_COLLECTION
                     + " FILTER u.email == @email AND u.password == @password"
                     + " RETURN u";
      Map<String, Object> bindVars = ImmutableMap.of("email", email, "password", password);

      DocumentCursor<AuthorisedUser> documentCursor = null;
      documentCursor = arangoDB.getArangoDriver()
                               .executeDocumentQuery(query,
                                                     bindVars,
                                                     arangoDB.getArangoDriver().getDefaultAqlQueryOptions(),
                                                     AuthorisedUser.class);
      final Optional<DocumentEntity<AuthorisedUser>> entityOptional = documentCursor.asList().stream().findFirst();
      if ( entityOptional.isPresent() ) {
        return Optional.ofNullable(entityOptional.get().getEntity());
      }
    } catch (PasswordException e) {
      Logger.error("Could not hash the password.", e);
    } catch (ArangoException e) {
      Logger.error("Could not execute query.", e);
    }
    return Optional.empty();
  }

  @Override public ArangoDatabase getArangoDB() {
    return arangoDB;
  }

  @Override protected String getCollectionName() {
    return ArangoDatabase.AUTHORIZED_USERS_COLLECTION;
  }

  @Override protected Class<?> getGenericType() {
    return AuthorisedUser.class;
  }
}
