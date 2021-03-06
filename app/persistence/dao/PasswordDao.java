package persistence.dao;

import com.arangodb.ArangoException;
import com.arangodb.DocumentCursor;
import com.arangodb.entity.DocumentEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import persistence.dao.generic.GenericDAOImpl;
import persistence.db.ArangoDB;
import persistence.entity.Password;
import play.Logger;
import play.libs.Json;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created by Leonard Daume on 22.11.2015.
 */
public class PasswordDao extends GenericDAOImpl<Password> implements IPasswordDAO {
  @Inject ArangoDB arangoDB;

  public void saveOrUpdateAllCredentials(List<Password> passwordsToSave) {
    passwordsToSave.parallelStream().forEach(passwordToSave -> {
      try {
        final List<String> updatesOrInserts = Lists.newArrayList();
        Json.parse(upsert(passwordToSave))
            .elements()
            .forEachRemaining(jsonNode -> updatesOrInserts.add(jsonNode.get("type").asText()));
        Logger.info("{} done.", updatesOrInserts);
      } catch (ArangoException e) {
        Logger.error("Could not upsert the credentialUser {}.", passwordToSave);
        throw new RuntimeException(e);
      }
    });
  }

  public List<Password> delete(final Password password) throws ArangoException {
    final StringBuilder queryBuilder = new StringBuilder("FOR p in " + ArangoDB.PASSWORDS_COLLECTION + " FILTER ");
    final JsonNode asJson = Json.toJson(password);
    final Map<String, Object> bindVars = Json.mapper().convertValue(asJson, Map.class);
    final AtomicBoolean firstFilter = new AtomicBoolean(true);
    bindVars.keySet().forEach(key -> {
      if ( firstFilter.getAndSet(false) ) {
        queryBuilder.append(" p." + key + " == @" + key);
      } else {
        queryBuilder.append(" && p." + key + " == @" + key);
      }
    });
    queryBuilder.append(" REMOVE p IN " + ArangoDB.PASSWORDS_COLLECTION + " LET removed = OLD RETURN removed");
    final String query = queryBuilder.toString();
    final DocumentCursor<Password> passwordsToDelete = arangoDB.getArangoDriver()
                                                               .executeDocumentQuery(query,
                                                                                     bindVars,
                                                                                     arangoDB.getArangoDriver()
                                                                                             .getDefaultAqlQueryOptions(),
                                                                                     Password.class);
    return passwordsToDelete.asList().stream().map(DocumentEntity::getEntity).collect(Collectors.toList());
  }

  @Override public ArangoDB getArangoDB() {
    return arangoDB;
  }

  @Override protected String getCollectionName() {
    return ArangoDB.PASSWORDS_COLLECTION;
  }

  @Override protected Class<?> getGenericType() {
    return Password.class;
  }
}
