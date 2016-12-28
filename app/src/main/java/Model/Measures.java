package Model;

public class Measures
{
    private String label;
    private String eqv;
    private String qty;
    private String value;

    public Measures(String label, String eqv, String qty, String value) {
        this.label = label;
        this.eqv = eqv;
        this.qty = qty;
        this.value = value;
    }

    public Measures() {

    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getEqv() {
        return eqv;
    }

    public void setEqv(String eqv) {
        this.eqv = eqv;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
