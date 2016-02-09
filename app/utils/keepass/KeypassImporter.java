package utils.keepass;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import persistence.entity.Password;
import play.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Leonard Daume on 22.11.2015.
 */
public class KeypassImporter {

  public static List<Password> fromKeepassCSV(final File csvFile) {
    final List<Password> passwords = Lists.newArrayList();
    try {
      passwords.addAll(FileUtils.readLines(csvFile).stream().skip(1).map(line -> {
        List<String> entries = Splitter.on(",").trimResults().splitToList(line);
        return new Password(stripQuotes(entries, 0),
                            stripQuotes(entries, 1),
                            stripQuotes(entries, 2),
                            stripQuotes(entries, 3),
                            stripQuotes(entries, 4));
      }).collect(Collectors.toList()));
    } catch (IOException e) {
      Logger.error("Could not map keepass csv");
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
