package persistence.dao.generic;

import com.arangodb.ArangoException;

import java.util.List;

/**
 * Created by Leonard Daume on 15.03.2016.
 */
public interface GenericDao<T> {
  List<T> getAll();

  String upsert(String searchExpressionKey,
                String searchExpressionValue,
                Object updateExpression,
                Object insertExpression) throws ArangoException;

  String upsert(Object objectToStore) throws ArangoException;

  String upsert(Object objectToSearch, Object objectToStore) throws ArangoException;
}
