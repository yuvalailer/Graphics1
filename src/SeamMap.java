
public class SeamMap implements Comparable<SeamMap>{
	int value;
	int index;
	int[] way;
	
	public SeamMap(int value, int index, int[] way) {
		this.value = value;
		this.index = index;
		this.way = way;
	}

	@Override
	public int compareTo(SeamMap o) {
		int compareVal = ((SeamMap) o).value;
		return (this.value - compareVal);
	}

}
