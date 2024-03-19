package br.com.humburguer;

import br.com.humburguer.DTO.DadosUsuario;
import br.com.humburguer.Enum.Fluxo;
import br.com.humburguer.Requests.AutenticaUsuarioLambda;
import br.com.humburguer.Requests.CadastroUsuarioLambda;
import br.com.humburguer.Requests.VerificaUsuarioLambda;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class LambdaMain implements RequestHandler<APIGatewayV2HTTPEvent, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CadastroUsuarioLambda cadastroUsuarioLambda = new CadastroUsuarioLambda();
    private final VerificaUsuarioLambda verificaUsuarioLambda = new VerificaUsuarioLambda();
    private final AutenticaUsuarioLambda autenticaUsuarioLambda = new AutenticaUsuarioLambda();

    @Override
    public String handleRequest(APIGatewayV2HTTPEvent input, Context context) {
        LambdaLogger logger = context.getLogger();
        String jsonBody = input.getBody();
        Map<String, String> queryParams = input.getQueryStringParameters();

        if (queryParams == null || queryParams.isEmpty() || !queryParams.containsKey("fluxo")) {
            return responseError(400, "O parâmetro 'fluxo' é obrigatório na URL.").getBody();
        }

        String fluxoValue = queryParams.get("fluxo");

        try {
            Fluxo fluxo = Fluxo.valueOf(fluxoValue);
            DadosUsuario dadosUsuario = null;
            if(jsonBody != null){
                dadosUsuario = objectMapper.readValue(jsonBody, DadosUsuario.class);
            }
            if (fluxo.equals(Fluxo.CADASTRO)) {
                return criar(dadosUsuario).getBody();
            } else if (fluxo.equals(Fluxo.LOGIN)) {
                return enviarEmail(dadosUsuario).getBody();
            } else if (fluxo.equals(Fluxo.AUTENTICACAO)) {
                String token = queryParams.get("token");
                return autenticar(token).getBody();
            } else {
                return responseError(400, "Fluxo inválido.").getBody();
            }
        } catch (IllegalArgumentException e) {
            return responseError(400, "Fluxo inválido: " + fluxoValue).getBody();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private APIGatewayProxyResponseEvent criar(DadosUsuario input) {
        return cadastroUsuarioLambda.handleRequest(input);
    }

    private APIGatewayProxyResponseEvent enviarEmail(DadosUsuario input) {
        return verificaUsuarioLambda.handleRequest(input);
    }

    private APIGatewayProxyResponseEvent autenticar(String token) {
        return autenticaUsuarioLambda.handleRequest(token);
    }

    private APIGatewayProxyResponseEvent responseError(int statusCode, String errorMessage) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(errorMessage);
        return response;
    }
}
