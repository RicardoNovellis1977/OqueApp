package com.example.ricardo.oqueapp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.ricardo.oqueapp.R;
import com.example.ricardo.oqueapp.activity.ChatActivity;
import com.example.ricardo.oqueapp.activity.GrupoActivity;
import com.example.ricardo.oqueapp.adapter.ContatosAdapter;
import com.example.ricardo.oqueapp.adapter.ConversasAdapter;
import com.example.ricardo.oqueapp.config.ConfiguracaoFirebase;
import com.example.ricardo.oqueapp.helper.RecyclerItemClickListener;
import com.example.ricardo.oqueapp.helper.UsuarioFirebase;
import com.example.ricardo.oqueapp.model.Conversa;
import com.example.ricardo.oqueapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewContatos;
    private ContatosAdapter adapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;

    public ContatosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        recyclerViewContatos = view.findViewById(R.id.recycler_view_contatos);
        usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();


        adapter = new ContatosAdapter(listaContatos, getContext());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewContatos.setLayoutManager(layoutManager);
        recyclerViewContatos.setHasFixedSize(true);
        recyclerViewContatos.setAdapter(adapter);

        recyclerViewContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(), recyclerViewContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Usuario> listaUsuarioAtualizada = adapter.getContatos();

                                Usuario usuarioSelecionado = listaUsuarioAtualizada.get(position);
                                boolean cabechalho = usuarioSelecionado.getEmail().isEmpty();

                                if (cabechalho){

                                    Intent i = new Intent(getActivity(), GrupoActivity.class);
                                    startActivity(i);

                                }else {

                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("chatContato",usuarioSelecionado);
                                    startActivity(intent);
                                }

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

     return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioRef.removeEventListener(valueEventListenerContatos);
    }

    public void recuperarContatos (){

        listaContatos.clear();

        Usuario itemGrupo = new Usuario();
        itemGrupo.setNome("Novo Grupo");
        itemGrupo.setEmail("");

        listaContatos.add(itemGrupo);

       valueEventListenerContatos = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dados : dataSnapshot.getChildren()){

                    Usuario usuario = dados.getValue(Usuario.class);

                    String emailUsuarioAtual = usuarioAtual.getEmail();

                    if (!emailUsuarioAtual.equals(usuario.getEmail())){
                        listaContatos.add(usuario);
                    }

                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void pesquisarContatos(String texto){
        //  Log.d("pesquisa","texto");

        List<Usuario> listaContatosBusca = new ArrayList<>();

        for (Usuario usuario : listaContatos){

       String nome = usuario.getNome().toLowerCase();
       if (nome.contains(texto)){
           listaContatosBusca.add(usuario);
       }


        }
        adapter = new ContatosAdapter(listaContatosBusca, getActivity());
        recyclerViewContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void recarregarContatos(){
        adapter = new ContatosAdapter(listaContatos, getActivity());
        recyclerViewContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

}
