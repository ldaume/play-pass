package persistence.dao.xodus;

import com.arangodb.ArangoException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.EntityIterator;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.jetbrains.annotations.Nullable;
import persistence.dao.PasswordDao;
import persistence.db.Database;
import persistence.db.XodusDatabase;
import persistence.entity.Password;
import play.libs.Json;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Leonard Daume on 15.03.2016.
 */
public class XodusPasswordDao implements PasswordDao {

    @Inject
    private XodusDatabase db;

    @Override
    public List<Password> delete(final Password password) throws ArangoException {
        List<Password> deletedPasswords = db.getEntityStore()
                .computeInTransaction(txn -> {
                    final List<Password> deletedEntities = Lists.newArrayList();
                    txn.getAll(Database.PASSWORDS_COLLECTION).forEach(entity -> db.passwordEntityToPassword(entity).ifPresent(possiblePwToDelete -> {
                        if (password.equals(possiblePwToDelete)) {
                            deletedEntities.add(possiblePwToDelete);
                            entity.delete();
                        }
                    }));
                    return deletedEntities;
                });
        return deletedPasswords;
    }

    @Override
    public List getAll() {
        List<Password> passwords = db.getEntityStore()
                .computeInReadonlyTransaction(txn -> {
                    final List<Password> entities = Lists.newArrayList();
                    txn.getAll(Database.PASSWORDS_COLLECTION).forEach(entity -> db.passwordEntityToPassword(entity).ifPresent(entities::add));
                    return entities;
                });
        return passwords;
    }

    @Override
    public String upsert(final String searchExpressionKey,
                         final String searchExpressionValue,
                         final Object updateExpression,
                         final Object insertExpression) throws ArangoException {
        final Map<String, String> oldAndNewPassword = Maps.newHashMap();
        db.getEntityStore()
                .executeInTransaction(txn -> {
                    EntityIterable allPasswords = txn.getAll(Database.PASSWORDS_COLLECTION);
                    Entity existingPassword = findExistingPassword(searchExpressionKey, searchExpressionValue, allPasswords);
                    if (existingPassword == null) {
                        final Entity newPassword = txn.newEntity(Database.PASSWORDS_COLLECTION);
                        Password password = (Password) insertExpression;
                        setPasswordEntity(newPassword, password);
                        oldAndNewPassword.put("old", Json.newObject().toString());
                        oldAndNewPassword.put("new", Json.toJson(password).toString());
                    } else {
                        Password password = (Password) updateExpression;
                        oldAndNewPassword.put("old", Json.toJson(db.passwordEntityToPassword(existingPassword)).toString());
                        setPasswordEntity(existingPassword, password);
                        oldAndNewPassword.put("new", Json.toJson(password).toString());
                    }
                });
        return Json.toJson(oldAndNewPassword).toString();
    }

    @Override
    public String upsert(final Object objectToStore) throws ArangoException {
        final Map<String, String> oldAndNewPassword = Maps.newHashMap();
        final Password passwordToStore = (Password) objectToStore;
        db.getEntityStore()
                .executeInTransaction(txn -> {
                    EntityIterable allPasswords = txn.getAll(Database.PASSWORDS_COLLECTION);
                    Entity existingPassword = findExistingPassword(passwordToStore, allPasswords);
                    persistPassword(oldAndNewPassword, passwordToStore, txn, existingPassword);
                });
        return Json.toJson(oldAndNewPassword).toString();
    }


    @Override
    public String upsert(final Object objectToSearch, final Object objectToStore) throws ArangoException {
        final Map<String, String> oldAndNewPassword = Maps.newHashMap();
        final Password passwordToStore = (Password) objectToStore;
        db.getEntityStore()
                .executeInTransaction(txn -> {
                    EntityIterable allPasswords = txn.getAll(Database.PASSWORDS_COLLECTION);
                    Entity existingPassword = findExistingPassword(((Password) objectToSearch), allPasswords);
                    persistPassword(oldAndNewPassword, passwordToStore, txn, existingPassword);
                });
        return Json.toJson(oldAndNewPassword).toString();
    }

    @Nullable
    private Entity findExistingPassword(String searchExpressionKey, String searchExpressionValue, EntityIterable allPasswords) {
        for (Entity password : allPasswords) {
            Comparable searchValue = password.getProperty(searchExpressionKey);
            if (searchExpressionValue.equals(searchValue)) {
                return password;
            }
        }
        return null;
    }

    private Entity findExistingPassword(Password passwordToSearch, EntityIterable allPasswords) {
        EntityIterator entityIterator = allPasswords.iterator();
        while (entityIterator.hasNext()) {
            Entity entity = entityIterator.next();
            Optional<Password> passwordOptional = db.passwordEntityToPassword(entity);
            if (passwordOptional.isPresent() && passwordToSearch.equals(passwordOptional.get())) {
                return entity;
            }
        }
        return null;
    }

    private void persistPassword(final Map<String, String> oldAndNewPassword, final Password passwordToStore, final StoreTransaction txn, final Entity existingPassword) {
        if (existingPassword == null) {
            final Entity newPassword = txn.newEntity(Database.PASSWORDS_COLLECTION);
            setPasswordEntity(newPassword, passwordToStore);
            oldAndNewPassword.put("old", Json.newObject().toString());
        } else {
            oldAndNewPassword.put("old", Json.toJson(db.passwordEntityToPassword(existingPassword)).toString());
            setPasswordEntity(existingPassword, passwordToStore);
        }
        oldAndNewPassword.put("new", Json.toJson(passwordToStore).toString());
    }

    private void setPasswordEntity(Entity passwordEntity, Password passwordToStore) {
        passwordEntity.setProperty("login", passwordToStore.getLogin());
        passwordEntity.setProperty("password", passwordToStore.getPassword());
        passwordEntity.setBlobString("json", Json.toJson(passwordToStore).toString());
    }
}
