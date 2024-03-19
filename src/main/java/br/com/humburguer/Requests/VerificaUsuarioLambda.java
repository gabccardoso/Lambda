package br.com.humburguer.Requests;


import br.com.humburguer.DAO.UsuarioDAO;
import br.com.humburguer.DTO.DadosUsuario;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

public class VerificaUsuarioLambda {

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    private static final String SECRET_KEY = "gab_123_humburguer_123";
    private static final String API_AUTENTICACAO = "https://8h3cpp48j6.execute-api.us-east-1.amazonaws.com/default/auth?fluxo=AUTENTICACAO&token=";

    public APIGatewayProxyResponseEvent handleRequest(DadosUsuario input) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        String cpf = input.getCpf();
        String email = usuarioDAO.verificarCPFERecuperaEmail(cpf);
        boolean cpfExiste = email != null;

        if (cpfExiste) {
            String token = gerarTokenJWT(cpf);
            String corpoEmail = "Clique no link a seguir para se autenticar: " + API_AUTENTICACAO + token;
            try {
                this.sendEmail(email, corpoEmail);
            } catch (UnirestException e) {
                throw new RuntimeException(e);
            }
            response.setStatusCode(200);
            response.setBody(token);
        } else {
            response.setStatusCode(404);
            response.setBody("CPF não encontrado.");
        }

        return response;
    }

    private String gerarTokenJWT(String cpf) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long agora = System.currentTimeMillis();
        Date expiracao = new Date(agora + 3600 * 1000);
        Key key = new SecretKeySpec(SECRET_KEY.getBytes(), signatureAlgorithm.getJcaName());


        return Jwts.builder().setId(cpf)
                .setIssuedAt(new Date())
                .setExpiration(expiracao)
                .setSubject(cpf)
                .signWith(signatureAlgorithm, key).compact();
    }

    private void sendEmail(String recipientEmail, String emailContent) throws UnirestException {
        String DOMAIN = "sandboxd05e4829cf85493da9cb6601570e3805.mailgun.org";
        String API_KEY = "09d285950d198ecc3d458ff78fdee365-b02bcf9f-0eb04575";
        HttpResponse<JsonNode> request = Unirest.post("https://api.mailgun.net/v3/" + DOMAIN + "/messages")
                .basicAuth("Authorization", API_KEY)
                .queryString("from", "Hummburguer <USER@YOURDOMAIN.COM>")
                .queryString("to", recipientEmail)
                .queryString("subject", "Link para acesso")
                .queryString("text", emailContent)
                .asJson();
        System.out.printf(String.valueOf(request.getBody()));
    }
}


