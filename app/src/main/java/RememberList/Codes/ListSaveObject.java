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
}
