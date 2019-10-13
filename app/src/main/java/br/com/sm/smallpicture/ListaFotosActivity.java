package br.com.sm.smallpicture;

import android.Manifest;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.sm.smallpicture.databinding.ActivityListaFotosBinding;
import br.com.sm.smallpicture.util.IabHelper;
import br.com.sm.smallpicture.util.IabResult;
import br.com.sm.smallpicture.util.Inventory;
import br.com.sm.smallpicture.util.Purchase;
import id.zelory.compressor.Compressor;
import id.zelory.compressor.FileUtil;

public class ListaFotosActivity extends AppCompatActivity implements ListaFotosAdapter.OnFotoClickListener {

    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int PICK_IMAGE_REQUEST_CAMERA = 1;

    private ActivityListaFotosBinding mBinding;
    private ListaFotosAdapter mAdapter;

    private ImageLoader mImageLoader;
    private List<Imagens> listaFotos;

    private DisplayImageOptions mDisplayImageOptions;
    private ImageLoaderConfiguration conf;

    private int reduzir = 1;
    private int qualidade = 75;
    private boolean jpegSelecionado = true;

    private double[] values = {1, 1.5, 2.0, 2.5, 3, 3.5, 4, 4.5, 5, 5.5};

    private MaterialDialog materialDialogCompress;

    private AdRequest adRequestBanner;
    private InterstitialAd mInterstitialAd;

    private int fotosSelecionadas = 0;

    private Menu menuPrivate;

    private File caminhoFoto;

    private SharedPreferences settings;

    // CONSTANTS
    private static final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgxAjW2kYGdr3rVypzoF1rGc7MNU/1OBQRSwsdBN42kXXTTMIa1RMRspRw6/yOoViwiNW5qsDtp54W4D6iXqeItR7p0jMCBdqchOB7hAUD+KIwT5k1eJboKB8chP4em3h6cIBCdLsRu5ou1myXnaFrsdkehUitEeBIT9cmwgeUQhLtoscNIHpgAZ4BDruNXpCjPY89e5JtaoeRlPqKqPCOyvwWBqk9duG8+jZnDQjKnO3lHl8rqoEApoowDHsNL7zOBG80l2AA95j69wtMMzZPnNAKJ96cXetRYPWM146ax8KXzMgvHPU3e57LmvlHNYGssArjlFbBdWQ40PXTQEPoQIDAQAB";
    private static final String PRODUCT_ID = "remover_anuncio";
    public IabHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_lista_fotos);

        mBinding.recyclerView.setHasFixedSize(true);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(dy > 0){
                    mBinding.fab.hideMenuButton(true);
                } else {
                    mBinding.fab.showMenuButton(true);
                }
            }
        });

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        int display_mode = getResources().getConfiguration().orientation;
        int collumnGrid;

        if (display_mode == Configuration.ORIENTATION_PORTRAIT) {
            collumnGrid = 2;
        } else {
            collumnGrid = 4;
        }

        GridLayoutManager grid = new GridLayoutManager(ListaFotosActivity.this, collumnGrid);
        mBinding.recyclerView.setLayoutManager(grid);

        mDisplayImageOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .displayer(new FadeInBitmapDisplayer(500))
                .build();

        conf = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(mDisplayImageOptions)
                .diskCacheSize(50 * 1024 * 1024)
                .threadPoolSize(5)
                .writeDebugLogs()
                .build();

        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(conf);

        if(getIntent().getClipData() != null) {
            new ListarImagensAsyncTask().execute(getIntent());
        }

        if(savedInstanceState != null){

            reduzir = savedInstanceState.getInt("reduzir");
            qualidade = savedInstanceState.getInt("qualidade");
            listaFotos = (List<Imagens>) savedInstanceState.getSerializable("listaFotos");
            fotosSelecionadas = savedInstanceState.getInt("fotosSelecionadas");
            jpegSelecionado = savedInstanceState.getBoolean("jpegSelecionado");

            mAdapter = new ListaFotosAdapter(getBaseContext(), listaFotos, mImageLoader, this);
            mBinding.recyclerView.setAdapter(mAdapter);

        } else {
            listaFotos = new ArrayList<>();
        }


        AdView mAdView = (AdView) findViewById(R.id.adView);



        if(!((MyApplication) getApplication()).premium){
            instanceIabHelper(false);

            adRequestBanner = new AdRequest.Builder()
                    .addTestDevice("CCD03D8BC5B2F63277810097989A6035")
                    .build();
            mAdView.loadAd(adRequestBanner);

            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.interstial_ad_unit_id));

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewInterstitial();
                }
            });

            requestNewInterstitial();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putInt("reduzir", reduzir);
        savedInstanceState.putInt("qualidade", qualidade);
        savedInstanceState.putSerializable("listaFotos", (Serializable) listaFotos);
        savedInstanceState.putSerializable("fotosSelecionadas", fotosSelecionadas);
        savedInstanceState.putBoolean("jpegSelecionado", jpegSelecionado);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void requestNewInterstitial() {
        if(!((MyApplication) getApplication()).premium){
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("CCD03D8BC5B2F63277810097989A6035")
                    .build();

            mInterstitialAd.loadAd(adRequest);
        }
    }

    public void showInterstial() {
        if(!((MyApplication) getApplication()).premium){
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_config, menu);

        menuPrivate = menu;

        if(fotosSelecionadas > 0){

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            itemSelecionarMenuToolbar(true);

            getSupportActionBar().setTitle(getString(R.string.quantidade_fotos_selecao, fotosSelecionadas));
        }else {
            itemSelecionarMenuToolbar(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                for(int i = 0; i < listaFotos.size(); i++){
                    listaFotos.get(i).setSelecionado(false);
                }

                getSupportActionBar().setTitle(getString(R.string.app_name));

                fotosSelecionadas = 0;

                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                itemSelecionarMenuToolbar(false);

                mAdapter.notifyDataSetChanged();

                break;

            case R.id.menuReduzir:



                if (listaFotos != null) {
                    if (listaFotos.size() > 0) {
                        configuracoesFotos();
                    } else {
                        Toast.makeText(this, getString(R.string.selecione_fotos), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.selecione_fotos), Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.menuRemover:

                int cont = fotosSelecionadas;

                while (cont > 0){
                    for (int i = 0; i < listaFotos.size(); i++){
                        if(listaFotos.get(i).isSelecionado()) {
                            mAdapter.notifyItemRemoved(i);
                            listaFotos.remove(i);
                            break;
                        }
                    }
                    cont--;
                }

                fotosSelecionadas = 0;

                getSupportActionBar().setTitle(getString(R.string.app_name));

                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                itemSelecionarMenuToolbar(false);

                break;

            case R.id.menuCompartilhar:

                if(fotosSelecionadas > 0) {
                    ArrayList<Uri> files = new ArrayList<>();

                    for (Imagens imagens : listaFotos) {
                        if (imagens.isSelecionado()) {
                            files.add(Uri.parse(imagens.getUri()));
                        }
                    }

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                    shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.compartilhar_com)));

                    fotosSelecionadas = 0;

                    for(int i = 0; i < listaFotos.size(); i++){
                        listaFotos.get(i).setSelecionado(false);
                    }

                    getSupportActionBar().setTitle(getString(R.string.app_name));

                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    itemSelecionarMenuToolbar(false);

                    mAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, getString(R.string.selecione_fotos), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.menuRemoverAnuncios:

                if(!((MyApplication) getApplication()).premium){
                    try {
                        mHelper.launchPurchaseFlow(this, "remover_anuncio", 1002, this.mIabPurchaseFinishedListener);
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao comprar", Toast.LENGTH_SHORT).show();
                    }
                }

                break;

            case R.id.menuAvaliar:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + this.getPackageName())));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
                }
                break;

            case R.id.menuOutrosApp:

                startActivity(new Intent(this, OutrosAppActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {

            if (data == null) {
                return;
            }

            new ListarImagensAsyncTask().execute(data);
        }

        if (requestCode == PICK_IMAGE_REQUEST_CAMERA && resultCode == RESULT_OK) {

            if (data == null) {
                data = new Intent();
                data.setData(Uri.parse("file://" + caminhoFoto));

                caminhoFoto = null;
            }

            new ListarImagensAsyncTask().execute(data);
        }

    }

    private void configuracoesFotos() {

        boolean wrapInScrollView = true;
        new MaterialDialog.Builder(this)
                .title(getString(R.string.configuracoes_saida))
                .customView(R.layout.dialog_config_fotos, wrapInScrollView)
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Dialog d = (Dialog) dialog;

                        SeekBar sbQualidade = (SeekBar) d.findViewById(R.id.sbQualidade);
                        SeekBar sbReduzir = (SeekBar) d.findViewById(R.id.sbReduzir);
                        final TextView txtBarraQualidade = (TextView) d.findViewById(R.id.txtBarraQualidade);
                        final TextView txtBarraReduzir = (TextView) d.findViewById(R.id.txtBarraReduzir);
                        RadioButton rb1 = (RadioButton) d.findViewById(R.id.rbJPEG);
                        RadioButton rb2 = (RadioButton) d.findViewById(R.id.rbWEBP);

                        if(jpegSelecionado){
                            rb1.isChecked();
                        } else {
                            rb2.isChecked();
                        }

                        //auxReduzir = reduzir;
                        //auxQualidade = qualidade;

                        sbQualidade.setProgress(qualidade);
                        txtBarraQualidade.setText("" + qualidade);

                        sbReduzir.setProgress(reduzir);
                        sbReduzir.setMax(8);
                        txtBarraReduzir.setText(values[reduzir] + "x");

                        sbQualidade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                qualidade = progress;
                                txtBarraQualidade.setText(qualidade + "");
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                            }
                        });

                        sbReduzir.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                reduzir = progress;
                                txtBarraReduzir.setText(values[reduzir] + "x");
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                            }
                        });

                        rb1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                jpegSelecionado = true;
                            }
                        });

                        rb2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                jpegSelecionado = false;
                            }
                        });
                    }
                })
                .negativeColor(Color.BLACK)
                .positiveText(getString(R.string.reduzir))
                .negativeText(getString(R.string.cancelar))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        clickCompress();
                    }
                })
                .show();
    }

    private void preecherListaFotos(Uri uri) {
        try {
            File actualImage = FileUtil.from(getBaseContext(), uri);

            Bitmap bmp = BitmapFactory.decodeFile(actualImage.getAbsolutePath());

            Imagens img = new Imagens();

            img.setWidth(bmp.getWidth());
            img.setHeigth(bmp.getHeight());
            img.setName(actualImage.getName());
            img.setCaminhoImagem(actualImage.getAbsolutePath());
            img.setUri(uri.toString());
            img.setTamanho(actualImage.length());
            img.setSeparator(false);

            listaFotos.add(img);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickAbrirFotos(View view) {
        mBinding.fab.close(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    public void clickCamera(View view) {
        mBinding.fab.close(true);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            String nomeFoto = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString() + ".jpeg";

            caminhoFoto = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SmallPictures", nomeFoto);

            Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(caminhoFoto));
            startActivityForResult(it, PICK_IMAGE_REQUEST_CAMERA);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    public void clickCompress() {

        materialDialogCompress = new MaterialDialog.Builder(ListaFotosActivity.this)
                .title(getString(R.string.app_name))
                .content(getString(R.string.reduzindo_imagens))
                .progress(true, 0)
                .cancelable(false)
                .show();

        new CompactarImagensAsyncTask().execute();

    }

    @Override
    public void onBackPressed() {

        if(fotosSelecionadas > 0){
            for(int i = 0; i < listaFotos.size(); i++){
                listaFotos.get(i).setSelecionado(false);
            }

            getSupportActionBar().setTitle(getString(R.string.app_name));
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            itemSelecionarMenuToolbar(false);

            mAdapter.notifyDataSetChanged();

            fotosSelecionadas = 0;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mImageLoader != null) {
            mImageLoader.clearMemoryCache();
            mImageLoader.clearDiskCache();
            mImageLoader.stop();
        }
    }

    @Override
    public void setOnFotoClickListener(ListaFotosAdapter.ListaFotosHolder holder) {

        if(fotosSelecionadas > 0){

            if(holder.mBinding.imagemSelecionada.getVisibility() == View.VISIBLE){

                listaFotos.get(holder.getAdapterPosition()).setSelecionado(false);

                fotosSelecionadas--;
                getSupportActionBar().setTitle(getString(R.string.quantidade_fotos_selecao, fotosSelecionadas));
                holder.mBinding.imagemSelecionada.setVisibility(View.GONE);

            } else {
                fotosSelecionadas++;
                getSupportActionBar().setTitle(getString(R.string.quantidade_fotos_selecao, fotosSelecionadas));
                listaFotos.get(holder.getAdapterPosition()).setSelecionado(true);
                holder.mBinding.imagemSelecionada.setVisibility(View.VISIBLE);
            }

            if(fotosSelecionadas == 0){
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                itemSelecionarMenuToolbar(false);

                getSupportActionBar().setTitle(getString(R.string.app_name));
            }

        } else {
            Imagens imagem = listaFotos.get(holder.getAdapterPosition());

            Intent intent = new Intent(ListaFotosActivity.this, AbrirImagemActivity.class);
            intent.putExtra("Imagem", imagem);
            startActivity(intent);
        }
    }

    @Override
    public void setOnFotoLongClickListener(ListaFotosAdapter.ListaFotosHolder holder) {

        if(fotosSelecionadas == 0){
            listaFotos.get(holder.getAdapterPosition()).setSelecionado(true);

            fotosSelecionadas++;
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            itemSelecionarMenuToolbar(true);

            getSupportActionBar().setTitle(getString(R.string.quantidade_fotos_selecao, fotosSelecionadas));

            holder.mBinding.imagemSelecionada.setVisibility(View.VISIBLE);
        }
    }

    private void itemSelecionarMenuToolbar(boolean selecao){

        //Reduzir imagens
        menuPrivate.getItem(0).setVisible(!selecao);

        //Remover anúncios
        menuPrivate.getItem(1).setVisible(((MyApplication) getApplication()).premium ? false : !selecao);

        //Avaliar App
        menuPrivate.getItem(2).setVisible(!selecao);

        //Outros App's
        menuPrivate.getItem(3).setVisible(!selecao);

        //Remover
        menuPrivate.getItem(4).setVisible(selecao);

        //Compartilhar
        menuPrivate.getItem(5).setVisible(selecao);

        for(int i = 0; i <= 5; i++){
            //Log.i("Android", "Position: " + i + " - Title: " + menuPrivate.getItem(i).getTitle());
        }
    }


    private class ListarImagensAsyncTask extends AsyncTask<Intent, Void, Void> {

        private MaterialDialog materialDialog;

        @Override
        protected void onPreExecute() {
            materialDialog = new MaterialDialog.Builder(ListaFotosActivity.this)
                    .title(getString(R.string.app_name))
                    .content(getString(R.string.buscando_imagens))
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected Void doInBackground(Intent... data) {

            ClipData clip = data[0].getClipData();

            listaFotos.clear();

            if (clip == null) {
                preecherListaFotos(data[0].getData());

            } else {
                for (int i = 0; i < clip.getItemCount(); i++) {
                    preecherListaFotos(clip.getItemAt(i).getUri());
                }
            }

            mAdapter = new ListaFotosAdapter(getBaseContext(), listaFotos, mImageLoader, ListaFotosActivity.this);

            atualizarLista();

            return null;
        }

        private void atualizarLista() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBinding.recyclerView.setAdapter(mAdapter);
                    requestNewInterstitial();
                }
            });
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            materialDialog.dismiss();
        }
    }

    private class CompactarImagensAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            File direct = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SmallPictures");

            if (!direct.exists()) {
                if (direct.mkdir()) ;
            }

            boolean erro = false;

            File actualImage;
            File compressedImage;
            Bitmap.CompressFormat formato = jpegSelecionado ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG;

            for (int i = 0; i < listaFotos.size(); i++) {

                try {
                    actualImage = FileUtil.from(getBaseContext(), Uri.parse(listaFotos.get(i).getUri()));

                    Bitmap imagemEntrada = BitmapFactory.decodeFile(actualImage.getAbsolutePath());

                    int width = (int) (imagemEntrada.getWidth() / values[reduzir]);
                    int heigth = (int) (imagemEntrada.getHeight() / values[reduzir]);

                    try {
                        compressedImage = new Compressor.Builder(ListaFotosActivity.this)
                                .setMaxWidth(width)
                                .setMaxHeight(heigth)
                                .setQuality(qualidade)
                                .setCompressFormat(formato)
                                .setDestinationDirectoryPath(direct.getPath())
                                .build()
                                .compressToFile(actualImage);

                        Bitmap imagemSaida = BitmapFactory.decodeFile(compressedImage.getAbsolutePath());

                        int porc = (int) ((compressedImage.length() * 100) / actualImage.length());

                        porc -= 100;

                        listaFotos.get(i).setCaminhoImagem(compressedImage.getAbsolutePath());
                        listaFotos.get(i).setWidth(imagemSaida.getWidth());
                        listaFotos.get(i).setHeigth(imagemSaida.getHeight());
                        listaFotos.get(i).setTamanho(compressedImage.length());
                        listaFotos.get(i).setPorcentagem(porc);
                        listaFotos.get(i).setUri(Uri.parse("file://" + listaFotos.get(i).getCaminhoImagem()).toString());

                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(compressedImage)));


                    } catch (OutOfMemoryError e2) {
                        exibirMensagem(getString(R.string.erro_memoria));
                        erro = true;
                        break;
                    }

                } catch (IOException e) {
                    erro = true;
                    e.printStackTrace();
                    exibirMensagem(e.getMessage());
                    break;
                } catch (NullPointerException e3) {
                    erro = true;
                    exibirMensagem(getString(R.string.erro_memoria));
                    e3.printStackTrace();
                    break;
                }
            }
            atualizarLista(erro);
            return null;
        }

        private void atualizarLista(final boolean erro) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!erro) {
                        mAdapter.notifyDataSetChanged();
                        materialDialogCompress.dismiss();
                        Toast.makeText(getBaseContext(), getString(R.string.fotos_reduzidas), Toast.LENGTH_SHORT).show();

                        showInterstial();
                    }
                }
            });
        }

        private void exibirMensagem(final String mensagem) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), mensagem, Toast.LENGTH_SHORT).show();
                    materialDialogCompress.dismiss();
                }
            });
        }
    }

    public IabHelper.QueryInventoryFinishedListener mQueryInventoryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {

        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            Log.i("Android", "onQueryInventoryFinished()");

            if (result.isFailure()) {
                Log.i("Android", "onQueryInventoryFinished() : FAIL : " + result);

            } else if (inv != null) {
                if (inv.hasDetails(PRODUCT_ID)) {
                    Log.i("Android", inv.getSkuDetails(PRODUCT_ID).getSku().toUpperCase());
                    Log.i("Android", "Sku: " + inv.getSkuDetails(PRODUCT_ID).getSku());
                    Log.i("Android", "Title: " + inv.getSkuDetails(PRODUCT_ID).getTitle());
                    Log.i("Android", "Type: " + inv.getSkuDetails(PRODUCT_ID).getType());
                    Log.i("Android", "Price: " + inv.getSkuDetails(PRODUCT_ID).getPrice());
                    Log.i("Android", "Description: " + inv.getSkuDetails(PRODUCT_ID).getDescription());
                    Log.i("Android", "Status purchase: " + (inv.hasPurchase(PRODUCT_ID) ? "COMPRADO" : "NÃO COMPRADO"));
                    Log.i("Android", "-------------------------------------");

                    if(inv.hasPurchase(PRODUCT_ID)){
                        ((MyApplication) getApplication()).premium = true;

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean("premium", true);
                        editor.commit();

                        if(((MyApplication) getApplication()).premium){
                            itemSelecionarMenuToolbar(false);
                        }

                        Log.i("Android", "APP PREMIUM COMPRADO IN BOOT");

                        mBinding.adView.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

    public IabHelper.OnIabPurchaseFinishedListener mIabPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            Log.i("Android", "onIabPurchaseFinished()");

            if (result.isFailure()) {
                Log.i("Android", "onIabPurchaseFinished() : FAIL : " + result);

                if(result.toString().contains("Unable to buy item")){

                    Toast.makeText(getApplicationContext(), "Você já é um usuario Premium", Toast.LENGTH_SHORT).show();
                }

                return;
            } else {
                ((MyApplication) getApplication()).premium = true;

                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("premium", true);
                editor.commit();

                Log.i("Android", "App Premium Comprado");
                Log.i("Android", info.getSku().toUpperCase());
                Log.i("Android", "Order ID: " + info.getOrderId());
                Log.i("Android", "DeveloperPayload: " + info.getDeveloperPayload());
            }
        }
    };

    public void instanceIabHelper(final boolean frag){
        mHelper = ((MyApplication) getApplication()).getmHelper();

        try{

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mHelper == null) {
                        mHelper = new IabHelper(ListaFotosActivity.this, base64EncodedPublicKey);
                        ((MyApplication) getApplication()).setmHelper(mHelper);

                        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                            @Override
                            public void onIabSetupFinished(IabResult result) {
                                Log.i("Android", "onIabSetupFinished()");

                                if (result.isFailure()) {
                                    Log.i("Android", "onIabSetupFinished() : FAIL : " + result);
                                } else {
                                    Log.i("Android", "onIabSetupFinished() : SUCCESS");

                                    List<String> productsIds = new ArrayList<>();
                                    productsIds.add(PRODUCT_ID);


                                    mHelper.queryInventoryAsync(true, productsIds, mQueryInventoryFinishedListener);
                                }
                            }
                        });

                        if(frag){
                            mHelper.launchPurchaseFlow(ListaFotosActivity.this, "remover_anuncio", 1002, mIabPurchaseFinishedListener);
                        }
                    }
                }
            }).start();

        }catch(NullPointerException e){ }
    }
}
