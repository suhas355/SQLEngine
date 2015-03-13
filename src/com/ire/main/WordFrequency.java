package com.ire.main;

public class WordFrequency {
	public String word;
	public int titleCount;
	public int textCount;
	public int referenceCount;
	public int infoBoxCount;
	public int externalLinkCount;
	public int categoryCount;
	public static String id;
	private static final String D = "|";
	private static final String T = "t";
	private static final String B = "b";
	private static final String C = "c";
	private static final String I = "i";
	private static final String R = "r";
	private static final String E = "e";
	public WordFrequency(String word){
		this.word=word;
		titleCount=textCount=referenceCount=infoBoxCount=externalLinkCount=categoryCount=0;
		
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(D);
		str.append(id);
		if(titleCount!=0){
			str.append(T);
			str.append(titleCount);
		}
		if(textCount!=0){
			str.append(B);
			str.append(textCount);
		}
		if(infoBoxCount!=0){
			str.append(I);
			str.append(infoBoxCount);
		}
		if(categoryCount!=0){
			str.append(C);
			str.append(categoryCount);
		}
		if(referenceCount!=0){
			str.append(R);
			str.append(referenceCount);
		}
		if(externalLinkCount!=0){
			str.append(E);
			str.append(externalLinkCount);
		}
		//str.append("|");
		return str.toString();
	}
}
