package io.github.nahkd123.tinyexpr;

import static io.github.nahkd123.tinyexpr.ExprToken.NumberLiteral.TYPE_DECIMAL;
import static io.github.nahkd123.tinyexpr.ExprToken.NumberLiteral.TYPE_HEXADECIMAL;
import static io.github.nahkd123.tinyexpr.ExprToken.NumberLiteral.TYPE_OCTAL;

import java.util.function.Consumer;

public sealed interface ExprToken {
	enum Keyword implements ExprToken {
		ADD("+"),
		SUB("-"),
		MUL("*"),
		DIV("/"),
		SHL("<<"),
		SHR(">>"),
		EQ("=="),
		NE("!="),
		LE("<="),
		GE(">="),
		LT("<"),
		GT(">"),
		AND("&"),
		OR("|"),
		XOR("^"),
		NOT("!"),
		INVERT("~"),
		GOPEN("("),
		GCLOSE(")"),
		BOPEN("["),
		BCLOSE("]"),
		PROPERTY("."),
		NEXT(","),
		TERNARY_TEST("?"),
		TERNARY_OR(":");

		private String content;

		private Keyword(String content) {
			this.content = content;
		}
	}

	record Symbol(String name) implements ExprToken {
	}

	record StringLiteral(String value) implements ExprToken {
	}

	record NumberLiteral(String intg, String frac, int type) implements ExprToken {
		public static final int TYPE_DECIMAL = 0;
		public static final int TYPE_OCTAL = 1;
		public static final int TYPE_HEXADECIMAL = 2;
	}

	static void tokenize(CharSequence input, int start, int end, Consumer<ExprToken> collector) {
		Keyword[] keywords = Keyword.values();
		CharCollector buf = new CharCollector(32);

		outer: while (start < end) {
			if (Character.isWhitespace(input.charAt(start))) {
				start++;
				continue;
			}

			if (input.charAt(start) == '0') {
				start++;
				char ch;
				String intg = null;
				int type;

				if (start >= end) {
					collector.accept(new NumberLiteral("0", "", TYPE_DECIMAL));
					return;
				} else if (input.charAt(start) >= '0' && input.charAt(start) <= '7') {
					type = TYPE_OCTAL;
					buf.push(input.charAt(start));
					start++;
				} else if (input.charAt(start) == 'x') {
					type = TYPE_HEXADECIMAL;
					start++;
				} else if (input.charAt(start) == '.') {
					type = TYPE_DECIMAL;
					intg = "0";
					start++;
				} else {
					collector.accept(new NumberLiteral("0", "", TYPE_DECIMAL));
					continue;
				}

				while (start < end
					&& ((type == TYPE_DECIMAL && (ch = input.charAt(start)) >= '0' && ch <= '9')
						| (type == TYPE_HEXADECIMAL && (((ch = input.charAt(start)) >= '0' && ch <= '9')
							| (ch >= 'a' && ch <= 'f')
							| (ch >= 'A' && ch <= 'F')))
						| (type == TYPE_OCTAL && (ch = input.charAt(start)) >= '0' && ch <= '7')
						| (intg == null && input.charAt(start) == '.'))) {
					if (input.charAt(start) == '.') {
						intg = buf.toString();
						buf.clear();
					} else {
						buf.push(input.charAt(start));
					}

					start++;
				}

				collector.accept(intg != null
					? new NumberLiteral(intg, buf.toString(), type)
					: new NumberLiteral(buf.toString(), "", type));
				buf.clear();
				continue;
			}

			if ((input.charAt(start) >= '1' && input.charAt(start) <= '9') ||
				(start + 1 < end
					&& input.charAt(start) == '.'
					&& input.charAt(start + 1) >= '0'
					&& input.charAt(start + 1) <= '9')) {
				String intg = input.charAt(start) == '.' ? "" : null;
				if (input.charAt(start) >= '0' && input.charAt(start) <= '9') buf.push(input.charAt(start));
				start++;

				while (start < end && ((input.charAt(start) >= '0' && input.charAt(start) <= '9')
					| (intg == null && input.charAt(start) == '.'))) {
					if (input.charAt(start) == '.') {
						intg = buf.toString();
						buf.clear();
					} else {
						buf.push(input.charAt(start));
					}

					start++;
				}

				collector.accept(intg != null
					? new NumberLiteral(intg, buf.toString(), NumberLiteral.TYPE_DECIMAL)
					: new NumberLiteral(buf.toString(), "", NumberLiteral.TYPE_DECIMAL));
				buf.clear();
				continue;
			}

			for (Keyword keyword : keywords) {
				if (end - start < keyword.content.length()) continue;
				if (!keyword.content.contentEquals(input.subSequence(start, start + keyword.content.length())))
					continue;

				collector.accept(keyword);
				start += keyword.content.length();
				continue outer;
			}

			if (Character.isJavaIdentifierStart(input.charAt(start))) {
				buf.push(input.charAt(start));
				start++;

				while (start < end && Character.isJavaIdentifierPart(input.charAt(start))) {
					buf.push(input.charAt(start));
					start++;
				}

				collector.accept(new Symbol(buf.toString()));
				buf.clear();
				continue;
			}

			if (input.charAt(start) == '"' || input.charAt(start) == '\'') {
				char terminator = input.charAt(start);
				boolean escaping = false;
				start++;

				while (start < end && (escaping || input.charAt(start) != terminator)) {
					if (escaping) {
						char ch = input.charAt(start);
						buf.push(switch (ch) {
						case 't' -> '\t';
						case 'n' -> '\n';
						case 'r' -> '\r';
						default -> ch;
						});
						escaping = false;
					} else {
						switch (input.charAt(start)) {
						case '\\':
							escaping = true;
							break;
						default:
							buf.push(input.charAt(start));
							break;
						}
					}

					start++;
				}

				if (start >= end) throw new IllegalArgumentException("Expecting '%s' but found end of input"
					.formatted(terminator));

				start++;
				collector.accept(new StringLiteral(buf.toString()));
				buf.clear();
				continue;
			}

			throw new IllegalArgumentException("Unable to parse '%s' at index %d in '%s'"
				.formatted(input.charAt(start), start, input));
		}
	}
}
