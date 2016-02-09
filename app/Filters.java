import com.google.inject.Inject;
import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;
import play.http.HttpFilters;

/**
 * Created by Leonard Daume on 09.02.2016.
 */
public class Filters implements HttpFilters {

  @Inject GzipFilter gzipFilter;

  public EssentialFilter[] filters() {
    return new EssentialFilter[] { gzipFilter };
  }
}
