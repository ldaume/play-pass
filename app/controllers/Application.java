package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;

@SubjectPresent
public class Application extends Controller {

  public F.Promise<Result> index() throws IOException {
    return F.Promise.promise(() -> redirect(routes.Passwords.index()));
  }
}
