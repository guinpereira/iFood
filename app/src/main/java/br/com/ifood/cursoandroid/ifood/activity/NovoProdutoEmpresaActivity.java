package br.com.ifood.cursoandroid.ifood.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import br.com.ifood.cursoandroid.ifood.R;
import br.com.ifood.cursoandroid.ifood.helper.UsuarioFirebase;
import br.com.ifood.cursoandroid.ifood.model.Empresa;
import br.com.ifood.cursoandroid.ifood.model.Produto;

public class NovoProdutoEmpresaActivity extends AppCompatActivity {

    private EditText editProdutoNome, editProdutoDescricao,
            editProdutoPreco;
    private String idUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_produto_empresa);

        /*Configurações inicias*/
        inicializarComponentes();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //Configurações Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo produto");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void validarDadosProduto(View view){

        //Valida se os campos foram preenchidos
        String nome = editProdutoNome.getText().toString();
        String descricao = editProdutoDescricao.getText().toString();
        String preco = editProdutoPreco.getText().toString();

        if( !nome.isEmpty()){
            if( !descricao.isEmpty()){
                if( !preco.isEmpty()){

                    Produto produto = new Produto();
                    produto.setIdUsuario( idUsuarioLogado );
                    produto.setNome( nome );
                    produto.setDescricao( descricao );
                    produto.setPreco( Double.parseDouble(preco) );
                    produto.salvar();

                    finish();
                    exibirMensagem("Produto salvo com sucesso!");

                }else{
                    exibirMensagem("Digite um preço para o produto");
                }
            }else{
                exibirMensagem("Digite uma descrição para o produto");
            }
        }else{
            exibirMensagem("Digite um nome para o produto");
        }

    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT)
                .show();
    }

    private void inicializarComponentes(){
        editProdutoDescricao = findViewById(R.id.editProdutoDescricao);
        editProdutoNome = findViewById(R.id.editProdutoNome);
        editProdutoPreco = findViewById(R.id.editProdutoPreco);
    }

}
