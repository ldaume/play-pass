package utils.keepass;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import persistence.entity.Password;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Created by Leonard Daume on 22.11.2015.
 */
public class KeypassImporter {

  public static List<Password> fromKeepassCSV(final File csvFile) {
    final List<Password> parsedPasswords = Lists.newArrayList();

    // read csv
    final CsvParserSettings parserSettings = new CsvParserSettings();
    parserSettings.getFormat().setLineSeparator("\n");
    parserSettings.setNullValue("");
    parserSettings.setHeaderExtractionEnabled(true);
    final RowListProcessor rowProcessor = new RowListProcessor();
    parserSettings.setRowProcessor(rowProcessor);
    final CsvParser csvParser = new CsvParser(parserSettings);
    csvParser.parse(csvFile);
    final List<String> headers = Lists.newArrayList(rowProcessor.getHeaders());
    final List<String[]> rows = rowProcessor.getRows();

    // change structure
    final List<Map<String, String>> passwordRows = Lists.newArrayList();
    rows.stream().forEach(row -> {
      int i = 0;
      final Map<String, String> rowEntry = Maps.newConcurrentMap();
      for ( String entry : row ) {
        rowEntry.put(headers.get(i), defaultString(entry));
        i++;
      }
      passwordRows.add(rowEntry);
    });

    // map csv rows to object
    passwordRows.forEach(password -> parsedPasswords.add(passwordFromMap(password)));

    return parsedPasswords;
  }

  private static Password passwordFromMap(final Map<String, String> passwordMap) {
    return new Password(passwordMap.get("Account"),
                        passwordMap.get("Login Name"),
                        passwordMap.get("Password"),
                        passwordMap.get("Web Site"),
                        passwordMap.get("Comments"));
  }
}
