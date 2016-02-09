package controllers;

import be.objectify.deadbolt.core.models.Subject;
import com.google.inject.Inject;
import persistence.dao.UserDao;
import persistence.entity.AuthorisedUser;
import play.data.Form;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.auth.login;

import java.io.IOException;
import java.util.Optional;

public class Authenticate extends Controller {
  @Inject UserDao userDao;

  public F.Promise<Result> authenticate() {
    return F.Promise.promise(() -> {
      Form<AuthorisedUser> loginForm = Form.form(AuthorisedUser.class).bindFromRequest();

      if ( loginForm.hasErrors() ) {
        return badRequest(login.render(loginForm));
      }

      final Optional<Subject> byEmail = userDao.findByEmailAndPassword(loginForm.get().getEmail(),
                                                                       loginForm.get().getPassword());
      if ( byEmail.isPresent() ) {
        session().clear();
        session("email", loginForm.get().email);
        return redirect(routes.Passwords.index());
      }
      loginForm.reject("No user found");
      return badRequest(login.render(loginForm));
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
