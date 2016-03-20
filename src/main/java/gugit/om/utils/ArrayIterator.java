package gugit.om.utils;


public class ArrayIterator<E> implements IDataIterator<E>{

	public E [] data;
	public int cursorPosition = 0;

	public ArrayIterator() {
	}
	
	public ArrayIterator(E[] array){
		setData(array);
	}

	public void setData(E[] array) {
		this.data = array;
		this.cursorPosition = 0;
	}

	@Override
	public E peek() {
		return data[cursorPosition];
	}

	@Override
	public E peek(int i) {
		return data[i];
	}
	
	@Override
	public E getNext(){
		return data[cursorPosition++];
	}
	
	@Override
	public void next() {
		cursorPosition ++;
	}

	@Override
	public void reset() {
		cursorPosition = 0;
	}

	@Override
	public int length() {
		return data.length;
	}

	@Override
	public boolean isFinished() {
		return cursorPosition == data.length;
	}
	
	@Override
	public int getPosition() {
		return cursorPosition;
	}

	@Override
	public boolean isOutOfBounds(int position) {
		return position<0 || position>=data.length;
	}

}
