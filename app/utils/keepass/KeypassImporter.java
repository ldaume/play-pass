package utils.keepass;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import persistence.entity.Password;
import play.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

/**
 * Created by Leonard Daume on 22.11.2015.
 */
public class KeypassImporter {

  public static List<Password> fromKeepassCSV(final File csvFile) {
    final List<Password> passwords = Lists.newArrayList();
    try {
      ObjectMapper mapper = new CsvMapper();
      final String csvString = FileUtils.readFileToString(csvFile);
      final MappingIterator<Map<String, String>> passWordIterator = mapper.readerFor(Map.class)
                                                                          .with(CsvSchema.emptySchema()
                                                                                         .withHeader()
                                                                                         .withEscapeChar('\\'))
                                                                          .readValues(csvString);
      passWordIterator.forEachRemaining(passwordMap -> {
        passwords.add(new Password(trimToEmpty(passwordMap.get("Account")),
                                   trimToEmpty(passwordMap.get("Login Name")),
                                   trimToEmpty(passwordMap.get("Password")),
                                   trimToEmpty(passwordMap.get("Web Site")),
                                   trimToEmpty(passwordMap.get("Comments"))));
      });
    } catch (Exception e) {
      Logger.error("Could not map keepass csv");
      throw new RuntimeException(e);
    }
    return passwords;
  }

  private static String stripQuotes(final List<String> entries, final int index) {
    String s = entries.get(index);
    s = StringUtils.removeStart(s, "\"");
    s = StringUtils.removeEnd(s, "\"");
    return s;
  }
}
