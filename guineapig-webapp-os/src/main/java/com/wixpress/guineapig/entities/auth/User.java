package com.wixpress.guineapig.entities.auth;

import com.google.api.services.oauth2.model.Userinfoplus;

public class User {
    final static public User stagingUser = new User(0, "", new Userinfoplus(), UsersRole.USER);
    private final Integer id;
    private final String email;
    private final Userinfoplus userinfoplus;
    private final UsersRole userRole;

    public User(Integer id, String email, Userinfoplus userinfoplus, UsersRole userRole) {
        this.id = id;
        this.email = email;
        this.userinfoplus = userinfoplus;
        this.userRole = userRole;
    }

    public Integer getId() {
        return id;
    }


    public String getEmail() {
        return email;
    }

    public Userinfoplus getUserinfoplus() {
        return userinfoplus;
    }

    public UsersRole getUserRole() {
        return userRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        if (id != null ? !id.equals(user.id) : user.id != null) return false;
        if (userRole != user.userRole) return false;
        if (userinfoplus != null ? !userinfoplus.equals(user.userinfoplus) : user.userinfoplus != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (userinfoplus != null ? userinfoplus.hashCode() : 0);
        result = 31 * result + (userRole != null ? userRole.hashCode() : 0);
        return result;
    }


    public static class UserBuilder {
        private Integer id;
        private String email;
        private Userinfoplus userinfoplus;
        private UsersRole userRole;

        public UserBuilder withId(Integer id) {
            this.id = id;
            return this;
        }


        public UserBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder withUserinfoplus(Userinfoplus userinfoplus) {
            this.userinfoplus = userinfoplus;
            return this;
        }

        public UserBuilder withUserRole(UsersRole userRole) {
            this.userRole = userRole;
            return this;
        }

        public User build() {
            return new User(id, email, userinfoplus, userRole);
        }
    }
}
