package net.sf.mcf2pdf.calendar;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class QtCssCompat {

	public static final String PREVIOUS_SIBLING_ATTRIBUTE = "cewe-qt-previous-sibling";

	private static final Pattern RULE = Pattern.compile("([^{}]+)\\{([^{}]*)\\}");

	private QtCssCompat() {
	}

	public static void markPreviousSiblings(Element root) {
		boolean previous = false;
		for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node instanceof Element) {
				Element child = (Element) node;
				if (previous) child.setAttribute(PREVIOUS_SIBLING_ATTRIBUTE, "1");
				previous = true;
				markPreviousSiblings(child);
			}
		}
	}

	public static String emulateAdjacentSiblingQuirk(String css) {
		String text = css.replaceAll("(?s)/\\*.*?\\*/", "");
		StringBuilder result = new StringBuilder(text.length());
		Matcher rule = RULE.matcher(text);
		while (rule.find()) {
			List<String> selectors = new ArrayList<String>();
			for (String selector : rule.group(1).split(",")) {
				selectors.add(rewrite(selector.trim()));
			}
			result.append(String.join(", ", selectors)).append(" {").append(rule.group(2)).append("}\n");
		}
		return result.toString();
	}

	static String rewrite(String selector) {
		String[] tokens = selector.replace(">", " > ").replace("+", " + ").trim().split("\\s+");
		List<String> parts = new ArrayList<String>();
		for (int i = 0; i < tokens.length; i++) {
			if (i > 0 && !isCombinator(tokens[i]) && !isCombinator(tokens[i - 1])) parts.add(" ");
			parts.add(tokens[i]);
		}
		int n = parts.size();
		if (n < 5 || !"+".equals(parts.get(n - 2)) || isCombinator(parts.get(n - 3))) return selector;
		String a = parts.get(n - 3);
		int weight = Math.max(1, count(a, '.') + count(a, '#') + count(a, '['));
		StringBuilder subject = new StringBuilder(parts.get(n - 1));
		for (int i = 0; i < weight; i++) subject.append('[').append(PREVIOUS_SIBLING_ATTRIBUTE).append(']');
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < n - 3; i++) {
			String part = parts.get(i);
			out.append(" ".equals(part) ? " " : isCombinator(part) ? " " + part + " " : part);
		}
		return out.append(subject).toString().replaceAll("\\s+", " ").trim();
	}

	private static boolean isCombinator(String token) {
		return " ".equals(token) || ">".equals(token) || "+".equals(token);
	}

	private static int count(String value, char c) {
		int result = 0;
		for (int i = 0; i < value.length(); i++) if (value.charAt(i) == c) result++;
		return result;
	}
}
