package com.wixpress.guineapig.web;

import com.wixpress.guineapig.entities.auth.User;
import com.wixpress.guineapig.entities.auth.UsersRole;
import com.wixpress.guineapig.topology.ClientTopology;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RequestMapping("/v1")
public abstract class BaseController {

    @Resource
    protected ClientTopology clientTopology;
    protected <T> GuineapigResult<T> success(T payload) {
        return new GuineapigResult<>(payload);
    }

    protected GuineapigResult failure(String message) {
        return new GuineapigResult(500, message);
    }

    private UsersRole getUserRole(HttpSession session) {
        if (session != null) {
            User user = getUser(session);
            if (user != null) {
                return user.getUserRole();
            }
        }
        return null;
    }

    User getUser(HttpSession session) {
        if (!clientTopology.isProduction()) {
            return User.stagingUser;
        }
        return (User) session.getAttribute("userSession");
    }

}
