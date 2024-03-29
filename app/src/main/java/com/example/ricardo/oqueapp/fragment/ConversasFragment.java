package com.example.ricardo.oqueapp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.ricardo.oqueapp.R;
import com.example.ricardo.oqueapp.activity.ChatActivity;
import com.example.ricardo.oqueapp.adapter.ConversasAdapter;
import com.example.ricardo.oqueapp.config.ConfiguracaoFirebase;
import com.example.ricardo.oqueapp.helper.RecyclerItemClickListener;
import com.example.ricardo.oqueapp.helper.UsuarioFirebase;
import com.example.ricardo.oqueapp.model.Conversa;
import com.example.ricardo.oqueapp.model.Usuario;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversasFragment extends Fragment {

    private RecyclerView recyclerViewConversas;
    private List<Conversa> listaConversas = new ArrayList<>();
    private ConversasAdapter adapter;
    private DatabaseReference database;
    private DatabaseReference conversasRef;
    private ChildEventListener childEventListenerConversas;

    public ConversasFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View view = inflater.inflate(R.layout.fragment_conversas, container, false);

       recyclerViewConversas = view.findViewById(R.id.recycler_list_conversas);

       adapter = new ConversasAdapter(listaConversas, getActivity());

       RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
       recyclerViewConversas.setLayoutManager(layoutManager);
       recyclerViewConversas.setHasFixedSize(true);
       recyclerViewConversas.setAdapter(adapter);

       recyclerViewConversas.addOnItemTouchListener(
               new RecyclerItemClickListener(
                       getActivity(), recyclerViewConversas,
                       new RecyclerItemClickListener.OnItemClickListener() {
                           @Override
                           public void onItemClick(View view, int position) {

                               List<Conversa> listaConversasAtualizada = adapter.getConversas();
                               Conversa conversaSelecionada = listaConversasAtualizada.get(position);

                               if (conversaSelecionada.getIsGroup().equals("true")){
                                   Intent intent = new Intent(getActivity(), ChatActivity.class);
                                   intent.putExtra("chatGrupo",conversaSelecionada.getGrupo());
                                   startActivity(intent);

                               }else{
                                   Intent intent = new Intent(getActivity(), ChatActivity.class);
                                   intent.putExtra("chatContato",conversaSelecionada.getUsuarioExibicao());
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

        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        database = ConfiguracaoFirebase.getFirebaseDatabase();

         conversasRef = database.child("conversas")
                .child(identificadorUsuario);

       return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversasRef.removeEventListener(childEventListenerConversas);
    }

    public void pesquisarConversas(String texto){
      //  Log.d("pesquisa","texto");

        List<Conversa> listaConversasBusca = new ArrayList<>();

        for (Conversa conversa : listaConversas){

            if (conversa.getUsuarioExibicao() != null){

                String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
                if (nome.contains(texto)){
                    listaConversasBusca.add(conversa);
                }
            }else {

                String nome = conversa.getGrupo().getNome().toLowerCase();
                if (nome.contains(texto)){
                    listaConversasBusca.add(conversa);
                }
            }


        }
        adapter = new ConversasAdapter(listaConversasBusca, getActivity());
        recyclerViewConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void recarregarConversas(){
        adapter = new ConversasAdapter(listaConversas, getActivity());
        recyclerViewConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void recuperarConversas (){

        listaConversas.clear();

       childEventListenerConversas = conversasRef.addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

               Conversa conversa = dataSnapshot.getValue(Conversa.class);
               listaConversas.add(conversa);

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
