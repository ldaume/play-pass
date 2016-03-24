package persistence.dao.arango;

import com.arangodb.ArangoException;
import com.arangodb.DocumentCursor;
import com.arangodb.entity.DocumentEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import persistence.dao.PasswordDao;
import persistence.dao.generic.arango.GenericArangoDao;
import persistence.db.ArangoDatabase;
import persistence.entity.Password;
import play.libs.Json;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created by Leonard Daume on 22.11.2015.
 */
public class ArangoPasswordDao extends GenericArangoDao<Password> implements PasswordDao {
    @Inject
    ArangoDatabase arangoDB;


    @Override
    public List<Password> delete(final Password password) throws ArangoException {
        final StringBuilder queryBuilder = new StringBuilder("FOR p in "
                + ArangoDatabase.PASSWORDS_COLLECTION
                + " FILTER ");
        final JsonNode asJson = Json.toJson(password);
        final Map<String, Object> bindVars = Json.mapper().convertValue(asJson, Map.class);
        final AtomicBoolean firstFilter = new AtomicBoolean(true);
        bindVars.keySet().forEach(key -> {
            if (firstFilter.getAndSet(false)) {
                queryBuilder.append(" p." + key + " == @" + key);
            } else {
                queryBuilder.append(" && p." + key + " == @" + key);
            }
        });
        queryBuilder.append(" REMOVE p IN " + ArangoDatabase.PASSWORDS_COLLECTION + " LET removed = OLD RETURN removed");
        final String query = queryBuilder.toString();
        final DocumentCursor<Password> passwordsToDelete = arangoDB.getArangoDriver()
                .executeDocumentQuery(query,
                        bindVars,
                        arangoDB.getArangoDriver()
                                .getDefaultAqlQueryOptions(),
                        Password.class);
        return passwordsToDelete.asList().stream().map(DocumentEntity::getEntity).collect(Collectors.toList());
    }

    @Override
    public ArangoDatabase getArangoDB() {
        return arangoDB;
    }

    @Override
    protected String getCollectionName() {
        return ArangoDatabase.PASSWORDS_COLLECTION;
    }

    @Override
    protected Class<?> getGenericType() {
        return Password.class;
    }
}
