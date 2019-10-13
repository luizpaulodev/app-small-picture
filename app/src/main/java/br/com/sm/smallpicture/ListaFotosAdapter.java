package br.com.sm.smallpicture;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DecimalFormat;
import java.util.List;

import br.com.sm.smallpicture.databinding.ItemListaFotosBinding;

/**
 * Created by Luiz Paulo Oliveira on 07/01/2017.
 */

public class ListaFotosAdapter extends RecyclerView.Adapter<ListaFotosAdapter.ListaFotosHolder> {

    private List<Imagens> mList;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ImageLoader mImageLoader;

    private OnFotoClickListener mListener;

    public ListaFotosAdapter(Context context, List<Imagens> lista, ImageLoader mImageLoader, OnFotoClickListener mListener){
        this.mContext = context;
        this.mList = lista;
        this.mImageLoader = mImageLoader;
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mListener = mListener;
    }

    @Override
    public ListaFotosHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = mLayoutInflater.inflate(R.layout.item_lista_fotos, parent, false);
        final ListaFotosHolder holder = new ListaFotosHolder(v);

        holder.mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.setOnFotoClickListener(holder);
                }
            }
        });

        holder.mBinding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(mListener != null){
                    mListener.setOnFotoLongClickListener(holder);
                    return true;
                }

                return false;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(final ListaFotosHolder holder, final int position) {

        if(mList.get(position).isSeparator()){


        } else {
            Imagens img = mList.get(position);

            holder.mBinding.txtDimensoes.setText(String.format("%sx%s", img.getWidth(), img.getHeigth()));
            holder.mBinding.txtTamanho.setText(getReadableFileSize(img.getTamanho()));
            holder.mBinding.txtNumeroFoto.setText(mContext.getString(R.string.imagem, (position + 1)));

            holder.mBinding.imagemGaleria.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.mBinding.txtPorcentagem.setText(img.getPorcentagem() + "%");
            holder.mBinding.txtPorcentagem.setTextColor(img.getPorcentagem() < 0 ? Color.GREEN : img.getPorcentagem() > 0 ? Color.RED : Color.BLACK);
            mImageLoader.displayImage(mList.get(position).getUri().toString(), holder.mBinding.imagemGaleria, null, null);

            holder.mBinding.imagemSelecionada.setVisibility(img.isSelecionado() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public interface OnFotoClickListener{
        void setOnFotoClickListener(ListaFotosHolder holder);
        void setOnFotoLongClickListener(ListaFotosHolder holder);
    }

    public class ListaFotosHolder extends RecyclerView.ViewHolder{

        public ItemListaFotosBinding mBinding;

        public ListaFotosHolder(View itemView) {
            super(itemView);

            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}
