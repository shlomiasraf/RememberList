package RememberList.Codes;

public class ListSharedObject
{
    private final int saves;
    private final String listName;

    private final String[] categories;

    public ListSharedObject(int saves, String listName, String[] categories)
    {
        this.saves = saves;
        this.listName = listName;
        this.categories = categories;
    }

    public int getSaves()
    {
        return saves;
    }

    public String getListName()
    {
        return listName;
    }
    public String[] getCategories()
    {
        return categories;
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
        ListSharedObject other = (ListSharedObject) obj;

        // Compare fields
        if (saves == other.getSaves() && listName.equals(other.getListName()) && categories.length == other.getCategories().length)
        {
            String[] otherCategories = other.getCategories();
            for(int i = 0; i < categories.length; i++)
            {
                if(!categories[i].equals(otherCategories[i]))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    @Override
    public String toString()
    {
        // Start with the list name followed by a comma and space.
        String toString = listName + ", ";
         // Append each category to the string, separated by a comma and space.
        for(String category : categories)
        {
            toString += category + ", ";
        }
        // Append the word "שמירות" (meaning "saves" in Hebrew) and the number of saves.
        toString += "שמירות " + String.valueOf(saves);
        return toString;
    }

}
