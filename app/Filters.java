import com.google.inject.Inject;
import play.filters.csrf.CSRFFilter;
import play.filters.gzip.GzipFilter;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

/**
 * Created by Leonard Daume on 09.02.2016.
 */
public class Filters implements HttpFilters {

  @Inject GzipFilter gzipFilter;
  @Inject CSRFFilter csrfFilter;

  public EssentialFilter[] filters() {
    return new EssentialFilter[] { gzipFilter.asJava() };
  }
}
