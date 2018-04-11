import java.util.ArrayList;

public class Metadata {
    private String name;
    private int numberOfPages;
    private int pageSize;
    private int size;
    private ArrayList<Page> pages;

    public Metadata(String n, int s, ArrayList<Page> p){
        name = n;
        numberOfPages = p.size();
        pageSize = 1024;
        size = s;
        pages = p;
    }

}
