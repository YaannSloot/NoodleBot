package main.IanSloat.noodlebot.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard parser and editor class for the proprietary TBML file format used by
 * ThiccBot. Document contents are organized in a hierarchical tree structure,
 * much like the structure of directories and subdirectories found on most
 * modern file systems.
 * 
 * @author Ian Sloat
 *
 */
public class NBMLSettingsParser {

	private static final Logger logger = LoggerFactory.getLogger(NBMLSettingsParser.class);

	private File nbmlFile;
	private boolean somethingWrong = false;
	private int docScope;
	private int scopeEnd;
	private ArrayList<String> nbmlLines;
	private String subDescriptor = "";
	private String currentScope = "";
	public static final String DOCROOT = "docroot";

	/**
	 * Creates a new TBML parser and opens the provided TBML file, or creates one if
	 * it does not exist
	 * 
	 * @param tbmlFile The file to be opened or created
	 */
	public NBMLSettingsParser(File tbmlFile) {
		if (!(tbmlFile.exists())) {
			try {
				tbmlFile.createNewFile();
				FileWriter fileWriter = new FileWriter(tbmlFile);
				fileWriter.write("<?nbml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<docroot>\r\n</docroot>");
				fileWriter.close();
				this.nbmlFile = tbmlFile;
				setScope("docroot");
			} catch (IOException e) {
				logger.error("A new nbml file could not be created at " + tbmlFile.getAbsolutePath());
				somethingWrong = true;
			}
		} else {
			this.nbmlFile = tbmlFile;
			setScope("docroot");
		}
	}

	private ArrayList<String> getFileLines() {
		ArrayList<String> lines = new ArrayList<String>();
		try {
			FileReader fileReader = new FileReader(nbmlFile);
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
		if (docScope + 1 == scopeEnd || !(scopeKey.contains("<obj"))) {
			return new int[] { -1, -1 };
		} else {
			int start = -1;
			int end = -1;
			boolean foundStart = false;
			for (int i = docScope + 1; i < scopeEnd; i++) {
				if (nbmlLines.get(i).equals(scopeKey)) {
					if (foundStart == false) {
						start = i;
						foundStart = true;
						scopeKey = scopeKey.replace("<obj", "</obj");
					} else {
						end = i;
						break;
					}
				}
			}
			return new int[] { start, end };
		}
	}

	/**
	 * Raises the scope of the document to an existing object enclosed in the
	 * current scoped object, or returns the document to the root scope if specified
	 * 
	 * @param scopeKey The object key to scope to, or "docroot" to return to the
	 *                 root of the document
	 * @return true if the object exists, false otherwise
	 */
	public boolean setScope(String scopeKey) {
		nbmlLines = getFileLines();
		if (somethingWrong == false) {
			if (nbmlLines.indexOf("<docroot>") != -1 && nbmlLines.indexOf("</docroot>") != -1) {
				if (scopeKey.equals("docroot")) {
					docScope = nbmlLines.indexOf("<docroot>");
					scopeEnd = nbmlLines.indexOf("</docroot>");
					subDescriptor = "  ";
					currentScope = "<docroot>";
					return true;
				} else {
					int[] subScope = indexScope(subDescriptor + "<obj " + scopeKey + ">");
					if (!(Arrays.equals(subScope, new int[] { -1, -1 })) || subScope[1] != -1) {
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

	/**
	 * Adds an object inside the currently scoped object
	 * 
	 * @param newKey The new object key
	 * @return true if an object was created, false if the object already exists
	 */
	public boolean addObj(String newKey) {
		if (Arrays.equals(indexScope(subDescriptor + "<obj " + newKey + ">"), new int[] { -1, -1 })) {
			nbmlLines.add(docScope + 1, subDescriptor + "<obj " + newKey + ">");
			nbmlLines.add(docScope + 2, subDescriptor + "</obj " + newKey + ">");
			scopeEnd += 2;
			writeSettings();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Adds a new value inside the currently scoped object
	 * 
	 * @param newKey The object key to assign to the value. If multiple values with
	 *               the same key exits, it is known as a value group
	 * @param value  The value to record
	 */
	public void addVal(String newKey, String value) {
		nbmlLines.add(docScope + 1, subDescriptor + "<val " + newKey + ">" + value + "</val>");
		scopeEnd++;
		writeSettings();
	}

	/**
	 * Retrieves the key names of all currently existing objects inside the
	 * currently scoped object
	 * 
	 * @return An ArrayList containing all the object key names
	 */
	public ArrayList<String> GetObjectNames() {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = docScope + 1; i < scopeEnd; i++) {
			String line = nbmlLines.get(i);
			if (line.startsWith(subDescriptor + "<obj ")) {
				String name = line.replaceFirst(subDescriptor + "<obj ", "");
				list.add(name.substring(0, name.lastIndexOf('>')));
			}
		}
		return list;
	}

	/**
	 * Queries the currently scoped object to check if the specified key-value pair
	 * exists
	 * 
	 * @param valKey the value key to search for
	 * @param value  the value to search for
	 * @return true if the pair exists, false otherwise
	 */
	public boolean valExists(String valKey, String value) {
		boolean val = false;
		for (int i = docScope + 1; i < scopeEnd; i++) {
			String line = nbmlLines.get(i);
			if (line.equals(subDescriptor + "<val " + valKey + ">" + value + "</val>")) {
				val = true;
				break;
			}
		}
		return val;
	}

	/**
	 * Retrieves the first value that exist in the specified value key group from
	 * the currently scoped object
	 * 
	 * @param valKey the value key to search for
	 * @return a String containing the first value retrieved from the group, or a
	 *         blank string if the key group does not exist
	 */
	public String getFirstInValGroup(String valKey) {
		String val = "";
		for (int i = docScope + 1; i < scopeEnd; i++) {
			String line = nbmlLines.get(i);
			if (line.startsWith(subDescriptor + "<val " + valKey + ">")) {
				val = line.replaceFirst(subDescriptor + "<val " + valKey + ">", "");
				val = val.substring(0, val.lastIndexOf("</val>"));
				break;
			}
		}
		return val;
	}

	/**
	 * Sets the first value in the specified key group of the currently scoped
	 * object to a new value, or does nothing if the key group does not exist
	 * 
	 * @param valKey the value key to focus on
	 * @param value  the new value to write
	 */
	public void setFirstInValGroup(String valKey, String value) {
		for (int i = docScope + 1; i < scopeEnd; i++) {
			String line = nbmlLines.get(i);
			if (line.startsWith(subDescriptor + "<val " + valKey + ">")) {
				nbmlLines.set(i, subDescriptor + "<val " + valKey + ">" + value + "</val>");
				break;
			}
		}
		writeSettings();
	}

	/**
	 * Removes the first value in the specified value key group in the currently
	 * scoped object, or does nothing if the key group does not exist
	 * 
	 * @param valKey the key group to focus on
	 */
	public void removeFirstInValGroup(String valKey) {
		for (int i = docScope + 1; i < scopeEnd; i++) {
			String line = nbmlLines.get(i);
			if (line.startsWith(subDescriptor + "<val " + valKey + ">")) {
				nbmlLines.remove(i);
				scopeEnd--;
				break;
			}
		}
		writeSettings();
	}

	/**
	 * Removes the first occurrence of the specified value from the specified key
	 * group in the currently scoped object, or does nothing if the value does not
	 * exist
	 * 
	 * @param valKey the key group to focus on
	 * @param value  the value to remove
	 */
	public void removeVal(String valKey, String value) {
		for (int i = docScope + 1; i < scopeEnd; i++) {
			String line = nbmlLines.get(i);
			if (line.equals(subDescriptor + "<val " + valKey + ">" + value + "</val>")) {
				nbmlLines.remove(i);
				scopeEnd--;
				break;
			}
		}
		writeSettings();
	}

	/**
	 * Retrieves every value from the specified key group in the currently scoped
	 * object
	 * 
	 * @param valKey the key group to retrieve the values from
	 * @return a list containing the retrieved values, or an empty list if the key
	 *         group does not exist
	 */
	public ArrayList<String> getValGroup(String valKey) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = docScope + 1; i < scopeEnd; i++) {
			String line = nbmlLines.get(i);
			if (line.startsWith(subDescriptor + "<val " + valKey + ">")) {
				String val = line.replaceFirst(subDescriptor + "<val " + valKey + ">", "");
				list.add(val.substring(0, val.lastIndexOf("</val>")));
			}
		}
		return list;
	}
 
	/**
	 * Counts the number of values that have the specified key in the currently scoped object
	 * @param valKey the key group to focus on
	 * @return a long value representing the total number of values that exist with the specified key
	 */
	public long tallyValGroup(String valKey) {
		long result = 0;
		for (int i = docScope + 1; i < scopeEnd; i++) {
			String line = nbmlLines.get(i);
			if (line.startsWith(subDescriptor + "<val " + valKey + ">")) {
				result++;
			}
		}
		return result;
	}
	
	/**
	 * Completely removes the specified value group from the currently scoped
	 * object, or does nothing if the key group does not exist
	 * 
	 * @param valKey the key group to remove
	 */
	public void removeValGroup(String valKey) {
		for (int i = docScope + 1; i < scopeEnd; i++) {
			String line = nbmlLines.get(i);
			if (line.startsWith(subDescriptor + "<val " + valKey + ">")) {
				nbmlLines.remove(i);
				i--;
				scopeEnd--;
			}
		}
		writeSettings();
	}

	/**
	 * Completely removes everything, including nested objects, from the currently
	 * scoped object
	 */
	public void clearCurrentObj() {
		for (int i = docScope + 1; i < scopeEnd; i++) {
			nbmlLines.remove(i);
			i--;
			scopeEnd--;
		}
		writeSettings();
	}

	/**
	 * Adds the provided list of values to the specified value key group in the
	 * currently scoped object
	 * 
	 * @param valKey  the key group to add the values to
	 * @param valList the list of values to add
	 */
	public void addValuesToGroup(String valKey, ArrayList<String> valList) {
		ArrayList<String> oldList = getValGroup(valKey);
		removeValGroup(valKey);
		for (String val : oldList) {
			if (!(valList.contains(val))) {
				valList.add(val);
			}
		}
		for (String val : valList) {
			addVal(valKey, val);
		}
	}

	/**
	 * Sets the current scope via a string similar in structure to a directory path.
	 * The method starts at the root of the document, and rolls up to the last
	 * object specified in the path. Nonexistent objects in the path will be created
	 * if they do not already exist.
	 * <p>
	 * For example, if the path "users/numbers/letters" is provided, and none of
	 * these objects exist, this method will cycle through each provided object
	 * name, starting at docroot, and ending at the "letters" object, which would be
	 * enclosed inside the "numbers" object, and so on. The parser scope would also
	 * have been set to the "letters" object. This method can be likened to the "cd"
	 * command found on most UNIX and DOS systems.
	 * 
	 * @param path the object path to scope to, represented as a String
	 */
	public void setScopePath(String path) {
		if (path.charAt(0) == '/') {
			path = path.substring(1);
		}
		if (path.charAt(path.length() - 1) == '/') {
			path = path.substring(0, path.length() - 1);
		}
		String[] targets = path.split("/");
		setScope(DOCROOT);
		for (String obj : targets) {
			addObj(obj);
			setScope(obj);
		}
	}

	private void writeSettings() {
		try {
			nbmlFile.delete();
			nbmlFile.createNewFile();
			FileWriter fileWriter = new FileWriter(nbmlFile);
			for (String line : nbmlLines) {
				fileWriter.write(line + "\r\n");
			}
			fileWriter.close();
		} catch (IOException e) {
			logger.error("Unable to write to settings file");
		}
	}

	/**
	 * Prints the contents of the currently scoped object to the console
	 */
	public void printScope() {
		String scopeName = currentScope.replaceFirst(subDescriptor.replaceFirst("  ", ""), "");
		System.out.println("Current scope:" + scopeName + "\nStart=" + docScope + "\nEnd=" + scopeEnd + "\nContent:");
		for (int i = docScope + 1; i < scopeEnd; i++) {
			System.out.println(nbmlLines.get(i).replaceFirst(subDescriptor, ""));
		}
	}

}
