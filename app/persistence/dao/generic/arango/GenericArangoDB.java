package persistence.dao.generic.arango;

import persistence.dao.generic.GenericDao;
import persistence.db.ArangoDatabase;

/**
 * Created by Leonard Daume on 04.02.2016.
 */
public interface GenericArangoDB<T> extends GenericDao {

  ArangoDatabase getArangoDB();
}
