package persistence.entity;

import org.hibernate.validator.constraints.URL;
import play.data.validation.Constraints;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by Leonard Daume on 22.11.2015.
 */
public class Password {
  //"Account"
  public String account;
  // "Authenticate Name"
  @Constraints.Required
  public String login;
  // "Password"
  @Constraints.Required
  public String password;
  // "Web Site"
  @URL
  public String webSite;
  // "Comments"
  public String comments;

  public Password() {
  }

  public Password(final String account,
                  final String login,
                  final String password,
                  final String webSite,
                  final String comments) {
    this.account = account;
    this.login = checkNotNull(login);
    this.password = checkNotNull(password);
    this.webSite = webSite;
    this.comments = comments;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(final String account) {
    this.account = account;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(final String login) {
    this.login = checkNotNull(login);
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = checkNotNull(password);
  }

  public String getWebSite() {
    return webSite;
  }

  public void setWebSite(final String webSite) {
    this.webSite = webSite;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(final String comments) {
    this.comments = comments;
  }

  public boolean isWebPassword() {
    return isNotBlank(login) && isNotBlank(password) && isNotBlank(webSite);
  }
}
