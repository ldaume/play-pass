package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.google.inject.Inject;
import exceptions.TypeMismatch;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import services.password.PasswordService;
import views.html.content.upload;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created by Leonard Daume on 23.11.2015.
 */
@SubjectPresent
public class Upload extends Controller {
  @Inject private PasswordService passwordService;
  @Inject private FormFactory formFactory;

  public CompletionStage<Result> index() {
    return CompletableFuture.supplyAsync(() -> ok(upload.render(formFactory.form().bindFromRequest())));
  }

  public CompletionStage<Result> doUpload() {
    final DynamicForm uploadForm = formFactory.form().bindFromRequest();
    final play.mvc.Http.MultipartFormData<File> body = request().body().asMultipartFormData();
    return CompletableFuture.supplyAsync(() -> {
      play.mvc.Http.MultipartFormData.FilePart<File> csv = body.getFile("csv");
      if ( csv == null ) {
        uploadForm.reject("Missing file");
        return badRequest(upload.render(uploadForm));
      }
      try {
        passwordService.importCsv(csv);
      } catch (TypeMismatch typeMismatch) {
        uploadForm.reject(typeMismatch.getMessage());
        return badRequest(upload.render(uploadForm));
      } catch (Exception e) {
        Logger.error("Could not import csv.", e);
        uploadForm.reject("Could not import csv.");
        return badRequest(upload.render(uploadForm));
      }
      return redirect(routes.Passwords.index());
    });
  }
}
