package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.google.inject.Inject;
import exceptions.TypeMismatch;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import services.password.PasswordService;
import views.html.content.upload;

/**
 * Created by Leonard Daume on 23.11.2015.
 */
@SubjectPresent
public class Upload extends Controller {
  @Inject private PasswordService passwordService;

  public F.Promise<Result> index() {
    return F.Promise.promise(() -> ok(upload.render(Form.form().bindFromRequest())));
  }

  public F.Promise<Result> doUpload() {
    return F.Promise.promise(() -> {
      DynamicForm uploadForm = Form.form().bindFromRequest();
      play.mvc.Http.MultipartFormData body = request().body().asMultipartFormData();
      play.mvc.Http.MultipartFormData.FilePart csv = body.getFile("csv");

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
