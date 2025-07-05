package br.com.ifood.cursoandroid.ifood.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import br.com.ifood.cursoandroid.ifood.R;
import br.com.ifood.cursoandroid.ifood.helper.ConfiguracaoFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Atraso de 3 segundos para transição da Splash
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Verificar se o usuário já está logado, caso sim, pular a tela de autenticação
                verificarUsuarioLogado();
            }
        }, 3000);  // 3 segundos
    }

    private void verificarUsuarioLogado() {
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();

        // Se o usuário estiver logado, realiza o logout forçado
        if (usuarioAtual != null) {
            autenticacao.signOut();  // Força o logout
            usuarioAtual = null;  // Reseta o objeto de usuário
        }

        // Verifica se o usuário está logado após o signOut()
        if (usuarioAtual == null) {
            // Caso não esteja logado, abre a tela de autenticação
            abrirAutenticacao();
        } else {
            // Se estiver logado, pega o tipo de usuário e abre a tela principal
            String tipoUsuario = usuarioAtual.getDisplayName();
            abrirTelaPrincipal(tipoUsuario);
        }
    }


    private void abrirAutenticacao() {
        Intent intent = new Intent(SplashActivity.this, AutenticacaoActivity.class);
        startActivity(intent);
        finish();  // Finaliza a SplashActivity para que não volte para ela
    }

    private void abrirTelaPrincipal(String tipoUsuario) {
        Intent intent;
        if ("E".equals(tipoUsuario)) {
            intent = new Intent(SplashActivity.this, EmpresaActivity.class);  // Empresa
        } else {
            intent = new Intent(SplashActivity.this, HomeActivity.class);  // Usuário
        }
        startActivity(intent);
        finish();  // Finaliza a SplashActivity
    }
}
