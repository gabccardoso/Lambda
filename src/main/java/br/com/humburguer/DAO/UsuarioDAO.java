package br.com.humburguer.DAO;

import br.com.humburguer.DTO.DadosUsuario;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

public class UsuarioDAO {

    private static final String TABLE_NAME = "Usuarios";
    private final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
    private final DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);

    public DadosUsuario recuperaUsuarioPorCPF(String cpf){
        Table table = dynamoDB.getTable(TABLE_NAME);
        Item item = table.getItem("CPF", cpf);
        DadosUsuario dadosUsuario = new DadosUsuario(cpf, item.getString("Nome"), item.getString("Email"));
        return dadosUsuario;
    }

    public String verificarCPFERecuperaEmail(String cpf) {
        Table table = dynamoDB.getTable(TABLE_NAME);
        Item item = table.getItem("CPF", cpf);
        if(item != null) return item.getString("Email");
        return null;
    }

    public void salvarUsuarioNoDynamoDB(String cpf, String email, String nome) {

        AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
        Table table = dynamoDB.getTable("Usuarios");
        Item cpfExistente = table.getItem("CPF", cpf);
        if(cpfExistente != null) throw new IllegalArgumentException("CPF já cadastrado");
        Item item = new Item()
                .withPrimaryKey("CPF", cpf)
                .withString("Email", email)
                .withString("Nome", nome);
        table.putItem(item);
    }
}
