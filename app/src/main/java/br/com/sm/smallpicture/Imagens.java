package br.com.sm.smallpicture;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Luiz Paulo Oliveira on 07/01/2017.
 */

public class Imagens implements Serializable {

    private String caminhoImagem;
    private String uri;
    private int width;
    private int heigth;
    private String name;
    private long Tamanho;
    private int porcentagem;
    private boolean selecionado;

    public Imagens() { }

    public Imagens(boolean separator) { this.separator = separator; }

    private boolean separator;

    public boolean isSeparator() {
        return separator;
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    public int getPorcentagem() {
        return porcentagem;
    }

    public boolean isSelecionado() {
        return selecionado;
    }

    public void setSelecionado(boolean selecionado) {
        this.selecionado = selecionado;
    }

    public void setPorcentagem(int porcentagem) {
        this.porcentagem = porcentagem;
    }

    public long getTamanho() {
        return Tamanho;
    }

    public void setTamanho(long tamanho) {
        Tamanho = tamanho;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeigth() {
        return heigth;
    }

    public void setHeigth(int heigth) {
        this.heigth = heigth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCaminhoImagem() {
        return caminhoImagem;
    }

    public void setCaminhoImagem(String caminhoImagem) {
        this.caminhoImagem = caminhoImagem;
    }
}
