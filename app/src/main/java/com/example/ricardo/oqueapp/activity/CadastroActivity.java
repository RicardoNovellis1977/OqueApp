package com.example.ricardo.oqueapp.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.ricardo.oqueapp.R;
import com.example.ricardo.oqueapp.config.ConfiguracaoFirebase;
import com.example.ricardo.oqueapp.helper.Base64Custom;
import com.example.ricardo.oqueapp.helper.UsuarioFirebase;
import com.example.ricardo.oqueapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText edtNome;
    private TextInputEditText edtEmail;
    private TextInputEditText edtSenha;
    private FirebaseAuth autenticacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
    }

    public void cadastrarUsuario(final Usuario usuario){

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        autenticacao.createUserWithEmailAndPassword(
            usuario.getEmail(),usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    Toast.makeText(CadastroActivity.this,"Sucesso ao cadastrar usuario!",Toast.LENGTH_LONG).show();

                    UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());
                    finish();

                    try {

                        String idUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                        usuario.setId(idUsuario);
                        usuario.salvar();

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else{
                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Por favor, digite um email valido";
                    }catch (FirebaseAuthUserCollisionException e){
                        excecao = "Esta conta ja foi cadastrada";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuario: "+ e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this,excecao,Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void validarUsuario(View view){

        String txtNome = edtNome.getText().toString();
        String txtEmail = edtEmail.getText().toString();
        String txtSenha = edtSenha.getText().toString();

        if (!txtNome.isEmpty()){

            if (!txtEmail.isEmpty()){

                if (!txtSenha.isEmpty()){

                    Usuario usuario = new Usuario();
                    usuario.setNome(txtNome);
                    usuario.setEmail(txtEmail);
                    usuario.setSenha(txtSenha);

                    cadastrarUsuario(usuario);
                }else {

                    Toast.makeText(CadastroActivity.this,"Preencha a senha",Toast.LENGTH_LONG).show();
                }

            }else{

                Toast.makeText(CadastroActivity.this,"Preencha o email",Toast.LENGTH_LONG).show();
            }
        }else {

            Toast.makeText(CadastroActivity.this,"Preencha o nome",Toast.LENGTH_LONG).show();
        }
    }
}
