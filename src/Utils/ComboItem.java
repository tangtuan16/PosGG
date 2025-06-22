package Utils;

public class ComboItem {
    private String value;
    private String displayText;

    public ComboItem(String value, String displayText) {
        this.value = value;
        this.displayText = displayText;
    }

    @Override
    public String toString() {
        return displayText;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ComboItem other = (ComboItem) obj;
        return value.equals(other.value);
    }

    public String getValue() {
        return value;
    }
}
