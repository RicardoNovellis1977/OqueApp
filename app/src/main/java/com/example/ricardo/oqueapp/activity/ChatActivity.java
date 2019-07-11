package com.example.ricardo.oqueapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ricardo.oqueapp.R;
import com.example.ricardo.oqueapp.adapter.MensagensAdapter;
import com.example.ricardo.oqueapp.api.NotificacaoService;
import com.example.ricardo.oqueapp.config.ConfiguracaoFirebase;
import com.example.ricardo.oqueapp.helper.Base64Custom;
import com.example.ricardo.oqueapp.helper.RetrofitConfig;
import com.example.ricardo.oqueapp.helper.UsuarioFirebase;
import com.example.ricardo.oqueapp.model.Conversa;
import com.example.ricardo.oqueapp.model.Grupo;
import com.example.ricardo.oqueapp.model.Mensagem;
import com.example.ricardo.oqueapp.model.Notificacao;
import com.example.ricardo.oqueapp.model.NotificacaoDados;
import com.example.ricardo.oqueapp.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ChatActivity extends AppCompatActivity {

    TextView textViewNome;
    CircleImageView circleImageViewFoto;
    private EditText editMensagem;
    private ImageView imageCamera;
    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;
    private DatabaseReference database;
    private StorageReference storage;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagem;
    private static final int SELECAO_CAMERA = 100;
    private Grupo grupo;

    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;

    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewNome = findViewById(R.id.text_view_nome_chat);
        circleImageViewFoto = findViewById(R.id.circle_image_foto_chat);
        editMensagem = findViewById(R.id.edit_mensagem);
        recyclerMensagens = findViewById(R.id.recycler_mensagens);
        imageCamera = findViewById(R.id.image_camera);

        usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            if (bundle.containsKey("chatGrupo")) {
                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
                textViewNome.setText(grupo.getNome());

                String foto = grupo.getFoto();
                if (foto != null) {

                    Uri url = Uri.parse(foto);
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleImageViewFoto);
                } else {
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }
            } else {
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                textViewNome.setText(usuarioDestinatario.getNome());
                String foto = usuarioDestinatario.getFoto();
                if (foto != null) {

                    Uri url = Uri.parse(usuarioDestinatario.getFoto());
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleImageViewFoto);
                } else {
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }
                idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
            }
        }

        adapter = new MensagensAdapter(mensagens, getApplicationContext());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setHasFixedSize(true);
        recyclerMensagens.setAdapter(adapter);

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        mensagensRef = database.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, SELECAO_CAMERA);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            Bitmap imagem = null;

            try {

                switch (requestCode) {
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                }
                if (imagem != null) {

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    String nomeImagem = UUID.randomUUID().toString();

                    StorageReference imagemRef = storage.child("imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeImagem);

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatActivity.this, "Erro ao fazer upload da imagem"
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Mensagem mensagem = new Mensagem();
                                    mensagem.setIdUsuario(idUsuarioRemetente);
                                    mensagem.setMensagem("image.jpeg");
                                    mensagem.setImagem(uri.toString());

                                    salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                                    salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                                    Toast.makeText(ChatActivity.this, "Sucesso ao enviar imagem"
                                            , Toast.LENGTH_SHORT).show();
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

    public void enviarMensagem(View view) {

        String textoMensagem = editMensagem.getText().toString();

        if (!textoMensagem.isEmpty()) {

            if (usuarioDestinatario != null) {

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(textoMensagem);

                RetrofitConfig retrofitConfig = new RetrofitConfig();

                String to = usuarioDestinatario.getToken();
                Notificacao notificacao = new Notificacao(usuarioRemetente.getNome(), textoMensagem);
                NotificacaoDados notificacaoDados = new NotificacaoDados(to, notificacao);

                NotificacaoService service = retrofitConfig.getRetrofit().create(NotificacaoService.class);
                Call<NotificacaoDados> call = service.salvarNotificacao(notificacaoDados);

                call.enqueue(new Callback<NotificacaoDados>() {
                    @Override
                    public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {
                        Toast.makeText(ChatActivity.this, "codigo: " + response.code(),
                                Toast.LENGTH_SHORT).show();

                        if (response.isSuccessful()) {

                        }
                    }

                    @Override
                    public void onFailure(Call<NotificacaoDados> call, Throwable t) {

                    }
                });

                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario, mensagem, false);

                salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem, false);

            } else {

                for (Usuario membro : grupo.getMembros()) {

                    FirebaseMessaging.getInstance().subscribeToTopic(grupo.getNome());

                    String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                    mensagem.setMensagem(textoMensagem);
                    mensagem.setNome(usuarioRemetente.getNome());

                    RetrofitConfig retrofitConfig = new RetrofitConfig();

                    final String to = "/topics/" + grupo.getNome();
                    Notificacao notificacao = new Notificacao(grupo.getNome() + " -> " + usuarioRemetente.getNome(), textoMensagem);
                    NotificacaoDados notificacaoDados = new NotificacaoDados(to, notificacao);

                    NotificacaoService service = retrofitConfig.getRetrofit().create(NotificacaoService.class);

                    Call<NotificacaoDados> call = service.salvarNotificacao(notificacaoDados);

                    call.enqueue(new Callback<NotificacaoDados>() {
                        @Override
                        public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {
                            Toast.makeText(ChatActivity.this, "codigo: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<NotificacaoDados> call, Throwable t) {

                        }
                    });

                    salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                    salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true);


                }

            }
        } else {

            Toast.makeText(this, "Digite uma mensagem para enviar !"
                    , Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarConversa(String idRemetente, String idDestinatario, Usuario usuarioExibicao, Mensagem msg, boolean isGroup) {

        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idRemetente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUltimaMensagem(msg.getMensagem());

        if (isGroup) {

            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo(grupo);

        } else {
            conversaRemetente.setUsuarioExibicao(usuarioExibicao);
            conversaRemetente.setIsGroup("false");
        }
        conversaRemetente.salvar();
    }

    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg) {

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        editMensagem.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagem();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagem);
    }

    private void recuperarMensagem() {

        childEventListenerMensagem = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
