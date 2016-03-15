package persistence.dao.xodus;

import com.arangodb.ArangoException;
import persistence.dao.PasswordDao;
import persistence.entity.Password;

import java.util.List;

/**
 * Created by Leonard Daume on 15.03.2016.
 */
public class XodusPasswordDao implements PasswordDao {

  // TODO implement
  @Override public void saveOrUpdateAllCredentials(final List<Password> passwordsToSave) {

  }

  // TODO implement
  @Override public List<Password> delete(final Password password) throws ArangoException {
    return null;
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
