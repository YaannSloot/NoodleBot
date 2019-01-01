package main.IanSloat.thiccbot.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TBMLSettingsParser {

	private static final Logger logger = LoggerFactory.getLogger(TBMLSettingsParser.class);
	
	private File tbmlFile;
	private boolean somethingWrong = false;
	private int docScope;
	private int scopeEnd;
	private ArrayList<String> tbmlLines;
	private String subDescriptor = "";
	private String currentScope = "";
	public static final String DOCROOT = "docroot";
	
	public TBMLSettingsParser (File tbmlFile){
		if(!(tbmlFile.exists())){
			try {
				tbmlFile.createNewFile();
				FileWriter fileWriter = new FileWriter(tbmlFile);
				fileWriter.write("<?tbml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<docroot>\r\n</docroot>");
				fileWriter.close();
				this.tbmlFile = tbmlFile;
				setScope("docroot");
			} catch (IOException e) {
				logger.error("A new tbml file could not be created at " + tbmlFile.getAbsolutePath());
				somethingWrong = true;
			}
		}
		else {
			this.tbmlFile = tbmlFile;
			setScope("docroot");
		}
	}
	
	private ArrayList<String> getFileLines() {
		ArrayList<String> lines = new ArrayList<String>();
		try {
			FileReader fileReader = new FileReader(tbmlFile);
			int ch;
			String line = "";
			while ((ch = fileReader.read()) != -1) {
				if ((char) ch == '\n' || (char) ch == '\r') {
					if (!(line.equals(""))) {
						lines.add(line);
						line = "";
					}
				} else {
					line += (char) ch;
				}
			}
			if (line.length() > 0) {
				lines.add(line);
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			logger.error("Could find settings file");
			somethingWrong = true;
		} catch (IOException e) {
			logger.error("Could not read settings file");
			somethingWrong = true;
		}
		return lines;
	}
	
	private int[] indexScope(String scopeKey) {
		if(docScope + 1 == scopeEnd || !(scopeKey.contains("<obj"))) {
			return new int[]{-1, -1};
		} else {
			int start = -1;
			int end = -1;
			boolean foundStart = false;
			for(int i = docScope + 1; i < scopeEnd; i++) {
				if(tbmlLines.get(i).equals(scopeKey)) {
					if(foundStart == false) {
						start = i;
						foundStart = true;
						scopeKey = scopeKey.replace("<obj", "</obj");
					} else {
						end = i;
						break;
					}
				}
			}
			return new int[] {start, end};
		}
	}
	
	public boolean setScope(String scopeKey) {
		tbmlLines = getFileLines();
		if(somethingWrong == false) {
			if(tbmlLines.indexOf("<docroot>") != -1 && tbmlLines.indexOf("</docroot>") != -1) {
				if(scopeKey.equals("docroot")) {
					docScope = tbmlLines.indexOf("<docroot>");
					scopeEnd = tbmlLines.indexOf("</docroot>");
					subDescriptor = "  ";
					currentScope = "<docroot>";
					return true;
				} else {
					int[] subScope = indexScope(subDescriptor + "<obj " + scopeKey + ">");
					if(!(Arrays.equals(subScope, new int[] {-1, -1})) || subScope[1] != -1) {
						docScope = subScope[0];
						scopeEnd = subScope[1];
						currentScope = subDescriptor + "<obj " + scopeKey + ">";
						subDescriptor += "  ";
						return true;
					} else {
						return false;
					}
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public boolean addObj(String newKey) {
		if(Arrays.equals(indexScope(subDescriptor + "<obj " + newKey + ">"), new int[] {-1, -1})) {
			tbmlLines.add(docScope + 1, subDescriptor + "<obj " + newKey + ">");
			tbmlLines.add(docScope + 2, subDescriptor + "</obj " + newKey + ">");
			scopeEnd += 2;
			writeSettings();
			return true;
		} else {
			return false;
		}
	}
	
	public void addVal(String newKey, String value) {
		tbmlLines.add(docScope + 1, subDescriptor + "<val " + newKey + ">" + value + "</val>");
		scopeEnd++;
		writeSettings();
	}
	
	public ArrayList<String> GetObjectNames(){
		ArrayList<String> list = new ArrayList<String>();
		for(int i = docScope + 1; i < scopeEnd; i++) {
			String line = tbmlLines.get(i);
			if(line.startsWith(subDescriptor + "<obj ")) {
				String name = line.replaceFirst(subDescriptor + "<obj ", "");
				list.add(name.substring(0, name.lastIndexOf('>')));
			}
		}
		return list;
	}
	
	public boolean valExists(String valKey, String value) {
		boolean val = false;
		for(int i = docScope + 1; i < scopeEnd; i++) {
			String line = tbmlLines.get(i);
			if(line.equals(subDescriptor + "<val " + valKey + ">" + value + "</val>")) {
				val = true;
				break;
			}
		}
		return val;
	}
	
	public String getFirstInValGroup(String valKey) {
		String val = "";
		for(int i = docScope + 1; i < scopeEnd; i++) {
			String line = tbmlLines.get(i);
			if(line.startsWith(subDescriptor + "<val " + valKey + ">")) {
				val = line.replaceFirst(subDescriptor + "<val " + valKey + ">", "");
				val = val.substring(0, val.lastIndexOf("</val>"));
				break;
			}
		}
		return val;
	}
	
	public void setFirstInValGroup(String valKey, String value) {
		for(int i = docScope + 1; i < scopeEnd; i++) {
			String line = tbmlLines.get(i);
			if(line.startsWith(subDescriptor + "<val " + valKey + ">")) {
				tbmlLines.remove(i);
				tbmlLines.add(i, subDescriptor + "<val " + valKey + ">" + value + "</val>");
				break;
			}
		}
		writeSettings();
	}
	
	public void removeFirstInValGroup(String valKey) {
		for(int i = docScope + 1; i < scopeEnd; i++) {
			String line = tbmlLines.get(i);
			if(line.startsWith(subDescriptor + "<val " + valKey + ">")) {
				tbmlLines.remove(i);
				scopeEnd--;
				break;
			}
		}
		writeSettings();
	}
	
	public void removeVal(String valKey, String value) {
		for(int i = docScope + 1; i < scopeEnd; i++) {
			String line = tbmlLines.get(i);
			if(line.equals(subDescriptor + "<val " + valKey + ">" + value + "</val>")) {
				tbmlLines.remove(i);
				scopeEnd--;
				break;
			}
		}
		writeSettings();
	}
	
	public ArrayList<String> getValGroup(String valKey){
		ArrayList<String> list = new ArrayList<String>();
		for(int i = docScope + 1; i < scopeEnd; i++) {
			String line = tbmlLines.get(i);
			if(line.startsWith(subDescriptor + "<val " + valKey + ">")) {
				String val = line.replaceFirst(subDescriptor + "<val " + valKey + ">", "");
				list.add(val.substring(0, val.lastIndexOf("</val>")));
			}
		}
		return list;
	}
	
	public void removeValGroup(String valKey) {
		for(int i = docScope + 1; i < scopeEnd; i++) {
			String line = tbmlLines.get(i);
			if(line.startsWith(subDescriptor + "<val " + valKey + ">")) {
				tbmlLines.remove(i);
				scopeEnd--;
			}
		}
	}
	
	public void addValuesToGroup(String valKey, ArrayList<String> valList) {
		ArrayList<String> oldList = getValGroup(valKey);
		removeValGroup(valKey);
		for(String val : oldList) {
			if(!(valList.contains(val))) {
				valList.add(val);
			}
		}
		for(String val : valList) {
			addVal(valKey, val);
		}
	}
	
	public void setScopePath(String path) {
		String[] targets = path.split("/");
		setScope(DOCROOT);
		for(String obj : targets) {
			addObj(obj);
			setScope(obj);
		}
	}
	
	private void writeSettings() {
		try {
			tbmlFile.delete();
			tbmlFile.createNewFile();
			FileWriter fileWriter = new FileWriter(tbmlFile);
			for (String line : tbmlLines) {
				fileWriter.write(line + "\r\n");
			}
			fileWriter.close();
		} catch (IOException e) {
			logger.error("Unable to write to settings file");
		}
	}
	
	public void printScope() {
		String scopeName = currentScope.replaceFirst(subDescriptor.replaceFirst("  ", ""), "");
		System.out.println("Current scope:" + scopeName + "\nStart=" + docScope + "\nEnd=" + scopeEnd + "\nContent:");
		for(int i = docScope + 1; i < scopeEnd; i++) {
			System.out.println(tbmlLines.get(i).replaceFirst(subDescriptor, ""));
		}
	}
	
}
