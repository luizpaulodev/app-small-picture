package br.com.sm.smallpicture;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import br.com.sm.smallpicture.databinding.ActivityMainBinding;
import id.zelory.compressor.Compressor;
import id.zelory.compressor.FileUtil;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 0;

    private ActivityMainBinding mBinding;

    private Bitmap imagemEntrada;

    private File actualImage;
    private File compressedImage;

    private ImageView img;
    private Bitmap.CompressFormat formato;

    private int width;
    private int heigth;
    private double divisor = 1.5;
    private int qualidade = 75;
    private double[] values = {1.5, 2.0, 2.5, 3, 3.5, 4, 4.5, 5,5.5};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mBinding.sbQualidade.setProgress(qualidade);
        mBinding.txtBarraQualidade.setText(qualidade + "");
        mBinding.txtBarraReduzir.setText("1.5x");

        mBinding.sbReduzir.setMax(7);

        formato = Bitmap.CompressFormat.JPEG;

        mBinding.sbQualidade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qualidade = progress;

                mBinding.txtBarraQualidade.setText(qualidade + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        mBinding.sbReduzir.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mBinding.txtBarraReduzir.setText(values[progress] + "x");
                divisor = values[progress];

                if(actualImage != null){
                    mBinding.dimensoesSaida.setText( (int)(imagemEntrada.getWidth() / divisor)  + "x" + (int) (imagemEntrada.getHeight() / divisor));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            try {

                actualImage = FileUtil.from(this, data.getData());

                imagemEntrada = BitmapFactory.decodeFile(actualImage.getAbsolutePath());

                mBinding.imgEntrada.setImageBitmap(imagemEntrada);
                mBinding.txtSizeImageEntrada.setText(String.format("Size : %s", getReadableFileSize(actualImage.length())));

                mBinding.dimensoesEntrada.setText(imagemEntrada.getWidth() + "x" + imagemEntrada.getHeight());
                mBinding.dimensoesSaida.setText( (int)(imagemEntrada.getWidth() / divisor)  + "x" + (int) (imagemEntrada.getHeight() / divisor));
                //clearImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickSelectImage(View view) {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public void clickCompress(View view) {
        if (actualImage == null) {
            //showError("Please choose an image!");
        } else {

            File direct = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SmallPictures");

            if(!direct.exists()) {
                if(direct.mkdir());
            }

            width = (int) (imagemEntrada.getWidth() / divisor);
            heigth = (int)(imagemEntrada.getHeight() / divisor);

            // Compress image in main thread using custom Compressor
            compressedImage = new Compressor.Builder(this)
                    .setMaxWidth(width)
                    .setMaxHeight(heigth)
                    .setQuality(qualidade)
                    .setCompressFormat(formato)
                    .setDestinationDirectoryPath(direct.getPath())
                    .build()
                    .compressToFile(actualImage);

            setCompressedImage();

        }
    }

    private void setCompressedImage() {

        Bitmap imagem = BitmapFactory.decodeFile(compressedImage.getAbsolutePath());

        mBinding.imgSaida.setImageBitmap(imagem);
        mBinding.txtSizeImageSaida.setText(String.format("Size : %s", getReadableFileSize(compressedImage.length())));
        mBinding.dimensoesSaida.setText(imagem.getWidth() + "x" + imagem.getHeight());

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(compressedImage)));
    }

    public void clickFormato(View view) {

        switch (view.getId()){
            case R.id.rbJPEG:
                formato = Bitmap.CompressFormat.JPEG;
                break;

            case R.id.rbWEBP:
                formato = Bitmap.CompressFormat.WEBP;
                break;
        }
    }
}
