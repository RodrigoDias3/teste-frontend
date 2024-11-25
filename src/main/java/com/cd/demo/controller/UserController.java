package com.cd.demo.controller;

import com.cd.demo.dao.User;
import com.cd.demo.form.SignUpForm;
import com.cd.demo.form.UserForm;
import com.cd.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        return userRepository.save(user);
    }

    @GetMapping("/login")
    public String login(ModelMap model, HttpSession session) {
        String nome = (String) session.getAttribute("nome");

        if (nome != null) {
            model.addAttribute("nome", nome );
            return "redirect:/";
        }

        model.addAttribute("userForm", new UserForm());
        return "pagina-login";
    }

    @PostMapping("/validar")
    String entrar(@Valid @ModelAttribute("userForm") UserForm userForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpSession session){
        if (bindingResult.hasErrors()) {
            return "pagina-login";
        }

        Optional<User> user = userRepository.findByNameAndEmail(userForm.nome, userForm.email);

        if (user.isEmpty()){
            redirectAttributes.addFlashAttribute("message", "Utilizador não encontrado");
            return "redirect:/login";
        }

        session.setAttribute("name", user.get().getName());
        session.setAttribute("email", user.get().getEmail());

        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signup(ModelMap model, HttpSession session) {
        String name = (String) session.getAttribute("name");

        if (name != null) {
            model.addAttribute("nomeCompleto", name);
            return "redirect:/";
        }

        model.addAttribute("signUpForm", new SignUpForm());
        return "pagina-signup";
    }

    @PostMapping("/signup")
    String registar(@Valid @ModelAttribute("signUpForm") SignUpForm signUpForm, BindingResult bindingResult, HttpSession session){
        if (bindingResult.hasErrors()) {
            return "pagina-signup";
        }

        Optional<User> user_valid = userRepository.findByNameAndEmail(signUpForm.nome, signUpForm.email);


        if (user_valid.isPresent()){
            bindingResult.rejectValue("primeiroNome", "error.signupForm", "Utilizador já existe com esse nome.");
            return "pagina-signup";
        }

        if (!signUpForm.getPassword().equals(signUpForm.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.signupForm", "As senhas não correspondem.");
        }

        User user = registerUser(signUpForm.getNome(), signUpForm.getEmail(), signUpForm.getPassword());

        session.setAttribute("name", user.getName());
        session.setAttribute("email", user.getEmail());
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();

        return "redirect:/login";
    }

}

