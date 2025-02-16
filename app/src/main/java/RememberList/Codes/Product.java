package RememberList.Codes;

public class Product
{
    String name; // The name of the product
    boolean box; // The checkbox state for the product (checked or unchecked)

    // Constructor to initialize the product with a description and a checkbox state
    Product(String _describe, boolean _box)
    {
        this.name = _describe; // Set the name of the product
        this.box = _box; // Set the checkbox state for the product
    }
    // Getter for isChecked
    public boolean isChecked() {
        return box;
    }

    // Setter for isChecked (optional, if needed)
    public void setChecked(boolean isChecked) {
        this.box = isChecked;
    }
}
