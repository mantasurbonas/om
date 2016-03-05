package gugit.om.utils;


public class ArrayIterator<E> {

	public E [] data;
	public int offset = 0;
	
	public ArrayIterator(E[] array){
		setData(array);
	}

	public ArrayIterator() {
	}

	public E peek() {
		return data[offset];
	}

	public E getNext(){
		return data[offset++];
	}
	
	public void next() {
		offset ++;
	}

	public void reset() {
		offset = 0;
	}

	public int length() {
		return data.length;
	}

	public boolean isFinished() {
		return offset == data.length;
	}

	public void setData(E[] array) {
		this.data = array;
		this.offset = 0;
	}

}
