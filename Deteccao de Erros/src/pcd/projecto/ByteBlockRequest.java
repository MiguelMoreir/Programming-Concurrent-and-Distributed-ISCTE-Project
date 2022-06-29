package pcd.projecto;


public class ByteBlockRequest implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int startIndex;
	int lenght;
	

	public ByteBlockRequest(int startIndex, int lenght) {
		super();
		this.startIndex = startIndex;
		this.lenght = lenght;
	}
	
	

	public int getStartIndex() {
		return startIndex;
	}



	public int getLenght() {
		return lenght;
	}



	@Override
	public String toString() {
		return "ByteBlockRequest [startIndex=" + startIndex + ", lenght=" + lenght + "]";
	}
	
}
