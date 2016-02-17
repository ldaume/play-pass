/*
 * Copyright 2012 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package security;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.cache.CacheApi;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;

public class DeadboltHandler extends AbstractDeadboltHandler {

  @Inject private CacheApi cacheApi;

  public F.Promise<Optional<Result>> beforeAuthCheck(final Http.Context context) {
    return F.Promise.promise(Optional::empty);
  }

  public F.Promise<Optional<Subject>> getSubject(final Http.Context context) {
    return F.Promise.promise(() -> {
      final Http.Session session = context.session();
      final String id = session.get("id");
      if ( StringUtils.isBlank(id) ) {
        return Optional.empty();
      }
      final Optional<Subject> subject = Optional.ofNullable(cacheApi.get(id));
      if ( !subject.isPresent() ) {
        session.clear();
      }
      return subject;
    });
  }

  @Override public F.Promise<Result> onAuthFailure(final Http.Context context, final String content) {
    return F.Promise.promise(() -> redirect("/play-pass/login"));
  }
}
