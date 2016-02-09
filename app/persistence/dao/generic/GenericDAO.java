package persistence.dao.generic;

import com.arangodb.ArangoException;
import persistence.db.ArangoDB;

import java.util.List;

/**
 * Created by Leonard Daume on 04.02.2016.
 */
public interface GenericDAO<T> {
  List<T> getAll();

  ArangoDB getArangoDB();

  String upsert(String searchExpressionKey,
                String searchExpressionValue,
                Object updateExpression,
                Object insertExpression) throws ArangoException;

  String upsert(Object objectToStore) throws ArangoException;
}
