package controllers;

import com.google.inject.Inject;
import exceptions.AuthException;
import persistence.entity.AuthorisedUser;
import play.data.Form;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import services.security.AuthService;
import views.html.auth.login;

import java.io.IOException;
import java.util.UUID;

public class Authenticate extends Controller {
  @Inject private AuthService authService;

  public F.Promise<Result> authenticate() {
    return F.Promise.promise(() -> {
      Form<AuthorisedUser> loginForm = Form.form(AuthorisedUser.class).bindFromRequest();

      if ( loginForm.hasErrors() ) {
        return badRequest(login.render(loginForm));
      }
      try {
        final UUID sessionId = authService.authorise(loginForm.get());
        session().clear();
        session("id", sessionId.toString());
      } catch (AuthException e) {
        session().clear();
        loginForm.reject(e.getMessage());
        return badRequest(login.render(loginForm));
      } catch (Exception e) {
        session().clear();
        loginForm.reject("Could not authenticate.");
        return badRequest(login.render(loginForm));
      }
      return redirect(routes.Passwords.index());
    });
  }

  public F.Promise<Result> login() throws IOException {
    return F.Promise.promise(() -> ok(login.render(Form.form(AuthorisedUser.class))));
  }

  public F.Promise<Result> logout() {
    return F.Promise.promise(() -> {
      session().clear();
      return redirect(routes.Passwords.index());
    });
  }
}
