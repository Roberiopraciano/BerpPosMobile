package br.com.berpsistemas.BerpPOSMobile.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ListaItens {

    /**
     * Mapeia o booleano "ownsObjects" do JSON.
     */
    @SerializedName("ownsObjects")
    private boolean ownsObjects;

    /**
     * Mapeia o array "listHelper" do JSON diretamente para uma lista de ItemFields.
     */
    @SerializedName("listHelper")
    private List<ItemFields> listHelper;

    public ListaItens() {
    }

    /** Retorna se ownsObjects veio como true/false no JSON. */
    public boolean isOwnsObjects() {
        return ownsObjects;
    }

    /** Define o valor de ownsObjects. */
    public void setOwnsObjects(boolean ownsObjects) {
        this.ownsObjects = ownsObjects;
    }

    /** Retorna a lista de ItemFields desserializada do JSON. */
    public List<ItemFields> getListHelper() {
        return listHelper;
    }

    /** Define a lista de ItemFields (usado internamente pelo GSON). */
    public void setListHelper(List<ItemFields> listHelper) {
        this.listHelper = listHelper;
    }
}
