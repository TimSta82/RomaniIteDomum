package com.example.tim.romaniitedomum.Util;

import android.util.Log;

import com.example.tim.romaniitedomum.artefact.Artefact;

import java.util.List;

public class FilterHelper {

    private static final String TAG = "FilterHelper";

    public static FilterHelper instance;
    private String artefactName;
    private String category;
    private String ageFrom;
    private String ageTo;
    private List<Artefact> filteredArtefactList;
    private BcAc annoDominiFrom;
    private BcAc annoDominiTo;
    private boolean isListFilterSet;
    private boolean isMapFilterSet;

    private FilterHelper() {

    }

    public static FilterHelper getInstance() {
        if (instance == null) {
            Log.d(TAG, "getInstance: instance created");
            instance = new FilterHelper();
        }
        return instance;
    }

    public String getArtefactName() {
        return artefactName;
    }

    public void setArtefactName(String artefactName) {
        this.artefactName = artefactName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAgeFrom() {
        return ageFrom;
    }

    public void setAgeFrom(String ageFrom) {
        this.ageFrom = ageFrom;
    }

    public String getAgeTo() {
        return ageTo;
    }

    public void setAgeTo(String ageTo) {
        this.ageTo = ageTo;
    }

    public List<Artefact> getFilteredArtefactList() {
        return filteredArtefactList;
    }

    public void setFilteredArtefactList(List<Artefact> filteredArtefactList) {
        this.filteredArtefactList = filteredArtefactList;
    }

    public BcAc getAnnoDominiFrom() {
        return annoDominiFrom;
    }

    public void setAnnoDominiFrom(BcAc annoDominiFrom) {
        this.annoDominiFrom = annoDominiFrom;
    }

    public BcAc getAnnoDominiTo() {
        return annoDominiTo;
    }

    public void setAnnoDominiTo(BcAc annoDominiTo) {
        this.annoDominiTo = annoDominiTo;
    }

    public boolean isListFilterSet() {
        return isListFilterSet;
    }

    public void setListFilterSet(boolean listFilterSet) {
        isListFilterSet = listFilterSet;
    }

    public boolean isMapFilterSet() {
        return isMapFilterSet;
    }

    public void setMapFilterSet(boolean mapFilterSet) {
        isMapFilterSet = mapFilterSet;
    }

    @Override
    public String toString() {
        return "FilterHelper{" +
                "artefactName='" + artefactName + '\'' +
                ", category='" + category + '\'' +
                ", ageFrom=" + ageFrom +
                ", ageTo=" + ageTo +
                ", annoDominiFrom=" + annoDominiFrom +
                ", annoDominiTo=" + annoDominiTo +
                ", isListFilterSet=" + isListFilterSet +
                ", isMapFilterSet=" + isMapFilterSet +
                '}';
    }
}
