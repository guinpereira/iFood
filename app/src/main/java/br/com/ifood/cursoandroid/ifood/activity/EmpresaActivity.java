package br.com.ifood.cursoandroid.ifood.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.ifood.cursoandroid.ifood.R;
import br.com.ifood.cursoandroid.ifood.adapter.AdapterProduto;
import br.com.ifood.cursoandroid.ifood.helper.ConfiguracaoFirebase;
import br.com.ifood.cursoandroid.ifood.listener.RecyclerItemClickListener;
import br.com.ifood.cursoandroid.ifood.model.Produto;

public class EmpresaActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private SearchBar searchBar;
    private SearchView searchView;
    private RecyclerView recyclerProdutos;
    private final List<Produto> produtos = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private AdapterProduto adapterProduto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empresa);

        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        // Configura Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Meus Produtos");
        setSupportActionBar(toolbar);

        // Configura SearchBar e SearchView
        if (searchBar != null && searchView != null) {
            searchBar.setHint("Buscar produtos");
            searchBar.setOnClickListener(view -> searchView.show());

            searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
                String textoBusca = v.getText().toString();
                searchView.hide();
                pesquisarProdutos(textoBusca);
                return true;
            });

            searchView.addTransitionListener((view, previousState, newState) -> {
                if (newState == SearchView.TransitionState.HIDDEN) {
                    searchBar.setText("");
                    recuperarProdutos(); // Volta à lista completa
                }
            });
        }

        // Configura RecyclerView
        recyclerProdutos.setLayoutManager(new LinearLayoutManager(this));
        recyclerProdutos.setHasFixedSize(true);
        adapterProduto = new AdapterProduto(produtos, this);
        recyclerProdutos.setAdapter(adapterProduto);

        // Recupera lista de produtos
        recuperarProdutos();

        // Clique nos produtos
        recyclerProdutos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerProdutos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Produto produtoSelecionado = produtos.get(position);
                                // Lógica para edição do produto, se necessário
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {}

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {}
                        }
                )
        );
    }

    private void inicializarComponentes() {
        searchBar = findViewById(R.id.search_bar);
        searchView = findViewById(R.id.search_view);
        recyclerProdutos = findViewById(R.id.recyclerProdutos);
    }

    private void pesquisarProdutos(String pesquisa) {
        DatabaseReference produtosRef = firebaseRef.child("produtos");
        Query query = produtosRef.orderByChild("nome")
                .startAt(pesquisa)
                .endAt(pesquisa + "\uf8ff");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                produtos.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    produtos.add(ds.getValue(Produto.class));
                }
                adapterProduto.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void recuperarProdutos() {
        DatabaseReference produtosRef = firebaseRef.child("produtos");
        produtosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                produtos.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    produtos.add(ds.getValue(Produto.class));
                }
                adapterProduto.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_empresa, menu); // menu_empresa.xml deve conter todos os itens corretos

        MenuItem itemPedido = menu.findItem(R.id.menuPedido);
        View actionView = itemPedido.getActionView();
        if (actionView != null) {
            actionView.setOnClickListener(v -> {
                // Aciona a ação como se o menu tivesse sido clicado
                onOptionsItemSelected(itemPedido);
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuSair) {
            deslogarUsuario();
            return true;
        } else if (id == R.id.menuConfiguracoes) {
            abrirConfiguracoes();
            return true;
        } else if (id == R.id.menuPedido) {
            abrirPedidos();
            return true;
        } else if (id == R.id.menuNovoProduto) {
            abrirNovoProduto();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deslogarUsuario() {
        try {
            autenticacao.signOut();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirNovoProduto() {
        startActivity(new Intent(this, NovoProdutoEmpresaActivity.class));
    }

    private void abrirPedidos() {
        startActivity(new Intent(this, PedidosActivity.class));
    }

    private void abrirConfiguracoes() {
        startActivity(new Intent(this, ConfiguracoesEmpresaActivity.class));
    }
}
