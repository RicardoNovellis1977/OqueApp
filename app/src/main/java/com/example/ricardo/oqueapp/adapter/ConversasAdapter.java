package com.example.ricardo.oqueapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ricardo.oqueapp.R;
import com.example.ricardo.oqueapp.model.Conversa;
import com.example.ricardo.oqueapp.model.Grupo;
import com.example.ricardo.oqueapp.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversasAdapter extends RecyclerView.Adapter<ConversasAdapter.MyViewHolder> {

    private List<Conversa> conversas;
    private Context context;

    public ConversasAdapter(List<Conversa> lista, Context c) {
        this.conversas = lista;
        this.context = c;
    }
    public List<Conversa> getConversas(){
        return this.conversas;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_contatos,viewGroup,false);
        return new MyViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        Conversa conversa = conversas.get(i);
        myViewHolder.ultimaMensagem.setText(conversa.getUltimaMensagem());

        if (conversa.getIsGroup().equals("true")){

            Grupo grupo = conversa.getGrupo();
            myViewHolder.nome.setText(grupo.getNome());

            if (grupo.getFoto() != null){

                Uri uri = Uri.parse(grupo.getFoto());
                Glide.with(context)
                        .load(uri)
                        .into(myViewHolder.foto);

            }else {
                myViewHolder.foto.setImageResource(R.drawable.padrao);
            }

        }else{

            Usuario usuario = conversa.getUsuarioExibicao();
            if (usuario != null){

                myViewHolder.nome.setText(usuario.getNome());

                if (usuario.getFoto() != null){

                    Uri uri = Uri.parse(usuario.getFoto());
                    Glide.with(context)
                            .load(uri)
                            .into(myViewHolder.foto);

                }else {
                    myViewHolder.foto.setImageResource(R.drawable.padrao);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return conversas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome;
        TextView ultimaMensagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.image_view_foto_contato);
            nome = itemView.findViewById(R.id.text_nome_contato);
            ultimaMensagem = itemView.findViewById(R.id.text_email_contato);


        }
    }
}
