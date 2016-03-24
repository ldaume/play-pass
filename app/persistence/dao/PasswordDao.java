package persistence.dao;

import com.arangodb.ArangoException;
import persistence.dao.generic.GenericDao;
import persistence.entity.Password;
import play.Logger;

import java.util.List;

/**
 * Created by Leonard Daume on 04.02.2016.
 */
public interface PasswordDao extends GenericDao {
    default void saveOrUpdateAllCredentials(List<Password> passwordsToSave) {
        passwordsToSave.parallelStream().forEach(passwordToSave -> {
            try {
//                final List<String> updatesOrInserts = Lists.newArrayList();
                String upsert = upsert(passwordToSave);
                /*Iterator<JsonNode> elements = Json.parse(upsert)
                        .elements();
                elements
                        .forEachRemaining(jsonNode -> updatesOrInserts.add(jsonNode.get("type").asText()));
                Logger.info("{} done.", updatesOrInserts);*/
            } catch (ArangoException e) {
                Logger.error("Could not upsert the credentialUser {}.", passwordToSave);
                throw new RuntimeException(e);
            }
        });
    }

    List<Password> delete(Password password) throws ArangoException;
}
