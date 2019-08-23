package com.example.ricardo.oqueapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ricardo.oqueapp.R;
import com.example.ricardo.oqueapp.adapter.GrupoSelecionadoAdapter;
import com.example.ricardo.oqueapp.config.ConfiguracaoFirebase;
import com.example.ricardo.oqueapp.helper.UsuarioFirebase;
import com.example.ricardo.oqueapp.model.Grupo;
import com.example.ricardo.oqueapp.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoActivity extends AppCompatActivity {

    private List<Usuario> listaMembrosSelecionado = new ArrayList<>();
    private TextView textTotalParticipantes;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private RecyclerView recyclerMembrosSelecionados;
    private CircleImageView imageGrupo;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private Grupo grupo;
    private FloatingActionButton fabSalvarGrupo;
    private EditText editNomeGrupo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo grupo");
        toolbar.setSubtitle("Defina o nome");
        setSupportActionBar(toolbar);

        textTotalParticipantes = findViewById(R.id.text_total_participantes);
        recyclerMembrosSelecionados = findViewById(R.id.recycler_membros_grupo);
        imageGrupo = findViewById(R.id.image_grupo);
        fabSalvarGrupo = findViewById(R.id.fab_salvar_grupo);
        editNomeGrupo = findViewById(R.id.edit_nome_gupo);
        grupo = new Grupo();

        storageReference = ConfiguracaoFirebase.getFirebaseStorage();

        imageGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, SELECAO_GALERIA);
                }
            }
        });


        if (getIntent().getExtras() != null) {

            List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable("membros");
            listaMembrosSelecionado.addAll(membros);

            textTotalParticipantes.setText("Paticipantes: " + listaMembrosSelecionado.size());

        }


        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionado, getApplicationContext());

        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recyclerMembrosSelecionados.setLayoutManager(layoutManager1);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);

        fabSalvarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String nomeGrupo = editNomeGrupo.getText().toString();

                listaMembrosSelecionado.add(UsuarioFirebase.getDadosUsuarioLogado());
                grupo.setMembros(listaMembrosSelecionado);
                grupo.setNome(nomeGrupo);
                grupo.salvar();

                Intent intent = new Intent(CadastroGrupoActivity.this, ChatActivity.class);
                intent.putExtra("chatGrupo", grupo);
                startActivity(intent);


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;

            try {

                Uri localImagemSelecionada = data.getData();
                imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);

                if (imagem != null) {
                    imageGrupo.setImageBitmap(imagem);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("grupos")
                            .child(grupo.getId() + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(CadastroGrupoActivity.this, "Erro ao fazer upload da imagem"
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(CadastroGrupoActivity.this, "Sucesso ao fazer upload da imagem"
                                    , Toast.LENGTH_SHORT).show();

                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(
                                    new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            grupo.setFoto(uri.toString());
                                        }
                                    });
                        }
                    });

                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
