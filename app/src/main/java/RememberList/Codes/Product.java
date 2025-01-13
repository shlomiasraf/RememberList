package RememberList.Codes;

public class Product
{
    String name; // The name of the product
    boolean box; // The checkbox state for the product (checked or unchecked)

    // Constructor to initialize the product with a description and a checkbox state
    Product(String _describe, boolean _box)
    {
        name = _describe; // Set the name of the product
        box = _box; // Set the checkbox state for the product
    }
}
