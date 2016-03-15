package persistence.dao;

import be.objectify.deadbolt.java.models.Subject;
import persistence.dao.generic.GenericDao;

import java.util.Optional;

/**
 * Created by Leonard Daume on 04.02.2016.
 */
public interface UserDao extends GenericDao {
  Optional<Subject> findByEmail(String email);

  Optional<Subject> findByEmailAndPassword(String email, String password);
}
