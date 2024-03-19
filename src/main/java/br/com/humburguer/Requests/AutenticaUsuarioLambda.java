package br.com.humburguer.Requests;

import br.com.humburguer.DAO.UsuarioDAO;
import br.com.humburguer.DTO.DadosUsuario;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

public class AutenticaUsuarioLambda {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final String SECRET_KEY = "gab_123_humburguer_123";
    private static final String SECRET_KEY_SESSAO = "humburguer_2024";

    public APIGatewayProxyResponseEvent handleRequest(String token) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        //Se o token for válido, ele retorna o CPF do usuário
        String cpf = null;
        cpf = isValidToken(token);
        response.setStatusCode(200);
        response.setBody(cpf);
        try {
            cpf = isValidToken(token);
            if(cpf != null){
                DadosUsuario dadosUsuario = usuarioDAO.recuperaUsuarioPorCPF(cpf);
                String sessao = criaSessao(dadosUsuario);
                response.setStatusCode(200);
                response.setBody(sessao);
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody(e.getMessage());
        }

        return response;
    }

    private String isValidToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
            if (claims.getExpiration().before(new Date())) throw new IllegalArgumentException("Token inválido");
            return claims.getSubject();
        } catch (Exception e) {
            return "Erro na validação do token: " + e.getMessage();
        }
    }

    private String criaSessao(DadosUsuario dadosUsuario){
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long agora = System.currentTimeMillis();
        Date expiracao = new Date(agora + 3600 * 1000);
        Key key = new SecretKeySpec(SECRET_KEY_SESSAO.getBytes(), signatureAlgorithm.getJcaName());
        Claims claims = Jwts.claims().setSubject(dadosUsuario.getCpf());
        claims.put("email", dadosUsuario.getEmail());
        claims.put("nome", dadosUsuario.getNome());

        return Jwts.builder().setId(dadosUsuario.getCpf())
                .setIssuedAt(new Date())
                .setClaims(claims)
                .setExpiration(expiracao)
                .signWith(signatureAlgorithm, key).compact();
    }
}


