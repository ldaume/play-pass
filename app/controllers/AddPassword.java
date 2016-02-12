package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.google.inject.Inject;
import persistence.dao.PasswordDao;
import persistence.entity.Password;
import play.data.Form;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.content.addPassword;

import java.io.IOException;

@SubjectPresent
public class AddPassword extends Controller {
  @Inject PasswordDao passwordDao;

  public F.Promise<Result> index() throws IOException {
    return F.Promise.promise(() -> ok(addPassword.render(Form.form(Password.class))));
  }

  public F.Promise<Result> add() {
    return F.Promise.promise(() -> {
      Form<Password> passwordForm = Form.form(Password.class).bindFromRequest();

      if ( passwordForm.hasErrors() ) {
        return badRequest(addPassword.render(passwordForm));
      }
      final Password password = new Password(passwordForm.get().getAccount(),
                                             passwordForm.get().getLogin(),
                                             passwordForm.get().getPassword(),
                                             passwordForm.get().getWebSite(),
                                             passwordForm.get().getComments());
      passwordDao.upsert(password);

      return redirect(routes.Passwords.index());
    });
  }
}
