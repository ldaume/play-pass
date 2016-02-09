package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.google.common.collect.Lists;
import com.google.common.net.MediaType;
import com.google.inject.Inject;
import persistence.dao.PasswordDao;
import persistence.entity.Password;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import utils.keepass.KeypassImporter;
import views.html.content.upload;

import java.util.List;

/**
 * Created by Leonard Daume on 23.11.2015.
 */
@SubjectPresent
public class Upload extends Controller {

  @Inject PasswordDao passwordDao;

  public F.Promise<Result> index() {
    return F.Promise.promise(() -> ok(upload.render(Form.form().bindFromRequest())));
  }

  public F.Promise<Result> doUpload() {
    return F.Promise.promise(() -> {
      DynamicForm uploadForm = Form.form().bindFromRequest();
      play.mvc.Http.MultipartFormData body = request().body().asMultipartFormData();
      play.mvc.Http.MultipartFormData.FilePart csv = body.getFile("csv");

      if ( csv != null ) {
        String contentType = csv.getContentType();
        final MediaType mediaType = MediaType.parse(contentType);
        final List<MediaType> allowedTypes = Lists.newArrayList(MediaType.CSV_UTF_8,
                                                                MediaType.MICROSOFT_EXCEL,
                                                                MediaType.OPENDOCUMENT_SPREADSHEET,
                                                                MediaType.OOXML_SHEET);
        if ( !allowedTypes.contains(mediaType) ) {
          uploadForm.reject("Only the types "
                            + allowedTypes
                            + " are allowed.\nBut {"
                            + mediaType
                            + "} was uploaded"
                            + ".");
          return badRequest(upload.render(uploadForm));
        }
        List<Password> passwords = KeypassImporter.fromKeepassCSV(csv.getFile());
        passwordDao.saveOrUpdateAllCredentials(passwords);
        return redirect(routes.Passwords.index());
      }
      uploadForm.reject("Missing file");
      return badRequest(upload.render(uploadForm));
    });
  }
}
