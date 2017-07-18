package jajimenez.workpage;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File> {
    public int compare(File lhs, File rhs) {
        int result = -1;

        if (lhs != null && rhs != null) {
            String lhsName = lhs.getName();
            String rhsName = rhs.getName();

            if (lhsName != null && rhsName != null) result = lhsName.compareTo(rhsName);
        }

        return result;
    }
}
