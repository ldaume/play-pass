package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@SubjectPresent
public class Application extends Controller {

  public CompletionStage<Result> index() throws IOException {
    return CompletableFuture.supplyAsync(() -> redirect(routes.Passwords.index()));
  }
}
