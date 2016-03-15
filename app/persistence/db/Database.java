package persistence.db;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.type.TypeFactory;
import persistence.entity.AuthorisedUser;
import play.Configuration;
import play.libs.Json;

import java.io.IOException;
import java.util.List;

/**
 * Created by Leonard Daume on 15.03.2016.
 */
public interface Database {

  String PASSWORDS_COLLECTION = "Passwords";
  String AUTHORIZED_USERS_COLLECTION = "AuthorisedUsers";

  default List<AuthorisedUser> getAuthorisedUsers() throws IOException {
    return Json.mapper()
               .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
               .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
               .readValue(getAuthorisedUsersJson(),
                          TypeFactory.defaultInstance().constructCollectionType(List.class, AuthorisedUser.class));
  }

  default String getAuthorisedUsersJson() {
    return getConfiguration().getString("authorised.users");
  }

  Configuration getConfiguration();
}
