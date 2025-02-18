package RememberList.Codes;

public class ListSaveObject
{
    private final int saves;
    private final String listName;

    public ListSaveObject(int saves, String listName)
    {
        this.saves = saves;
        this.listName = listName;
    }

    public int getSaves()
    {
        return saves;
    }

    public String getListName()
    {
        return listName;
    }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true; // If both references point to the same object
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false; // Ensure the object is not null and is of the same class
        }

        // Safe type casting
        ListSaveObject other = (ListSaveObject) obj;

        // Compare fields
        return saves == other.getSaves() && listName.equals(other.getListName());
    }
}
