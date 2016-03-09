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
package persistence.entity;

import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Role;
import be.objectify.deadbolt.java.models.Subject;
import play.data.validation.Constraints;

import java.util.List;

/**
 * @author Leonard Daume
 */
public class AuthorisedUser implements Subject {

  @Constraints.Required(message = "Email is required.")
  @Constraints.Email(message = "Not a valid email address.")
  public String email;

  @Constraints.Required(message = "Password is required.")
  public String password;
  public List<SecurityRole> roles;
  public List<UserPermission> permissions;

  public AuthorisedUser(final String email,
                        final String password,
                        final List<SecurityRole> roles,
                        final List<UserPermission> permissions) {
    this.email = email;
    this.password = password;
    this.roles = roles;
    this.permissions = permissions;
  }

  public AuthorisedUser() {
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  @Override public List<? extends Role> getRoles() {
    return roles;
  }

  @Override public List<? extends Permission> getPermissions() {
    return permissions;
  }

  @Override public String getIdentifier() {
    return email;
  }

  public void setPermissions(final List<UserPermission> permissions) {
    this.permissions = permissions;
  }

  public void setRoles(final List<SecurityRole> roles) {
    this.roles = roles;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }
}
