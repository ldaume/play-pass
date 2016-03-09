package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.arangodb.ArangoException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import org.apache.commons.collections4.CollectionUtils;
import persistence.entity.Password;
import play.data.Form;
import play.data.FormFactory;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@SubjectPresent
public class Passwords extends Controller {
  @Inject private PasswordService passwordService;

  @Inject private FormFactory formFactory;

  public CompletionStage<Result> index() throws IOException {
    return CompletableFuture.supplyAsync(() -> ok(passwords.render("All known passwords.")));
  }

  public CompletionStage<Result> data() throws IOException {
    return CompletableFuture.supplyAsync(() -> {
      final JsonNode allPasswords = passwordService.allPasswords();
      return ok(allPasswords);
    });
  }

  public CompletionStage<Result> addPasswordForm() throws IOException {
    return CompletableFuture.supplyAsync(() -> ok(addPassword.render(formFactory.form(Password.class))));
  }

  public CompletionStage<Result> add() {
    final Form<Password> passwordForm = formFactory.form(Password.class).bindFromRequest();
    return CompletableFuture.supplyAsync(() -> {
      try {
        if ( passwordForm.hasErrors() ) {
          return badRequest(addPassword.render(passwordForm));
        }
        final Password passwordFromForm = passwordForm.get();
        passwordService.insertOrUpdate(passwordFromForm);

        return redirect(routes.Passwords.index());
      } catch (ArangoException e) {
        throw new RuntimeException(e);
      }
    });
  }

  public CompletionStage<Result> edit() {
    final JsonNode jsonNode = request().body().asJson();
    return CompletableFuture.supplyAsync(() -> {
      try {
        final Password from = Json.mapper().treeToValue(jsonNode.get("from"), Password.class);
        final Password to = Json.mapper().treeToValue(jsonNode.get("to"), Password.class);
        final String change = passwordService.change(from, to);
        return ok(Json.toJson(change));
      } catch (JsonProcessingException | ArangoException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @BodyParser.Of(BodyParser.Json.class) public CompletionStage<Result> delete() {
    final Http.RequestBody body = request().body();
    return CompletableFuture.supplyAsync(() -> {
      try {
        final Password passwordToDelete = Json.mapper().treeToValue(body.asJson(), Password.class);

        List<Password> deletedPasswords = passwordService.delete(passwordToDelete);
        final ObjectNode result = Json.newObject();
        result.put("deleteCount", CollectionUtils.size(deletedPasswords));
        return ok(result);
      } catch (JsonProcessingException | ArangoException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
