package persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.URL;
import play.data.validation.Constraints;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by Leonard Daume on 22.11.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Password {
    public String account;
    @Constraints.Required
    public String login;
    @Constraints.Required
    public String password;
    @URL
    public String webSite;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Password password1 = (Password) o;

        if (account != null ? !account.equals(password1.account) : password1.account != null) return false;
        if (!login.equals(password1.login)) return false;
        if (!password.equals(password1.password)) return false;
        if (webSite != null ? !webSite.equals(password1.webSite) : password1.webSite != null) return false;
        return comments != null ? comments.equals(password1.comments) : password1.comments == null;

    }

    @Override
    public int hashCode() {
        int result = account != null ? account.hashCode() : 0;
        result = 31 * result + login.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + (webSite != null ? webSite.hashCode() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        return result;
    }
}
