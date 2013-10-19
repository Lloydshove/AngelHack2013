package photoapp;


public class Spot implements Comparable<Spot> {

	private double x;
	
	private double y;
	
	private int count;

	private String photos;
	
	public Spot() {
	}

	public Spot(double x, double y, int count, String photos) {
		super();
		this.x = x;
		this.y = y;
		this.count = count;
		this.photos = photos;
	}

	public String getPhotos() {
		return photos;
	}

	public void setPhotos(String photos) {
		this.photos = photos;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "Spot [x=" + x + ", y=" + y + ", count=" + count + ", photos="+ photos + "]";
	}

	@Override
	public int compareTo(Spot o) {
		if(o.getCount() > this.getCount()) return 1;
		if(o.getCount() < this.getCount()) return -1;
		return 0;
	}

}
