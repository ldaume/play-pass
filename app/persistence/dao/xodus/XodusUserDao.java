package persistence.dao.xodus;

import be.objectify.deadbolt.java.models.Subject;
import com.arangodb.ArangoException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import de.qaware.heimdall.Password;
import de.qaware.heimdall.PasswordException;
import de.qaware.heimdall.PasswordFactory;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import persistence.dao.UserDao;
import persistence.db.Database;
import persistence.db.XodusDatabase;
import persistence.entity.AuthorisedUser;
import play.Logger;
import play.libs.Json;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Leonard Daume on 15.03.2016.
 */
public class XodusUserDao implements UserDao {

    @Inject
    private XodusDatabase db;


    @Override
    public Optional<Subject> findByEmail(final String email) {
        final Optional<AuthorisedUser> userEntity = db.getEntityStore()
                .computeInReadonlyTransaction(txn -> {
                    Entity entity = txn.find(Database.AUTHORIZED_USERS_COLLECTION, "email", email)
                            .distinct()
                            .getFirst();
                    return db.userEntityToAuthorisedUser(entity);
                });
        return asOptionalSubject(userEntity);
    }

    @Override
    public Optional<Subject> findByEmailAndPassword(final String email, final String password) {
        final Optional<AuthorisedUser> authorisedUser = db.getEntityStore()
                .computeInReadonlyTransaction(txn -> {
                    Entity entity = txn.find(Database.AUTHORIZED_USERS_COLLECTION, "email", email)
                            .distinct()
                            .getFirst();
                    return db.userEntityToAuthorisedUser(entity);
                });
        if (authorisedUser.isPresent()) {
            final Password passwordFactory = PasswordFactory.create();
            try {
                final String hash = passwordFactory.hash(password.toCharArray());
                if (StringUtils.equals(hash, authorisedUser.get().getPassword())) {
                    return asOptionalSubject(authorisedUser);
                }
            } catch (PasswordException e) {
                Logger.error("Could not hash password.", e);
            }
        }
        return Optional.empty();
    }

    private Optional<Subject> asOptionalSubject(final Optional<AuthorisedUser> authorisedUserOptional) {
        if (authorisedUserOptional.isPresent()) {
            return Optional.of(authorisedUserOptional.get());
        }
        return Optional.empty();
    }

    @Override
    public List<AuthorisedUser> getAll() {
        List<AuthorisedUser> authorisedUsers = db.getEntityStore()
                .computeInReadonlyTransaction(txn -> {
                    final List<AuthorisedUser> entities = Lists.newArrayList();
                    txn.getAll(Database.AUTHORIZED_USERS_COLLECTION).forEach(entity -> db.userEntityToAuthorisedUser(entity).ifPresent(entities::add));
                    return entities;
                });
        return authorisedUsers;
    }

    @Override
    public String upsert(final String searchExpressionKey,
                         final String searchExpressionValue,
                         final Object updateExpression,
                         final Object insertExpression) throws ArangoException {
        final Map<String, String> oldAndNewUser = Maps.newHashMap();
        db.getEntityStore()
                .executeInTransaction(txn -> {
                    EntityIterable allUsers = txn.getAll(Database.AUTHORIZED_USERS_COLLECTION);
                    Entity existingUser = findExistingUser(searchExpressionKey, searchExpressionValue, allUsers);
                    if (existingUser == null) {
                        final Entity newUser = txn.newEntity(Database.AUTHORIZED_USERS_COLLECTION);
                        AuthorisedUser authorisedUser = (AuthorisedUser) insertExpression;
                        setUserEntity(newUser, authorisedUser);
                        oldAndNewUser.put("old", Json.newObject().toString());
                        oldAndNewUser.put("new", Json.toJson(authorisedUser).toString());
                    } else {
                        AuthorisedUser authorisedUser = (AuthorisedUser) updateExpression;
                        oldAndNewUser.put("old", Json.toJson(db.userEntityToAuthorisedUser(existingUser)).toString());
                        setUserEntity(existingUser, authorisedUser);
                        oldAndNewUser.put("new", Json.toJson(authorisedUser).toString());
                    }
                });
        return Json.toJson(oldAndNewUser).toString();
    }

    @Override
    public String upsert(final Object objectToStore) throws ArangoException {
        final Map<String, String> oldAndNewUser = Maps.newHashMap();
        final AuthorisedUser userToStore = (AuthorisedUser) objectToStore;
        db.getEntityStore()
                .executeInTransaction(txn -> {
                    EntityIterable allUsers = txn.getAll(Database.AUTHORIZED_USERS_COLLECTION);
                    Entity existingUser = findExistingUser("email", userToStore.getEmail(), allUsers);
                    persistUser(oldAndNewUser, userToStore, txn, existingUser);
                });
        return Json.toJson(oldAndNewUser).toString();
    }


    @Override
    public String upsert(final Object objectToSearch, final Object objectToStore) throws ArangoException {
        final Map<String, String> oldAndNewUser = Maps.newHashMap();
        final AuthorisedUser userToStore = (AuthorisedUser) objectToStore;
        db.getEntityStore()
                .executeInTransaction(txn -> {
                    EntityIterable allUsers = txn.getAll(Database.AUTHORIZED_USERS_COLLECTION);
                    Entity existingUser = findExistingUser("email", ((AuthorisedUser) objectToSearch).getEmail(), allUsers);
                    persistUser(oldAndNewUser, userToStore, txn, existingUser);
                });
        return Json.toJson(oldAndNewUser).toString();
    }

    @Nullable
    private Entity findExistingUser(String searchExpressionKey, String searchExpressionValue, EntityIterable allUsers) {
        Entity existingUser = null;
        for (Entity user : allUsers) {
            Comparable searchValue = user.getProperty(searchExpressionKey);
            if (searchExpressionValue.equals(searchValue)) {
                existingUser = user;
                break;
            }
        }
        return existingUser;
    }

    private void setUserEntity(Entity newUser, AuthorisedUser authorisedUser) {
        newUser.setProperty("email", authorisedUser.getEmail());
        newUser.setProperty("password", authorisedUser.getPassword());
        newUser.setBlobString("json", Json.toJson(authorisedUser).toString());
    }


    private void persistUser(final Map<String, String> oldAndNewUser, final AuthorisedUser userToStore, final StoreTransaction txn, final Entity existingUser) {
        if (existingUser == null) {
            final Entity newUser = txn.newEntity(Database.AUTHORIZED_USERS_COLLECTION);
            setUserEntity(newUser, userToStore);
            oldAndNewUser.put("old", Json.newObject().toString());
        } else {
            oldAndNewUser.put("old", Json.toJson(db.userEntityToAuthorisedUser(existingUser)).toString());
            setUserEntity(existingUser, userToStore);
        }
        oldAndNewUser.put("new", Json.toJson(userToStore).toString());
    }
}
