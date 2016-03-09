package utils;

import com.google.inject.Inject;

/**
 * Created by leonard on 09.03.16.
 */
public class StartInterceptor {

  @Inject
  public StartInterceptor(PlayUtils playUtils) {
    playUtils.logConfig();
  }
}
