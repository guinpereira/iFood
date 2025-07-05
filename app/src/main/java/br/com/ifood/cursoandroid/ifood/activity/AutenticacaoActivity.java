package br.com.ifood.cursoandroid.ifood.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import br.com.ifood.cursoandroid.ifood.R;
import br.com.ifood.cursoandroid.ifood.helper.ConfiguracaoFirebase;
import br.com.ifood.cursoandroid.ifood.helper.UsuarioFirebase;

public class AutenticacaoActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private Switch tipoAcesso, tipoUsuario;
    private LinearLayout linearTipoUsuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autenticacao);

        inicializaComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        verificarUsuarioLogado();

        tipoAcesso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    linearTipoUsuario.setVisibility(View.VISIBLE);
                } else {
                    linearTipoUsuario.setVisibility(View.GONE);
                }
            }
        });

        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                if (!email.isEmpty()) {
                    if (!senha.isEmpty()) {

                        if (tipoAcesso.isChecked()) {
                            // Cadastro
                            autenticacao.createUserWithEmailAndPassword(email, senha)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                String tipoUsuario = getTipoUsuario();
                                                UsuarioFirebase.atualizarTipoUsuario(tipoUsuario, new UsuarioFirebase.DadosAtualizadosCallback() {
                                                    @Override
                                                    public void onSucesso() {
                                                        Toast.makeText(AutenticacaoActivity.this,
                                                                "Cadastro realizado com sucesso!",
                                                                Toast.LENGTH_SHORT).show();
                                                        abrirTelaPrincipal(tipoUsuario);
                                                    }

                                                    @Override
                                                    public void onFalha(Exception e) {
                                                        Toast.makeText(AutenticacaoActivity.this,
                                                                "Erro ao atualizar perfil: " + e.getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            } else {
                                                String erroExcecao = "";

                                                try {
                                                    throw task.getException();
                                                } catch (FirebaseAuthWeakPasswordException e) {
                                                    erroExcecao = "Digite uma senha mais forte!";
                                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                                    erroExcecao = "Por favor, digite um e-mail válido";
                                                } catch (FirebaseAuthUserCollisionException e) {
                                                    erroExcecao = "Esta conta já foi cadastrada";
                                                } catch (Exception e) {
                                                    erroExcecao = "Erro ao cadastrar usuário: " + e.getMessage();
                                                    e.printStackTrace();
                                                }

                                                Toast.makeText(AutenticacaoActivity.this,
                                                        "Erro: " + erroExcecao,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            // Login
                            autenticacao.signInWithEmailAndPassword(email, senha)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                if (user != null) {
                                                    user.reload().addOnCompleteListener(reloadTask -> {
                                                        if (reloadTask.isSuccessful()) {
                                                            String tipoUsuario = user.getDisplayName();

                                                            if (tipoUsuario == null) {
                                                                Toast.makeText(AutenticacaoActivity.this,
                                                                        "Tipo de usuário não encontrado!",
                                                                        Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(AutenticacaoActivity.this,
                                                                        "Logado com sucesso",
                                                                        Toast.LENGTH_SHORT).show();
                                                                abrirTelaPrincipal(tipoUsuario);
                                                            }
                                                        } else {
                                                            Toast.makeText(AutenticacaoActivity.this,
                                                                    "Erro ao recarregar dados do usuário",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            } else {
                                                Toast.makeText(AutenticacaoActivity.this,
                                                        "Erro ao fazer login: " + task.getException(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }

                    } else {
                        Toast.makeText(AutenticacaoActivity.this,
                                "Preencha a senha!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AutenticacaoActivity.this,
                            "Preencha o E-mail!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verificarUsuarioLogado() {
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if (usuarioAtual != null) {
            String tipoUsuario = usuarioAtual.getDisplayName();
            abrirTelaPrincipal(tipoUsuario);
        }
    }

    private String getTipoUsuario() {
        return tipoUsuario.isChecked() ? "E" : "U";
    }

    private void abrirTelaPrincipal(String tipoUsuario) {
        if (tipoUsuario != null && tipoUsuario.equals("E")) {
            startActivity(new Intent(getApplicationContext(), EmpresaActivity.class));
        } else {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }
        finish();
    }

    private void inicializaComponentes() {
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        botaoAcessar = findViewById(R.id.buttonAcesso);
        tipoAcesso = findViewById(R.id.switchAcesso);
        tipoUsuario = findViewById(R.id.switchTipoUsuario);
        linearTipoUsuario = findViewById(R.id.linearTipoUsuario);
    }

    private void logoutSeguro() {
        FirebaseAuth.getInstance().signOut();
        new Handler(Looper.getMainLooper()).post(() -> {
            CookieManager.getInstance().removeAllCookies(null);
        });
    }
}
