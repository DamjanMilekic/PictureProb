package Model;

import java.util.ArrayList;
import java.util.List;

import Model.Measures;

public class Nutrients
{
private int nutr_id;
    private String name;
    private String group;
    private String unit;
    private String value;
    private List<Measures> measuresDetails;

    public Nutrients(int nutr_id, String name, String group, String unit, String value, List<Measures> measuresDetails) {
        this.nutr_id = nutr_id;
        this.name = name;
        this.group = group;
        this.unit = unit;
        this.value = value;
        this.measuresDetails = measuresDetails;
    }

    public int getNutr_id() {
        return nutr_id;
    }

    public void setNutr_id(int nutr_id) {
        this.nutr_id = nutr_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Measures> getMeasuresDetails() {
        return measuresDetails;
    }

    public void setMeasuresDetails(List<Measures> measuresDetails) {
        this.measuresDetails = measuresDetails;
    }

    public Nutrients() {

    }
}
