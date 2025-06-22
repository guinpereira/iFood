package br.com.ifood.cursoandroid.ifood.helper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Helper para operações relacionadas ao usuário Firebase.
 */
public class UsuarioFirebase {

    /**
     * Retorna o ID (UID) do usuário logado.
     * @return String - UID do usuário.
     */
    public static String getIdUsuario() {
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        FirebaseUser currentUser = autenticacao.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    /**
     * Retorna o usuário atual logado no Firebase.
     * @return FirebaseUser - usuário atual.
     */
    public static FirebaseUser getUsuarioAtual() {
        FirebaseAuth usuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        FirebaseUser user = usuario.getCurrentUser();
        if (user == null) {
            // Pode adicionar log de erro aqui se necessário
        }
        return user;
    }

    /**
     * Interface para callback de atualização de dados do usuário.
     */
    public interface DadosAtualizadosCallback {
        void onSucesso();
        void onFalha(Exception e);
    }

    /**
     * Atualiza o displayName (utilizado como tipo de usuário: "E" para empresa, "U" para usuário).
     * @param tipo String - tipo de usuário ("E" ou "U").
     * @param callback DadosAtualizadosCallback - retorno de sucesso ou falha da operação.
     */
    public static void atualizarTipoUsuario(String tipo, DadosAtualizadosCallback callback) {
        try {
            FirebaseUser user = getUsuarioAtual();
            if (user == null) {
                if (callback != null) {
                    callback.onFalha(new Exception("Usuário não está logado"));
                }
                return;
            }

            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(tipo)
                    .build();

            user.updateProfile(profile)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (callback != null) {
                                callback.onSucesso();
                            }
                        } else {
                            if (callback != null) {
                                callback.onFalha(task.getException());
                            }
                        }
                    });

        } catch (Exception e) {
            if (callback != null) {
                callback.onFalha(e);
            }
        }
    }

    /**
     * (Opcional) Atualiza o nome do usuário no Firebase Auth.
     * @param nome String - novo nome para o usuário.
     * @param callback DadosAtualizadosCallback - retorno de sucesso ou falha da operação.
     */
    public static void atualizarNomeUsuario(String nome, DadosAtualizadosCallback callback) {
        try {
            FirebaseUser user = getUsuarioAtual();
            if (user == null) {
                if (callback != null) {
                    callback.onFalha(new Exception("Usuário não está logado"));
                }
                return;
            }

            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build();

            user.updateProfile(profile)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (callback != null) {
                                callback.onSucesso();
                            }
                        } else {
                            if (callback != null) {
                                callback.onFalha(task.getException());
                            }
                        }
                    });

        } catch (Exception e) {
            if (callback != null) {
                callback.onFalha(e);
            }
        }
    }
}
