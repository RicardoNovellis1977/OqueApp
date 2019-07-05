package com.example.ricardo.oqueapp.activity;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.ricardo.oqueapp.R;
import com.example.ricardo.oqueapp.config.ConfiguracaoFirebase;
import com.example.ricardo.oqueapp.fragment.ContatosFragment;
import com.example.ricardo.oqueapp.fragment.ConversasFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();


        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("O Que App");
        setSupportActionBar(toolbar);

        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                .add("Conversas",ConversasFragment.class)
                .add("Contatos",ContatosFragment.class)
                .create()
        );

        final ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);

        searchView = findViewById(R.id.material_search);

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

                ConversasFragment fragment = (ConversasFragment) adapter.getPage(0);
                fragment.recarregarConversas();
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
               // Log.d("evento","onQueryTextSubmit");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
               // Log.d("evento","onQueryTextChang");

                switch (viewPager.getCurrentItem()){
                    case 0:

                        ConversasFragment conversasFragment = (ConversasFragment) adapter.getPage(0);

                        if (newText != null && !newText.isEmpty()){
                            conversasFragment.pesquisarConversas(newText.toLowerCase());
                        }else {
                            conversasFragment.recarregarConversas();
                        }
                        break;

                    case 1:

                        ContatosFragment contatosFragment = (ContatosFragment) adapter.getPage(1);

                        if (newText != null && !newText.isEmpty()){
                            contatosFragment.pesquisarContatos(newText.toLowerCase());
                        }else {
                            contatosFragment.recarregarContatos();
                        }
                        break;
                }



                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);

        MenuItem item = menu.findItem(R.id.menuPesquisa);
        searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuSair :

                deslogarUsuario();
                finish();

                break;
            case R.id.menuConfiguracoes:
                abrirConfiguracao();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deslogarUsuario(){

        try {

            autenticacao.signOut();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void abrirConfiguracao(){
        Intent intent = new Intent(MainActivity.this,ConfiguracoesActivity.class);
        startActivity(intent);
    }
}
