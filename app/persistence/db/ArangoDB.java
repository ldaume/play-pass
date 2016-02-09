package persistence.db;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.ArangoHost;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.UserEntity;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import persistence.entity.AuthorisedUser;
import play.Logger;
import play.Play;
import play.libs.Json;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
/**
 * Created by Leonard Daume on 22.11.2015.
 */


/**
 * The connection to a ArangoDB.
 * <p>
 * Created by Leonard Daume on 16.10.2015.
 */
@Singleton
public class ArangoDB {

  public static final String PASSWORDS_COLLECTION = "Passwords";
  public static final String AUTHORIZED_USERS_COLLECTION = "AuthorisedUsers";
  private static final String PASSWORDS_DB = "ReinventPasswords";

  private ArangoDriver arangoDriver;

  @Inject public ArangoDB() {
    initDb(null, null, null, null, null, false);
  }

  /**
   * DB bootstrapping.
   *
   * @param hostIp
   * @param username
   * @param password
   * @param db
   * @param collections
   * @param truncateCollections
   */
  private void initDb(final String hostIp,
                      final String username,
                      final String password,
                      final String db,
                      final List<String> collections,
                      final boolean truncateCollections) {
    // Initialize configure
    ArangoConfigure configure = new ArangoConfigure();
    String host = Play.application().configuration().getString("arango.host", "localhost");
    Integer port = Play.application().configuration().getInt("arango.port", 8529);
    configure.setArangoHost(new ArangoHost(Optional.ofNullable(hostIp).orElse(host), port));
    String arangoUsername = Play.application().configuration().getString("arango.username", "root");
    configure.setUser(Optional.ofNullable(username).orElse(arangoUsername));
    String arangoPassword = Play.application().configuration().getString("arango.password", "pw");
    Logger.info("Will connect to arango as user ({}) with pw ({}) @ {}:{} ...",
                arangoUsername,
                arangoPassword,
                host,
                port);
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      Logger.error("Could not sleep.");
    }
    configure.setPassword(Optional.ofNullable(password).orElse(arangoPassword));
    configure.init();

    // Create Driver (this instance is thread-safe)
    arangoDriver = new ArangoDriver(configure);

    try {
      String dbName = Optional.ofNullable(db).orElse(PASSWORDS_DB);
      if ( !arangoDriver.getDatabases().getResult().contains(dbName) ) {
        UserEntity user = arangoDriver.getUser(configure.getUser());
        user.setPassword(configure.getPassword());
        arangoDriver.createDatabase(dbName, user);
      }
      arangoDriver = new ArangoDriver(configure, dbName);
      initCollection(collections, truncateCollections);
    } catch (ArangoException e) {
      Logger.error("Could not change db", e);
    }
    Logger.info("... connection established.");
  }

  /**
   * Collections bootstrapping.
   *
   * @param collections
   * @param truncateCollections
   *
   * @throws ArangoException
   */
  private void initCollection(final List<String> collections, final boolean truncateCollections)
    throws ArangoException {
    final List<CollectionEntity> existingCollectionEntities = arangoDriver.getCollections().getCollections();
    final List<String> existingColNames = Lists.newArrayList();
    existingCollectionEntities.forEach(collectionEntity -> existingColNames.add(collectionEntity.getName()));
    final List<String> collectionsToAdd = Optional.ofNullable(collections)
                                                  .orElse(Lists.newArrayList(PASSWORDS_COLLECTION,
                                                                             AUTHORIZED_USERS_COLLECTION));
    for ( String collection : collectionsToAdd ) {
      final boolean isAuthorisedUserCollection = StringUtils.equals(collection, AUTHORIZED_USERS_COLLECTION);
      if ( !existingColNames.contains(collection) ) {
        arangoDriver.createCollection(collection);
        if ( isAuthorisedUserCollection ) {
          getArangoDriver().createHashIndex(AUTHORIZED_USERS_COLLECTION, true, "email");
        }
      }
      if ( truncateCollections ) {
        arangoDriver.truncateCollection(collection);
      }
      if ( isAuthorisedUserCollection ) {
        final String jsonString = Play.application().configuration().getString("authorised.users");
        if ( StringUtils.isNotBlank(jsonString) ) {
          try {
            final List<AuthorisedUser> allDefaultUsers = Json.mapper()
                                                             .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
                                                             .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
                                                                        true)
                                                             .readValue(jsonString,
                                                                        TypeFactory.defaultInstance()
                                                                                   .constructCollectionType(List.class,
                                                                                                            AuthorisedUser.class));
            allDefaultUsers.forEach(authorisedUser -> {
              final JsonNode jsonNode = Json.toJson(authorisedUser);
              final String query = "UPSERT { email: \""
                                   + authorisedUser.getEmail()
                                   + "\"} INSERT "
                                   + jsonNode.toString()
                                   + " UPDATE "
                                   + jsonNode.toString()
                                   + " IN "
                                   + AUTHORIZED_USERS_COLLECTION;
              try {
                getArangoDriver().executeAqlQueryJSON(query,
                                                      ImmutableMap.of(),
                                                      getArangoDriver().getDefaultAqlQueryOptions());
              } catch (ArangoException e) {
                Logger.error("Could not upsert the user {}", jsonNode);
              }
            });
          } catch (IOException e) {
            Logger.error("Could not add default users.");
          }
        }
      }
    }
  }

  public ArangoDriver getArangoDriver() {
    return arangoDriver;
  }

  public ArangoDB(final String hostIp,
                  final String username,
                  final String password,
                  final String db,
                  final List<String> collections,
                  final boolean truncateCollections) {
    initDb(hostIp, username, password, db, collections, truncateCollections);
  }
}
