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

import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.models.Subject;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.cache.CacheApi;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class DeadboltHandler extends AbstractDeadboltHandler {

  @Inject private CacheApi cacheApi;

  @Inject public DeadboltHandler(final ExecutionContextProvider ecProvider) {
    super(ecProvider);
  }

  public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.Context context) {
    return CompletableFuture.supplyAsync(Optional::empty);
  }

  public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context) {
    return CompletableFuture.supplyAsync(() -> {
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

  @Override public CompletionStage<Result> onAuthFailure(final Http.Context context, final String content) {
    return CompletableFuture.supplyAsync(() -> redirect("/play-pass/login"));
  }
}
