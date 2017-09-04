package jajimenez.workpage.logic;

import java.util.Comparator;

import jajimenez.workpage.data.model.Country;

public class CountryComparator implements Comparator<Country> {
    public int compare(Country a, Country b) {
        int result = -1;

        if (a != null && b != null) {
            String name1 = a.getName();
            String name2 = b.getName();

            if (name1 != null && !name1.isEmpty() && name2 != null && !name2.isEmpty()) {
                result = name1.compareTo(name2);
            }
        }

        return result;
    }
}