package br.com.ifood.cursoandroid.ifood.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

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
import br.com.ifood.cursoandroid.ifood.adapter.AdapterEmpresa;
import br.com.ifood.cursoandroid.ifood.helper.ConfiguracaoFirebase;
import br.com.ifood.cursoandroid.ifood.listener.RecyclerItemClickListener;
import br.com.ifood.cursoandroid.ifood.model.Empresa;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private SearchBar searchBar;
    private SearchView searchView;
    private RecyclerView recyclerEmpresa;
    private List<Empresa> empresas = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private AdapterEmpresa adapterEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        // Configura Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configura SearchBar com SearchView (Material 3)
        if (searchView != null) {
            searchBar.setHint("Buscar restaurantes");
            searchBar.setOnClickListener(view -> searchView.show());

            searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
                String textoBusca = v.getText().toString();
                searchView.hide();
                pesquisarEmpresas(textoBusca);
                return true;
            });

            searchView.addTransitionListener((searchView1, previousState, newState) -> {
                if (newState == SearchView.TransitionState.HIDDEN) {
                    searchBar.setText("");
                    recuperarEmpresas();
                }
            });
        }

        // Configura RecyclerView
        recyclerEmpresa.setLayoutManager(new LinearLayoutManager(this));
        recyclerEmpresa.setHasFixedSize(true);
        adapterEmpresa = new AdapterEmpresa(empresas);
        recyclerEmpresa.setAdapter(adapterEmpresa);

        // Recupera lista de empresas
        recuperarEmpresas();

        // Clique no RecyclerView
        recyclerEmpresa.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerEmpresa,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Empresa empresaSelecionada = empresas.get(position);
                                Intent i = new Intent(HomeActivity.this, CardapioActivity.class);
                                i.putExtra("empresa", empresaSelecionada);
                                startActivity(i);
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
        recyclerEmpresa = findViewById(R.id.recyclerEmpresa);
    }

    private void pesquisarEmpresas(String pesquisa) {
        DatabaseReference empresasRef = firebaseRef.child("empresas");
        Query query = empresasRef.orderByChild("nome")
                .startAt(pesquisa)
                .endAt(pesquisa + "\uf8ff");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                empresas.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    empresas.add(ds.getValue(Empresa.class));
                }
                adapterEmpresa.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void recuperarEmpresas() {
        DatabaseReference empresaRef = firebaseRef.child("empresas");
        empresaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                empresas.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    empresas.add(ds.getValue(Empresa.class));
                }
                adapterEmpresa.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_usuario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuSair) {
            deslogarUsuario();
        } else if (item.getItemId() == R.id.menuConfiguracoes) {
            abrirConfiguracoes();
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

    private void abrirConfiguracoes() {
        startActivity(new Intent(HomeActivity.this, ConfiguracoesUsuarioActivity.class));
    }
}
