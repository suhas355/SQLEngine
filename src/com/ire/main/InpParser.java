package com.ire.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class InpParser {
	private static final String OUTPUT_FORMAT = "UTF-8";
	private static final String OUTPUT_FILE = "./doc/index.txt";
	private static final String STOP_WORD_FILE="./doc/stopwords.txt";
	private static final String INP_FILE="./doc/sample.xml";
	private static final String TEXT="text";
	private static final String TITLE="title";
	private static final String PAGE="page";
	private static final String ID="id";
	public static final String COMMENT_PAT = "==";
	
	private static final String[] UNWANTED_PTRN={"\\<!--.*?--\\>","\\[\\[Image:.*\\]\\]","\\[\\[File:.*\\]\\]","\\[\\[...?:.*\\]\\]"}; 
	private Map<String, Boolean> stopWordMap;
	private SaxParserHandler saxParserHandler;
	private Map<String, WordFrequency> wordMap;
	private Map<String,StringBuilder> indexMap;
	private String curId;
	private WordFrequency wordObj;
	private StringBuilder s;
	private WordStemmer stem;
	private static BigInteger  docId;
	private BigInteger tempBigId;
		
	public InpParser(){
		saxParserHandler = new SaxParserHandler();
		stopWordMap=new HashMap<String, Boolean>();
		indexMap = new TreeMap<String,StringBuilder>();
		wordMap = new HashMap<String, WordFrequency>();
		s=new StringBuilder();
		stem=  new WordStemmer();
	}
	
	
	public class SaxParserHandler extends DefaultHandler{
		private static final String TITLE_PATTREN = "[^a-z0-9-]";
		private static final String TEXT_PATTREN = "[^a-z]";
		private boolean isTitle;
		private boolean isId;
		private boolean isText;
		public boolean prevTagIsTitle;
		public SaxParserHandler(){
			isTitle=isId=isText=prevTagIsTitle=false;
		}
		
		@Override
		public void startElement(String uri,String localName,String tagName,
				Attributes attributed) throws SAXException{
			if(prevTagIsTitle && !ID.equals(tagName)){
				prevTagIsTitle=false;
			}
			//System.out.println("st");
			if(TEXT.equals(tagName)){
				isText=true;
				s=new StringBuilder();
			}else if(TITLE.equals(tagName)){
				isTitle=true;
				s=new StringBuilder();
			}else if(ID.equals(tagName) && prevTagIsTitle){
				isId=true;
			}else if(PAGE.equals(tagName)){
				wordMap.clear();
			}
			
		}
		
		@Override
		public void endElement(String uri,String localName,String tagName)
		throws SAXException{
			
			if(TEXT.equals(tagName)){
				isText=false;
				processText();	
				
			}else if(TITLE.equals(tagName)){
				isTitle=false;
				prevTagIsTitle=true;
				processTitleText();
			}else if(ID.equals(tagName)){
				isId=false;
			}else if(PAGE.equals(tagName)){
				copyToIndexMap();
			}
			
		}
		
		public boolean validateType(String temp){
			
			if(temp.contains(TextType.INFO_BOX_PAT)){
				TextType.CURRENT_TAG=TextType.INFO_BOX;
				return false;
			}
			if(temp.equals(TextType.INFO_BOX_PAT_END)){
				TextType.CURRENT_TAG=TextType.NORMAL;
					return false;
					
			}
			if(temp.contains(TextType.CATEGORY_PAT)){
				TextType.CURRENT_TAG=TextType.CATEGORY;
				return false;
			}
			
			if(temp.contains(TextType.EXTERNAL_LINK_PAT)){
				TextType.CURRENT_TAG=TextType.EXTERNAL_LINK;
				return false;
			}
			if(temp.contains(TextType.REFERENCES_PAT)){
				TextType.CURRENT_TAG=TextType.REFERENCES;
				return false;
			}
			if(temp.startsWith(COMMENT_PAT) && temp.endsWith(COMMENT_PAT)){
				//System.out.println(temp);
				return false;
			}
				return true;
		}
		
		
		
		@Override
		public void characters(char ch[],int start,int length)
		throws SAXException{
			if(isText){
				//System.out.println("haha");
				//StringBuilder s=new StringBuilder();
				s.append(ch, start, length);
				
			}else if(isTitle){
				s.append(ch, start, length);
				
			}else if(isId){
				StringBuilder s=new StringBuilder();
				s.append(ch, start, length);
				//System.out.println("id is:"+s);
				curId= s.toString();
				//System.out.println("id is:"+curId);
				tempBigId = new BigInteger(curId);
				tempBigId=tempBigId.subtract(docId);
				
				WordFrequency.id=tempBigId.toString();
				//System.out.println("id is:"+WordFrequency.id);
				
			}
		}

		private void processTitleText() {
			StringTokenizer tkn = new StringTokenizer(s.toString()," \n");
			String temp,temp1;
			while(tkn.hasMoreElements()){
					
					temp=tkn.nextToken();
					temp=temp.toLowerCase();
					temp=temp.replaceAll(TITLE_PATTREN, " ");
					
					StringTokenizer newTkn = new StringTokenizer(temp," ");
					while(newTkn.hasMoreElements()){
						temp1=newTkn.nextToken();
						if(!stopWordMap.containsKey(temp1)){
					
							if(wordMap.containsKey(temp1)){
								wordObj=wordMap.get(temp1);
								wordObj.titleCount++;
							}else{
								wordObj = new WordFrequency(temp1);
								wordObj.titleCount++;
								wordMap.put(temp1,wordObj);
							}
						}
					}
			}
		}

		private void processText() {
			String data = s.toString();
			for(String ptrn: UNWANTED_PTRN )
				data=data.replaceAll(ptrn, "");
			StringTokenizer tkn = new StringTokenizer(data," \n");
			String temp,temp1;
			while(tkn.hasMoreElements()){
				temp=tkn.nextToken();
				if("".equals(temp))
					continue;
				//System.out.println(temp+"---------");
				if(temp!=null && validateType(temp) && temp.length()>2){
					temp=temp.toLowerCase();
					temp=temp.replaceAll(TEXT_PATTREN, " ");
					StringTokenizer newTkn = new StringTokenizer(temp," ");
					while(newTkn.hasMoreElements()){
						temp1=newTkn.nextToken();
						//if(temp1.contains(UNDERSCORE))
						//	continue;
						//
						if(!stopWordMap.containsKey(temp1)){
							temp1=stem.stemIt(temp1);
							handleTextWord(temp1);
						}
					}
				}	
			}
		}
		
	}
	
	public void handleTextWord(String text){
		
		wordObj=wordMap.get(text);
		if(wordObj==null)
			wordObj=new WordFrequency(text);
		
		switch (TextType.CURRENT_TAG){
		case TextType.INFO_BOX: wordObj.infoBoxCount++;
								break;
		case TextType.CATEGORY: wordObj.categoryCount++;
								break;
		case TextType.REFERENCES: wordObj.referenceCount++;
								break;
		case TextType.EXTERNAL_LINK: wordObj.externalLinkCount++;
								break;
		default:			wordObj.textCount++;
							break;
		}
		wordMap.put(text, wordObj);
	}
	
	public void copyToIndexMap() {
		//String generate
		
		for(Map.Entry<String, WordFrequency> entry:wordMap.entrySet()){
			String key =entry.getKey();
			StringBuilder builder = indexMap.get(key);
			//System.out.println(key+"--"+builder);
			if(builder==null){
				builder = new StringBuilder();
				//builder.append(COLON);
			}
			builder.append(entry.getValue().toString());
			indexMap.put(key, builder);
			
		}
	}
	
	public void populateStopWord() throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(new File(STOP_WORD_FILE)));
		String line;
		while((line=br.readLine())!=null){
			stopWordMap.put(line, true);
		}
		br.close();
		
		
	}
	
	public void printIt(){
		for(Map.Entry<String,StringBuilder> entry:indexMap.entrySet()){
			System.out.println(entry.getKey()+entry.getValue());
		}
	}
	public void putItInFile() throws Exception{
		PrintWriter writer = new PrintWriter(OUTPUT_FILE, OUTPUT_FORMAT);
		String term, value;
		for (Entry<String, StringBuilder> entry : indexMap.entrySet()) {

		term = entry.getKey();
		value = entry.getValue().toString();
		writer.println(term+value);
		}
		writer.close();
	}
	public void getFirstDocId() {
		BufferedReader br=null;
		try{
			br = new BufferedReader(new FileReader(INP_FILE));
			String line;
			while((line=br.readLine())!=null){
				if(line.contains("<id>")){
					line=line.replaceAll("</?id>", "").replaceAll("\\s", "");
					//System.out.println(line);
					docId = new BigInteger(line);
					break;
				}
			}
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(br!=null){
				try{
					br.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
	}
	public static void main(String[] args){
		InpParser inpParser = new InpParser();
		try{
			inpParser.getFirstDocId();
			SAXParserFactory fac = SAXParserFactory.newInstance();
			SAXParser sPars = fac.newSAXParser();
			long stTime=System.currentTimeMillis();
			inpParser.populateStopWord();
			//System.out.println("parse");
			sPars.parse(INP_FILE,inpParser.saxParserHandler);
			inpParser.putItInFile();
			long enTime=System.currentTimeMillis();
			//inpParser.printIt();
			
			System.out.println("time:"+(enTime-stTime)/1000.0 +"secs");
			//System.out.println(count);
			
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}catch(ParserConfigurationException e){
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
	
	
final class TextType{
	
	public static final int NORMAL = 0;
	public static final int INFO_BOX = 1;
	public static final int REFERENCES = 2;
	public static final int CATEGORY = 3;
	public static final int EXTERNAL_LINK = 4;
	public static int CURRENT_TAG =0;
	
	public static final String INFO_BOX_PAT = "{{Infobox";
	public static final String REFERENCES_PAT = "==References";
	public static final String CATEGORY_PAT = "[[Category";
	public static final String EXTERNAL_LINK_PAT = "==External";
	
	public static final String INFO_BOX_PAT_END = "}}";
	public static final String CATEGORY_PAT_END = "]]";
	
	
}
