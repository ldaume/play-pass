package persistence.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import de.qaware.heimdall.Password;
import de.qaware.heimdall.PasswordException;
import de.qaware.heimdall.PasswordFactory;
import jetbrains.exodus.entitystore.*;
import org.jetbrains.annotations.NotNull;
import persistence.entity.AuthorisedUser;
import play.Configuration;
import play.Logger;
import play.libs.Json;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Created by Leonard Daume on 15.03.2016.
 */
public class XodusDatabase implements Database {

    private final PersistentEntityStore entityStore;
    private final Configuration configuration;

    @Inject
    public XodusDatabase(Configuration configuration) {
        this.configuration = configuration;
        final String dbDir = configuration.getString("xodus.dir", "embeddedDB");
        Logger.info("Will stablish embedded database in {} ...", dbDir);
        entityStore = PersistentEntityStores.newInstance(dbDir);
        initDB();
    }

    private void initDB() {
        final Boolean dbEstablished = entityStore.computeInTransaction(new StoreTransactionalComputable<Boolean>() {
            @Override
            public Boolean compute(
                    @NotNull
                    final StoreTransaction txn) {
                try {
                    final EntityIterable all = txn.getAll(AUTHORIZED_USERS_COLLECTION);
                    final List<AuthorisedUser> authorisedUsers = getAuthorisedUsers();
                    final Password password = PasswordFactory.create();
                    authorisedUsers.forEach(authorisedUser -> {
                        try {
                            authorisedUser.setPassword(password.hash(authorisedUser.getPassword().toCharArray()));
                        } catch (PasswordException e) {
                            throw new RuntimeException(e);
                        }
                        final Entity existingUser = txn.find(AUTHORIZED_USERS_COLLECTION, "email", authorisedUser.getEmail())
                                .distinct()
                                .getFirst();
                        if (existingUser == null) {
                            final Entity newUser = txn.newEntity(AUTHORIZED_USERS_COLLECTION);
                            newUser.setProperty("email", authorisedUser.getEmail());
                            try {
                                authorisedUser.setPassword(password.hash(authorisedUser.getPassword().toCharArray()));
                            } catch (PasswordException e) {
                                throw new RuntimeException(e);
                            }
                            newUser.setProperty("password", authorisedUser.getPassword());
                            newUser.setBlobString("json", Json.toJson(authorisedUser).toString());
                        } else {
                            existingUser.setProperty("password", authorisedUser.getPassword());
                            existingUser.setBlobString("json", Json.toJson(authorisedUser).toString());
                            txn.saveEntity(existingUser);
                        }
                    });
                    return true;
                } catch (IOException e) {
                    Logger.error("... could not init db.", e);
                    return false;
                }
            }
        });
        Logger.info("... database established {}.", dbEstablished);
    }

    public Optional<AuthorisedUser> userEntityToAuthorisedUser(final Entity userEntity) {
        if (userEntity != null) {
            try {
                return Optional.of(Json.mapper()
                        .treeToValue(Json.parse(userEntity.getBlobString("json")), AuthorisedUser.class));
            } catch (JsonProcessingException e) {
                Logger.error("Could not map entity.", e);
            }
        }
        return Optional.empty();
    }

    public Optional<persistence.entity.Password> passwordEntityToPassword(final Entity passwordEntity) {
        if (passwordEntity != null) {
            try {
                return Optional.of(Json.mapper()
                        .treeToValue(Json.parse(passwordEntity.getBlobString("json")), persistence.entity.Password.class));
            } catch (JsonProcessingException e) {
                Logger.error("Could not map entity.", e);
            }
        }
        return Optional.empty();
    }

    public PersistentEntityStore getEntityStore() {
        return entityStore;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
