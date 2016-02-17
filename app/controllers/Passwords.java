package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import persistence.entity.Password;
import play.Logger;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.password.PasswordService;
import views.html.content.addPassword;
import views.html.content.passwords;

import java.io.IOException;
import java.util.List;

@SubjectPresent
public class Passwords extends Controller {
  @Inject private PasswordService passwordService;

  public F.Promise<Result> index() throws IOException {
    return F.Promise.promise(() -> ok(passwords.render("All known passwords.")));
  }

  public F.Promise<Result> data() throws IOException {
    return F.Promise.promise(() -> {
      final JsonNode allPasswords = passwordService.allPasswords();
      return ok(allPasswords);
    });
  }

  public F.Promise<Result> addPasswordForm() throws IOException {
    return F.Promise.promise(() -> ok(addPassword.render(Form.form(Password.class))));
  }

  public F.Promise<Result> add() {
    return F.Promise.promise(() -> {
      Form<Password> passwordForm = Form.form(Password.class).bindFromRequest();

      if ( passwordForm.hasErrors() ) {
        return badRequest(addPassword.render(passwordForm));
      }
      final Password passwordFromForm = passwordForm.get();
      passwordService.insertOrUpdate(passwordFromForm);

      return redirect(routes.Passwords.index());
    });
  }

  public F.Promise<Result> edit() {
    return F.Promise.promise(() -> {
      Form<Password> passwordForm = Form.form(Password.class).bindFromRequest();
      return ok(addPassword.render(passwordForm));
    });
  }

  @BodyParser.Of(BodyParser.Json.class) public F.Promise<Result> delete() {
    return F.Promise.promise(() -> {
      final Http.RequestBody body = request().body();
      final Password passwordToDelete = Json.mapper().treeToValue(body.asJson(), Password.class);

      List<Password> deletedPasswords = passwordService.delete(passwordToDelete);
      final ObjectNode result = Json.newObject();
      result.put("deleteCount", CollectionUtils.size(deletedPasswords));
      return ok(result);
    });
  }
}
