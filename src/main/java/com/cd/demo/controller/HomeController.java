package com.cd.demo.controller;

import com.cd.demo.dao.User;
import com.cd.demo.form.ConsultaForm;
import com.cd.demo.form.UserForm;
import com.cd.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

import org.apache.cxf.*;

@Controller
public class HomeController {
    @Autowired
    private UserRepository userRepository;

    //String host_server = "localhost";
    String host_server = "192.168.56.101";
    Integer port_server = 12345;

    public HomeController() throws RemoteException {
    }

    @GetMapping("/")
    String home(ModelMap model, HttpSession session){
        model.addAttribute("session", session);
        return "pagina-teste";
    }

    @GetMapping("/clinica/{id}")
    String clinica(@PathVariable("id") String id, ModelMap model) throws RemoteException {

        try (Socket socket = new Socket(host_server, port_server);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            String comando = "GET_CLINICA " + id;
            output.println(comando);
            String clinica = input.readLine();

            comando = "GET_MEDICOS " + id;
            output.println(comando);
            String medicos = input.readLine();

            Map<String, String> medicos_map = new HashMap<>();

            String[] pares = medicos.split(";");

            for (String par : pares) {
                String[] partes = par.split(":");
                if (partes.length == 2) {
                    String idMedico = partes[0];
                    String especialidade = partes[1];
                    medicos_map.put(idMedico, especialidade);
                }
            }

            model.addAttribute("clinica_nome", clinica);
            model.addAttribute("medicos", medicos_map);

            return "pagina-clinica";

        } catch (Exception e) {
            System.out.println("Erro no cliente: " + e.getMessage());
            e.printStackTrace();
            return "pagina-NF";
        }
    }

    @GetMapping("/marcar/consulta")
    public String marcarConsulta(@RequestParam(value = "medicoId", required = false) Integer id_medico, @RequestParam(value = "clinicaId", required = false) String id_clinica, ModelMap model, HttpSession session) {
        ConsultaForm consultaForm = new ConsultaForm();

        if (id_clinica != null) {
            consultaForm.setClinica(id_clinica);
            model.addAttribute("clinicaEscolhida", id_clinica);
        }

        if (id_medico != null) {
            int id = id_medico;
            consultaForm.setMedico(id);
            model.addAttribute("medicoEscolhido", id);
        }

        model.addAttribute("consultaForm", consultaForm);
        return "marcar-consulta";
    }

    @PostMapping("/marcar/consulta")
    String registar(@Valid @ModelAttribute("consultaForm") ConsultaForm consultaForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "marcar-consulta";
        }

        String name = (String) session.getAttribute("name");
        if (name == null){
            redirectAttributes.addFlashAttribute("message", "Sem sessão iniciada, faça login antes de fazer uma marcação");
            return "redirect:/marcar/consulta";
        }

        Optional<User> user_valid = userRepository.findByNameAndEmail((String) session.getAttribute("name"),(String) session.getAttribute("email"));

        try (Socket socket = new Socket(host_server, port_server);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            String comando = "MARCA_CONSULTA " + consultaForm.clinica + " " + consultaForm.medico + " " + consultaForm.data + " " + user_valid.get().getId();
            output.println(comando);
            String resposta = input.readLine();

            if (resposta.equals("Sucesso")){
                return "redirect:/marcacoes";
            } else {
                System.out.println(resposta);
                return "pagina-NF";
            }

        } catch (Exception e) {
            System.out.println("Erro no cliente: " + e.getMessage());
            e.printStackTrace();
            return "pagina-NF";
        }
    }

    @GetMapping("/marcacoes")
    String marcacoes(ModelMap model, HttpSession session) {
        Optional<User> user_valid = userRepository.findByNameAndEmail((String) session.getAttribute("name"),(String) session.getAttribute("email"));

        if (user_valid.isEmpty()){
            model.addAttribute("userForm", new UserForm());
            return "pagina-login";
        }

        try (Socket socket = new Socket(host_server, port_server);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            String comando = "GET_CONSULTAS_BY_USER " + user_valid.get().getId();
            output.println(comando);
            String resposta = input.readLine();

            Map<String, List<Map<String, String>>> consultasMap = getStringListMap(resposta);
            model.addAttribute("consultasMap", consultasMap);
            return "marcacoes";
        } catch (Exception e) {
            System.out.println("Erro no cliente: " + e.getMessage());
            e.printStackTrace();
            return "pagina-NF";
        }

    }

    @GetMapping("/teste/rest")
    String testeRest(ModelMap model, HttpSession session) {
        try {
            URL url = new URL("http://localhost:8080/testrest/rest/getName");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            Scanner scanner;
            String response;

            if (conn.getResponseCode() != 200) {
                scanner = new Scanner(conn.getErrorStream());
                response = "Error From Server \n\n";
            } else {
                scanner = new Scanner(conn.getInputStream());
                response = "Response From Server \n\n";
            }

            scanner.useDelimiter("\\Z");
            System.out.println(response + scanner.next());
            scanner.close();
            conn.disconnect();

            return "";

        } catch (Exception e) {
            System.out.println("Erro no cliente: " + e.getMessage());
            e.printStackTrace();
            return "pagina-NF";
        }
    }

    @GetMapping("/teste/SOAP")
    String testeSOAP(ModelMap model, HttpSession session) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setAddress("");
        factory.setServiceClass(Utility.class);

        Object client = factory.create();

        try {

            int response = ((Utility) client).funcao(argumentos);

            // O QUE FAZ

        } catch (Exception e) {
            System.out.println("Erro no cliente: " + e.getMessage());
            e.printStackTrace();
            return "pagina-NF";
        }
    }

    // FUNCOES DE TRADUCAO
    private static Map<String, List<Map<String, String>>> getStringListMap(String resposta) {
        Map<String, List<Map<String, String>>> consultasMap = new HashMap<>();

        String[] clinicas = resposta.split("\\|");
        for (String clinicaInfo : clinicas) {
            if (clinicaInfo.isBlank()) continue;

            // Dividindo a informação da clínica
            String[] parts = clinicaInfo.split(";");

            if (parts.length < 2) continue; // Verifica se há ao menos a clínica e a especialidade

            String clinicaId = parts[0]; // Nome ou ID da clínica
            String especialidade = parts[1]; // Especialidade do médico

            // Lista para armazenar as consultas de cada clínica
            List<Map<String, String>> consultasClinica = consultasMap.computeIfAbsent(clinicaId, k -> new ArrayList<>());

            // Verifica e processa cada consulta, a partir do índice 2
            for (int i = 2; i < parts.length; i++) {
                String[] consultaDetails = parts[i].split(":");

                if (consultaDetails.length < 2) continue; // Verifica se há ao menos o ID e a data/hora

                String idMarcacao = consultaDetails[0];
                String dataHora = consultaDetails[1];

                // Adiciona os detalhes da consulta a um novo mapa
                Map<String, String> consultaDetailsMap = new HashMap<>();
                consultaDetailsMap.put("especialidade", especialidade);
                consultaDetailsMap.put("idMarcacao", idMarcacao);
                consultaDetailsMap.put("dataHora", dataHora);

                // Adiciona o mapa com os detalhes da consulta à lista de consultas da clínica
                consultasClinica.add(consultaDetailsMap);
            }
        }

        return consultasMap;
    }
}
