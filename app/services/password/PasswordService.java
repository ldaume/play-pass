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
import play.Logger;
import play.libs.Json;
import play.mvc.Http.MultipartFormData.FilePart;
import utils.keepass.KeypassImporter;

import java.io.File;
import java.util.List;

/**
 * Created by leonard on 16.02.16.
 */
public class PasswordService {
    @Inject
    private PasswordDao passwordDao;

    public JsonNode allPasswords() {
        final ObjectNode objectNode = Json.newObject();
        final List<Password> allPasswords = passwordDao.getAll();
        objectNode.set("data", Json.toJson(allPasswords));
        return objectNode;
    }

    public void insertOrUpdate(final Password password) throws ArangoException {
        passwordDao.upsert(password);
    }

    public void importCsv(final FilePart csv) throws TypeMismatch {
        final String contentType = csv.getContentType();
        final MediaType mediaType = MediaType.parse(contentType);
        final List<MediaType> allowedTypes = Lists.newArrayList(MediaType.CSV_UTF_8,
                MediaType.MICROSOFT_EXCEL,
                MediaType.OPENDOCUMENT_SPREADSHEET,
                MediaType.OOXML_SHEET);
        if (!allowedTypes.contains(mediaType) && !allowedTypes.contains(mediaType.withCharset(Charsets.UTF_8))) {
            throw new TypeMismatch("Only the types "
                    + allowedTypes
                    + " are allowed.\nBut {"
                    + mediaType
                    + "} was uploaded"
                    + ".");
        }

        try {
            List<Password> passwords = KeypassImporter.fromKeepassCSV((File) csv.getFile());
            passwordDao.saveOrUpdateAllCredentials(passwords);
        } catch (Exception e) {
            Logger.error("Could not import passwords.", e);
            throw e;
        }
    }

    public List<Password> delete(final Password password) throws ArangoException {
        try {
            return passwordDao.delete(password);
        } catch (Exception e) {
            Logger.error("Could not delete password.", e);
            throw e;
        }
    }

    public String change(final Password from, final Password to) throws ArangoException {
        try {
            return passwordDao.upsert(from, to);
        } catch (Exception e) {
            Logger.error("Could not change password.", e);
            throw e;
        }
    }
}
