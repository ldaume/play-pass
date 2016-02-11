package persistence.dao;

import be.objectify.deadbolt.core.models.Subject;
import com.arangodb.ArangoException;
import com.arangodb.DocumentCursor;
import com.arangodb.entity.DocumentEntity;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import de.qaware.heimdall.Password;
import de.qaware.heimdall.PasswordException;
import de.qaware.heimdall.PasswordFactory;
import persistence.dao.generic.GenericDAOImpl;
import persistence.db.ArangoDB;
import persistence.entity.AuthorisedUser;

import java.util.Map;
import java.util.Optional;

/**
 * Created by Leonard Daume on 22.11.2015.
 */
public class UserDao extends GenericDAOImpl<AuthorisedUser> implements IUserDao {
  @Inject ArangoDB arangoDB;

  @Override public Optional<Subject> findByEmail(final String email) {
    String query = "FOR u IN " + ArangoDB.AUTHORIZED_USERS_COLLECTION + " FILTER u.email == @email RETURN u";
    Map<String, Object> bindVars = ImmutableMap.of("email", email);

    DocumentCursor<AuthorisedUser> documentCursor = null;
    try {
      documentCursor = arangoDB.getArangoDriver()
                               .executeDocumentQuery(query,
                                                     bindVars,
                                                     arangoDB.getArangoDriver().getDefaultAqlQueryOptions(),
                                                     AuthorisedUser.class);
    } catch (ArangoException e) {
      return Optional.empty();
    }

    final Optional<DocumentEntity<AuthorisedUser>> entityOptional = documentCursor.asList().stream().findFirst();
    if ( entityOptional.isPresent() ) {
      return Optional.ofNullable(entityOptional.get().getEntity());
    }
    return Optional.empty();
  }

  @Override public Optional<Subject> findByEmailAndPassword(final String email, final String password) {
    final Password passwordFactory = PasswordFactory.create();
    try {
      final String hash = passwordFactory.hash(password.toCharArray());
    } catch (PasswordException e) {
      e.printStackTrace();
    }
    String query = "FOR u IN "
                   + ArangoDB.AUTHORIZED_USERS_COLLECTION
                   + " FILTER u.email == @email AND u.password == @password"
                   + " RETURN u";
    Map<String, Object> bindVars = ImmutableMap.of("email", email, "password", password);

    DocumentCursor<AuthorisedUser> documentCursor = null;
    try {
      documentCursor = arangoDB.getArangoDriver()
                               .executeDocumentQuery(query,
                                                     bindVars,
                                                     arangoDB.getArangoDriver().getDefaultAqlQueryOptions(),
                                                     AuthorisedUser.class);
    } catch (ArangoException e) {
      return Optional.empty();
    }

    final Optional<DocumentEntity<AuthorisedUser>> entityOptional = documentCursor.asList().stream().findFirst();
    if ( entityOptional.isPresent() ) {
      return Optional.ofNullable(entityOptional.get().getEntity());
    }
    return Optional.empty();
  }

  @Override public ArangoDB getArangoDB() {
    return arangoDB;
  }

  @Override protected String getCollectionName() {
    return ArangoDB.AUTHORIZED_USERS_COLLECTION;
  }

  @Override protected Class<?> getGenericType() {
    return AuthorisedUser.class;
  }
}
