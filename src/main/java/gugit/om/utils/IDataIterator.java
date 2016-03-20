package gugit.om.utils;

public interface IDataIterator <E>{

	/***
	 * returns cursor to a start position.
	 */
	void reset();

	/***
	 * returns current cursor position
	 */
	int getPosition();
	
	/***
	 * returns an item from a current cursor position
	 */
	E peek();
	
	/***
	 * moves cursor to a next position
	 */
	void next();

	/***
	 * same as E e=peek(); next(); return e;
	 */
	E getNext();

	/***
	 * returns item from a specified position, does not change cursor location
	 */
	E peek(int position);
	
	/**
	 * returns num of items accessible
	 */
	int length();
	
	/**
	 * returns cursor_position>=length;
	 */
	boolean isFinished();
	
	/**
	 * returns 0 <= position < length
	 */
	boolean isOutOfBounds(int position);
	
}
