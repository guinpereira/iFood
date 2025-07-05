package br.com.ifood.cursoandroid.ifood.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import br.com.ifood.cursoandroid.ifood.R;
import br.com.ifood.cursoandroid.ifood.adapter.AdapterProduto;
import br.com.ifood.cursoandroid.ifood.helper.ConfiguracaoFirebase;
import br.com.ifood.cursoandroid.ifood.helper.UsuarioFirebase;
import br.com.ifood.cursoandroid.ifood.listener.RecyclerItemClickListener;
import br.com.ifood.cursoandroid.ifood.model.Empresa;
import br.com.ifood.cursoandroid.ifood.model.ItemPedido;
import br.com.ifood.cursoandroid.ifood.model.Pedido;
import br.com.ifood.cursoandroid.ifood.model.Produto;
import br.com.ifood.cursoandroid.ifood.model.Usuario;
import dmax.dialog.SpotsDialog;

public class CardapioActivity extends AppCompatActivity {

    private RecyclerView recyclerProdutosCardapio;
    private ImageView imageEmpresaCardapio;
    private TextView textNomeEmpresaCardapio;
    private Empresa empresaSelecionada;
    private AlertDialog dialog;
    private TextView textCarrinhoQtd, textCarrinhoTotal;

    private AdapterProduto adapterProduto;
    private List<Produto> produtos = new ArrayList<>();
    private List<ItemPedido> itensCarrinho = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private String idEmpresa;
    private String idUsuarioLogado;
    private Usuario usuario;
    private Pedido pedidoRecuperado;
    private int qtdItensCarrinho;
    private Double totalCarrinho;
    private int metodoPagamento;

    // Badge
    private View actionView;
    private TextView textBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);

        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            empresaSelecionada = (Empresa) bundle.getSerializable("empresa");
            textNomeEmpresaCardapio.setText(empresaSelecionada.getNome());
            idEmpresa = empresaSelecionada.getIdUsuario();
            String url = empresaSelecionada.getUrlImagem();
            Picasso.get().load(url).into(imageEmpresaCardapio);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Cardápio");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerProdutosCardapio.setLayoutManager(new LinearLayoutManager(this));
        recyclerProdutosCardapio.setHasFixedSize(true);
        adapterProduto = new AdapterProduto(produtos, this);
        recyclerProdutosCardapio.setAdapter(adapterProduto);

        recyclerProdutosCardapio.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerProdutosCardapio,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                confirmarQuantidade(position);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {}

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {}
                        }
                )
        );

        recuperarProdutos();
        recuperarDadosUsuario();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cardapio, menu);

        MenuItem item = menu.findItem(R.id.menuPedido);
        actionView = item.getActionView();

        if (actionView != null) {
            textBadge = actionView.findViewById(R.id.text_badge);
            ImageView icone = actionView.findViewById(R.id.icone_menu);

            atualizarBadge();

            actionView.setOnClickListener(v -> onOptionsItemSelected(item));
        }

        return true;
    }

    private void atualizarBadge() {
        if (textBadge == null) return;
        if (qtdItensCarrinho > 0) {
            textBadge.setText(String.valueOf(qtdItensCarrinho));
            textBadge.setVisibility(View.VISIBLE);
        } else {
            textBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuPedido) {
            confirmarPedido();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmarQuantidade(final int posicao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quantidade");
        builder.setMessage("Digite a quantidade");

        final EditText editQuantidade = new EditText(this);
        editQuantidade.setText("1");
        builder.setView(editQuantidade);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String quantidade = editQuantidade.getText().toString();
            Produto produtoSelecionado = produtos.get(posicao);
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setIdProduto(produtoSelecionado.getIdProduto());
            itemPedido.setNomeProduto(produtoSelecionado.getNome());
            itemPedido.setPreco(produtoSelecionado.getPreco());
            itemPedido.setQuantidade(Integer.parseInt(quantidade));
            itensCarrinho.add(itemPedido);

            if (pedidoRecuperado == null) {
                pedidoRecuperado = new Pedido(idUsuarioLogado, idEmpresa);
            }

            pedidoRecuperado.setNome(usuario.getNome());
            pedidoRecuperado.setEndereco(usuario.getEndereco());
            pedidoRecuperado.setItens(itensCarrinho);
            pedidoRecuperado.salvar();

            qtdItensCarrinho += itemPedido.getQuantidade();
            totalCarrinho += itemPedido.getQuantidade() * itemPedido.getPreco();
            atualizarBadge();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void confirmarPedido() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione um método de pagamento");

        CharSequence[] itens = new CharSequence[]{"Dinheiro", "Máquina cartão"};
        builder.setSingleChoiceItems(itens, 0, (dialog, which) -> metodoPagamento = which);

        final EditText editObservacao = new EditText(this);
        editObservacao.setHint("Digite uma observação");
        builder.setView(editObservacao);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String observacao = editObservacao.getText().toString();
            pedidoRecuperado.setMetodoPagamento(metodoPagamento);
            pedidoRecuperado.setObservacao(observacao);
            pedidoRecuperado.setStatus("confirmado");
            pedidoRecuperado.confimar();
            pedidoRecuperado.remover();
            pedidoRecuperado = null;

            qtdItensCarrinho = 0;
            totalCarrinho = 0.0;
            itensCarrinho.clear();
            atualizarBadge();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void recuperarDadosUsuario() {
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando dados")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference usuariosRef = firebaseRef.child("usuarios").child(idUsuarioLogado);
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    usuario = dataSnapshot.getValue(Usuario.class);
                }
                recuperPedido();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void recuperPedido() {
        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos_usuario")
                .child(idEmpresa)
                .child(idUsuarioLogado);

        pedidoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                qtdItensCarrinho = 0;
                totalCarrinho = 0.0;
                itensCarrinho = new ArrayList<>();

                if (dataSnapshot.getValue() != null) {
                    pedidoRecuperado = dataSnapshot.getValue(Pedido.class);
                    itensCarrinho = pedidoRecuperado.getItens();

                    for (ItemPedido itemPedido : itensCarrinho) {
                        int qtde = itemPedido.getQuantidade();
                        Double preco = itemPedido.getPreco();
                        totalCarrinho += (qtde * preco);
                        qtdItensCarrinho += qtde;
                    }
                }

                DecimalFormat df = new DecimalFormat("0.00");
                textCarrinhoQtd.setText("qtd: " + qtdItensCarrinho);
                textCarrinhoTotal.setText("R$ " + df.format(totalCarrinho));

                atualizarBadge();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void recuperarProdutos() {
        DatabaseReference produtosRef = firebaseRef
                .child("produtos")
                .child(idEmpresa);

        produtosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                produtos.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    produtos.add(ds.getValue(Produto.class));
                }
                adapterProduto.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void inicializarComponentes() {
        recyclerProdutosCardapio = findViewById(R.id.recyclerProdutosCardapio);
        imageEmpresaCardapio = findViewById(R.id.imageEmpresaCardapio);
        textNomeEmpresaCardapio = findViewById(R.id.textNomeEmpresaCardapio);
        textCarrinhoQtd = findViewById(R.id.textCarrinhoQtd);
        textCarrinhoTotal = findViewById(R.id.textCarrinhoTotal);
    }
}
