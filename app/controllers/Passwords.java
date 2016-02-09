package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;
import org.apache.commons.lang3.StringUtils;
import persistence.dao.PasswordDao;
import persistence.entity.Password;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.content.passwords;

import java.io.IOException;
import java.util.List;

@SubjectPresent
public class Passwords extends Controller {

  @Inject PasswordDao passwordDao;

  public F.Promise<Result> index() throws IOException {
    return F.Promise.promise(() -> ok(passwords.render("All known passwords.")));
  }

  public F.Promise<Result> data() throws IOException {
    return F.Promise.promise(() -> {
      final ObjectNode objectNode = Json.newObject();
      try {
        final List<Password> allPasswords = passwordDao.getAll();
        objectNode.put("data", Json.toJson(allPasswords));
        objectNode.get("data").elements().forEachRemaining(jsonNode -> {
          final ObjectNode node = (ObjectNode) jsonNode;
          String url = node.get("webSite").asText();
          if ( StringUtils.isNotBlank(url) && !url.contains(" ") ) {

            String urlToDisplay = url;
            try {
              if ( !StringUtils.containsIgnoreCase(url, "://") ) {
                url = "http://" + url;
              }
              final URL parsedUrl = URL.parse(url);
              urlToDisplay = parsedUrl.host().toHumanString();
              node.replace("webSite",
                           Json.toJson("<a target='_blank' href='" + url + "'" + ">" + urlToDisplay + "</a>"));
            } catch (GalimatiasParseException e) {
              Logger.error("Could not parse {}", jsonNode);
            }
          }
        });
      } catch (Exception e) {
        Logger.error("bad happened", e);
      }
      return ok(objectNode);
    });
  }
}
