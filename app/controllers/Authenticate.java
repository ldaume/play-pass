package controllers;

import com.google.inject.Inject;
import exceptions.AuthException;
import persistence.entity.AuthorisedUser;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.security.AuthService;
import views.html.auth.login;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Authenticate extends Controller {
  @Inject private AuthService authService;
  @Inject private FormFactory formFactory;

  public CompletionStage<Result> authenticate() {
    final Form<AuthorisedUser> loginForm = formFactory.form(AuthorisedUser.class).bindFromRequest();
    final Http.Session session = session();
    return CompletableFuture.supplyAsync(() -> {
      if ( loginForm.hasErrors() ) {
        return badRequest(login.render(loginForm));
      }
      try {
        final UUID sessionId = authService.authorise(loginForm.get());
        session.clear();
        session.put("id", sessionId.toString());
      } catch (AuthException e) {
        session.clear();
        loginForm.reject(e.getMessage());
        return badRequest(login.render(loginForm));
      } catch (Exception e) {
        session.clear();
        loginForm.reject("Could not authenticate.");
        return badRequest(login.render(loginForm));
      }
      return redirect(routes.Passwords.index());
    });
  }

  public CompletionStage<Result> login() throws IOException {
    return CompletableFuture.supplyAsync(() -> ok(login.render(formFactory.form(AuthorisedUser.class))));
  }

  public CompletionStage<Result> logout() {
    final Http.Session session = session();
    return CompletableFuture.supplyAsync(() -> {
      session.clear();
      return redirect(routes.Passwords.index());
    });
  }
}
