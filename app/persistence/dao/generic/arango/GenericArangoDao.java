package persistence.dao.generic.arango;

import com.arangodb.ArangoException;
import com.arangodb.DocumentCursor;
import com.arangodb.entity.DocumentEntity;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import play.libs.Json;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Leonard Daume on 04.02.2016.
 */
public abstract class GenericArangoDao<T> implements GenericArangoDB<T> {

  /**
   * Gets all available Documents from a collection and maps them to T.
   *
   * @return the list of T
   */
  @Override public List<T> getAll() {
    DocumentCursor<T> documentCursor = null;
    try {
      documentCursor = getArangoDB().getArangoDriver()
                                    .executeDocumentQuery("FOR t IN " + getCollectionName() + " RETURN t",
                                                          ImmutableMap.of(),
                                                          getArangoDB().getArangoDriver().getDefaultAqlQueryOptions(),
                                                          (Class<T>) getGenericType());
    } catch (ArangoException e) {
      return Lists.newArrayList();
    }
    return documentCursor.asList().stream().map(DocumentEntity::getEntity).collect(Collectors.toList());
  }

  /**
   * The name of the collections where T is stored.
   *
   * @return the collection name
   */
  protected abstract String getCollectionName();

  /**
   * The class of the entity to map.
   *
   * @return class
   */
  protected abstract Class<?> getGenericType();

  /**
   * Executes an upsert query and returns the raw JSON response. <p> Can be used for checking whether certain documents
   * exist, and to update them in case they exist, or create them in case they do not exist. On a single server, upserts
   * are executed transactionally in an all-or-nothing fashion. For sharded collections, the entire update operation is
   * not transactional. </p>
   *
   * @param searchExpressionKey   contains the document key to be looked for. <ul><li>In case at least one document in
   *                              collection matches the search-expression, it will be updated using the
   *                              update-expression. When more than one document in the collection matches the
   *                              search-expression, it is undefined which of the matching documents will be
   *                              updated.</li><li>In case no such document can be found in collection, a new document
   *                              will be inserted into the collection as specified in the insert-expression</li></ul>
   * @param searchExpressionValue contains the document value for the specified key to be looked for.
   * @param updateExpression      any object which is mappable to a JsonNode.
   * @param insertExpression      any object which is mappable to a JsonNode.
   *
   * @return the statement.
   */
  @Override public String upsert(final String searchExpressionKey,
                                 final String searchExpressionValue,
                                 final Object updateExpression,
                                 final Object insertExpression) throws ArangoException {
    final String query = "UPSERT { "
                         + searchExpressionKey
                         + ": \""
                         + searchExpressionValue
                         + "\"} INSERT "
                         + Json.toJson(insertExpression).toString()
                         + " UPDATE "
                         + Json.toJson(updateExpression).toString()
                         + " IN "
                         + getCollectionName()
                         + " RETURN { doc: NEW, type: OLD ? 'update' : 'insert' }";
    return executeAqlQuery(query);
  }

  private String executeAqlQuery(final String query) throws ArangoException {
    return getArangoDB().getArangoDriver()
                        .executeAqlQueryJSON(query,
                                             ImmutableMap.of(),
                                             getArangoDB().getArangoDriver().getDefaultAqlQueryOptions());
  }

  @Override public String upsert(final Object objectToStore) throws ArangoException {
    final String jsonToStore = Json.toJson(objectToStore).toString();
    final String query = "UPSERT "
                         + jsonToStore
                         + " INSERT "
                         + jsonToStore
                         + " UPDATE "
                         + jsonToStore
                         + " IN "
                         + getCollectionName()
                         + " RETURN { doc: NEW, type: OLD ? 'update' : 'insert' }";
    return executeAqlQuery(query);
  }

  @Override public String upsert(final Object objectToSearch, final Object objectToStore) throws ArangoException {
    final String jsonToSearch = Json.toJson(objectToSearch).toString();
    final String jsonToStore = Json.toJson(objectToStore).toString();
    final String query = "UPSERT "
                         + jsonToSearch
                         + " INSERT "
                         + jsonToStore
                         + " UPDATE "
                         + jsonToStore
                         + " IN "
                         + getCollectionName()
                         + " RETURN { doc: NEW, type: OLD ? 'update' : 'insert' }";
    return executeAqlQuery(query);
  }
}
