package persistence.dao;

import com.arangodb.ArangoException;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import persistence.dao.generic.GenericDAOImpl;
import persistence.db.ArangoDB;
import persistence.entity.Password;
import play.Logger;
import play.libs.Json;

import java.util.List;

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
