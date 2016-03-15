package persistence.dao;

import com.arangodb.ArangoException;
import persistence.dao.generic.GenericDao;
import persistence.entity.Password;

import java.util.List;

/**
 * Created by Leonard Daume on 04.02.2016.
 */
public interface PasswordDao extends GenericDao {
  void saveOrUpdateAllCredentials(List<Password> passwordsToSave);

  List<Password> delete(Password password) throws ArangoException;
}
