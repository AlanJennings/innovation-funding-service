package com.worth.ifs.login;

import com.worth.ifs.commons.security.UserAuthenticationService;
import com.worth.ifs.user.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * This controller handles user login, logout and authentication / authorization.
 * It will also redirect the user after the login/logout is successful.
 */
@Controller
@Configuration
public class LoginController {
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    UserAuthenticationService userAuthenticationService;


    @RequestMapping(value="/login", method=RequestMethod.GET)
     public String login( Model model, HttpServletRequest request) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }
    @RequestMapping(value="/login", params={"logout"})
    public String logout(HttpServletResponse response) {
        userAuthenticationService.removeAuthentication(response);
        return "redirect:/login";
    }

    @RequestMapping(value="/login", method=RequestMethod.POST)
    public String loginSubmit(@Valid @ModelAttribute LoginForm loginForm, BindingResult bindingResult, HttpServletResponse response) {
        String destination = "";
        if(bindingResult.hasErrors()){
            destination = "login";
        }else{
            try {
                User authenticatedUser = userAuthenticationService.authenticate(loginForm.getEmail(), loginForm.getPassword());
                userAuthenticationService.addAuthentication(response, authenticatedUser);
                destination = redirectionForUser(authenticatedUser);

            } catch(BadCredentialsException bce) {
                bindingResult.rejectValue("email", "Your username/password combination doesn't seem to work", "Your username/password combination doesn't seem to work");
                bindingResult.rejectValue("password", "Your username/password combination doesn't seem to work", "Your username/password combination doesn't seem to work");
                destination = "login";
            }
        }

        return destination;
    }

    private String redirectionForUser(User user) {
        String roleName = "";
        if(user.getRoles().size() > 0) {
            roleName = user.getRoles().get(0).getName();
        }
        return "redirect:/" + roleName + "/dashboard";
    }

    private String redirectionForUknownUser() {
        log.info("No user found");
        return "redirect:/login?invalid";
    }
}

