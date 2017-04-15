import java.util.Comparator;

public class ColumnComparator implements Comparator<int[][][]> {
	        @Override
	        public int compare(int[][][] o1, int[][][] o2) {
	            return o1[0][0][0] - o2[0][0][0];
	        }
	    }

