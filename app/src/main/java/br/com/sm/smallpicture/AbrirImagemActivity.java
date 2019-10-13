package br.com.sm.smallpicture;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.text.DecimalFormat;

import br.com.sm.smallpicture.databinding.ActivityAbrirImagemBinding;

public class AbrirImagemActivity extends AppCompatActivity {

    private ActivityAbrirImagemBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_abrir_imagem);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Imagens img = (Imagens) getIntent().getSerializableExtra("Imagem");

        setSupportActionBar(mBinding.defToolbar);
        getSupportActionBar().setTitle(img.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bitmap bmp = BitmapFactory.decodeFile(img.getCaminhoImagem());

        SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) findViewById(R.id.imgAbrirFoto);
        imageView.setImage(ImageSource.bitmap(bmp));
        imageView.setZoomEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
