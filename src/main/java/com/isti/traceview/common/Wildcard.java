package com.isti.traceview.common;

import com.isti.traceview.TraceView;
import com.isti.traceview.TraceViewException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * this class realize unix command line wildcard syntax
 * 
 * @author Max Kokoulin
 */
public class Wildcard {
	private static final Logger logger = Logger.getLogger(Wildcard.class);

	List<String> path = null;
	List<File> lst = new ArrayList<>();

	/**
	 * @param pattern
	 *            wildcard expression
	 * @param text
	 *            text to test
	 * @return flag if text matches pattern
	 */
	public static boolean matches(String pattern, String text) {
		boolean ret = text.matches(wildcardToRegex(pattern));
		logger.debug("Match " + text + " with pattern " + wildcardToRegex(pattern) + ": " + ret);
		return ret;
	}

	/**
	 * Finds files by mask
	 * 
	 * @param mask
	 *            wildcard expression to find file
	 * @return list of found files
	 * @throws TraceViewException if the mask is not parseable
	 */
	public List<File> getFilesByMask(String mask) throws TraceViewException {
		lst.clear();
		try {
			if (TraceView.osNameString.contains("Windows")) {
				String[] as = mask.replaceFirst(":\\\\\\\\", ":\\\\").split("\\\\");
				path = new ArrayList<>(Arrays.asList(as));
				if (path.get(0).contains(":")) {
					// absolute path
					explore(path.get(0) + "\\\\", 0);
				} else if (path.get(0).equals(".") || path.get(0).equals("..") || path.get(0).equals("~")) {
					// relative path
					explore(path.get(0), 0);
				} else {
					// relative path without point
					path.add(0, ".");
					explore(path.get(0), 0);
				}
			} else {
				String[] as = mask.split(File.separator);
				path = new ArrayList<>(Arrays.asList(as));
				if (path.get(0).equals(".") || path.get(0).equals("..") || path.get(0).equals("~")) {
					// relative path
					explore(path.get(0), 0);
				} else if (path.get(0).equals("")) {
					// absolute path
					explore("/", 0);
				} else {
					// relative path without point
					path.add(0, ".");
					explore(path.get(0), 0);
				}
			}
		} catch (Exception e) {
			StringBuilder message = new StringBuilder();
			message.append("Can't parse wildcarded path: '" + mask + "'" + e.toString());
			throw new TraceViewException(message.toString());
		}
		return lst;
	}

	/**
	 * Recursive searching
	 * 
	 * @param partPath - currently exploring point of path
	 * @param i - current depth
	 */

	private void explore(String partPath, int i) {
		File f = new File(partPath);
		File[] dir = f.listFiles();
      //System.out.format("== explore(partPath=%s i=%d) File f=%s path=%s (dir.length=%d)\n", partPath, i, f.getName(), f.getPath(), dir.length);
		if (dir.length > 0) {
			for (File file : dir) {
				if (path.size() > i + 1) {
					if (Wildcard.matches(path.get(i + 1), file.getName())) {
						if (file.isDirectory()) {
							explore(partPath + File.separator + file.getName(), i + 1);
						} else {
							//lst.add(dir[j]);
							// MTH: This was adding matches *along* the path, not just at the terminus, e.g.,
							//      -d 'xs0/seed/*/2012/2012_160*/*seed' was matching xs0/seed/jts.seed
							// Added if statement to make sure we only match at the end of the path
							// This has not been tested on Windows!
							if ((path.size() - i) == 2) {
								//System.out.format("== +++ Found matching file=[%s] [path.size()=%d i=%d]\n", dir[j], path.size(), i);
								lst.add(file);
							}
						}
					}
				} else {
					if (file.isDirectory()) {
						explore(partPath + File.separator + file.getName(), i + 1);
					} else {
						lst.add(file);
					}
				}
			}
		}
	}

	public static String wildcardToRegex(String wild) {
		if (wild == null)
			return null; 
		StringBuilder buffer = new StringBuilder();
		char[] chars = wild.toCharArray();
		for (char aChar : chars) {
			if (aChar == '*') {
				buffer.append(".*");
			} else if (aChar == '?') {
				buffer.append(".");
			} else if (aChar == '!') {
				buffer.append("^");
			} else if (aChar == '{') {
				buffer.append("(");
			} else if (aChar == '}') {
				buffer.append(")");
			} else if (aChar == ',') {
				buffer.append("|");
			} else if ("+()^$.|\\".indexOf(aChar) != -1) {
				buffer.append('\\').append(aChar); // prefix all unused metacharacters with
			}
			// backslash
			else {
				buffer.append(aChar);
			}
		}
		return buffer.toString();
	}
}
