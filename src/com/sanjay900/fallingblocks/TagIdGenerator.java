package com.sanjay900.fallingblocks;

public class TagIdGenerator {

	    private volatile int SHARED_ID = Short.MAX_VALUE;
	    private volatile int SHARED_SIMPLE_ID = Short.MIN_VALUE;

	    public int nextId(int counter) {
	        return nextId(counter, false);
	    }

	    public int nextSimpleId(int counter) {
	        return nextId(counter, true);
	    }

	    private int nextId(int counter, boolean simple) {
	        int firstId = simple ? ++SHARED_SIMPLE_ID : ++SHARED_ID;
	        if (simple) {
	            for (int i = 0; i <= (counter * 4); i++) {
	                if ((firstId + i) > 0) {
	                    SHARED_SIMPLE_ID = Short.MIN_VALUE;
	                    return nextId(counter, true);
	                }
	            }
	            SHARED_SIMPLE_ID += counter * 4;
	        } else {
	            SHARED_ID += counter * 4;
	        }
	        return firstId;
	    }
}
