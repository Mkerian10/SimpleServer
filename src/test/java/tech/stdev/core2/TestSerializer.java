package tech.stdev.core2;

import java.io.Serializable;
import java.util.Random;

public class TestSerializer implements Serializable{
	
	
	int i = new Random().nextInt();
	
	String s = "Hi World!";
	
	@Override
	public boolean equals(Object obj){
		return ((TestSerializer)obj).i == this.i;
	}
}
