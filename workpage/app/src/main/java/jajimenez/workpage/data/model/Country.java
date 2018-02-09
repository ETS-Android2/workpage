package jajimenez.workpage.data.model;

import java.lang.Comparable;
import java.util.Locale;

public class Country extends Entity implements Comparable<Country> {
    private String code;

    public Country() {
        super();
        code = "";
    }

    public Country(String code) {
        super();
        this.code = code;
    }

    public Country(long id, String code) {
        super(id);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        String name = "";

        try {
            Locale locale = new Locale("", code);
            name = locale.getDisplayCountry();
        }
        catch (Exception e) {
            // Nothing to do
        }

        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof Country && compareTo((Country) other) == 0);
    }

    public int compareTo(Country other) {
        int result = 1;
        if (other != null) result = (this.getName()).compareTo(other.getName());

        return result;
    }
}
