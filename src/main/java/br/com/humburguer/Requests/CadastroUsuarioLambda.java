package br.com.humburguer.Requests;

import br.com.humburguer.DAO.UsuarioDAO;
import br.com.humburguer.DTO.DadosUsuario;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class CadastroUsuarioLambda {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public APIGatewayProxyResponseEvent handleRequest(DadosUsuario input) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            String cpf = input.getCpf();
            String email = input.getEmail();
            String nome = input.getNome();

            usuarioDAO.salvarUsuarioNoDynamoDB(cpf, email, nome);
            response.setStatusCode(200);
            response.setBody("Usuário cadastrado com sucesso.");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("Erro ao cadastrar usuário: " + e.getMessage());
        }
        return response;
    }



}

