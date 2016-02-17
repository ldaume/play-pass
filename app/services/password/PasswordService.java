package services.password;

import com.arangodb.ArangoException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.net.MediaType;
import com.google.inject.Inject;
import exceptions.TypeMismatch;
import persistence.dao.PasswordDao;
import persistence.entity.Password;
import play.libs.Json;
import play.mvc.Http;
import utils.keepass.KeypassImporter;

import java.util.List;

/**
 * Created by leonard on 16.02.16.
 */
public class PasswordService {
  @Inject private PasswordDao passwordDao;

  public JsonNode allPasswords() {
    final ObjectNode objectNode = Json.newObject();
    final List<Password> allPasswords = passwordDao.getAll();
    objectNode.put("data", Json.toJson(allPasswords));
    return objectNode;
  }

  public void insertOrUpdate(final Password password) throws com.arangodb.ArangoException {
    passwordDao.upsert(password);
  }

  public void importCsv(final Http.MultipartFormData.FilePart csv) throws TypeMismatch {
    final String contentType = csv.getContentType();
    final MediaType mediaType = MediaType.parse(contentType);
    final List<MediaType> allowedTypes = Lists.newArrayList(MediaType.CSV_UTF_8,
                                                            MediaType.MICROSOFT_EXCEL,
                                                            MediaType.OPENDOCUMENT_SPREADSHEET,
                                                            MediaType.OOXML_SHEET);
    if ( !allowedTypes.contains(mediaType) && !allowedTypes.contains(mediaType.withCharset(Charsets.UTF_8)) ) {
      throw new TypeMismatch("Only the types "
                             + allowedTypes
                             + " are allowed.\nBut {"
                             + mediaType
                             + "} was uploaded"
                             + ".");
    }
    List<Password> passwords = KeypassImporter.fromKeepassCSV(csv.getFile());
    passwordDao.saveOrUpdateAllCredentials(passwords);
  }

  public List<Password> delete(final Password password) throws ArangoException {
    return passwordDao.delete(password);
  }
}
