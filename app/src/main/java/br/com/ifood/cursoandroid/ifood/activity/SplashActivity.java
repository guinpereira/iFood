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

        // Se o usuário já estiver logado, ir diretamente para a tela principal
        if (usuarioAtual != null) {
            String tipoUsuario = usuarioAtual.getDisplayName();
            abrirTelaPrincipal(tipoUsuario);  // Redireciona para a tela principal
        } else {
            // Caso contrário, abre a tela de autenticação
            abrirAutenticacao();
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
