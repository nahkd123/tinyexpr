package io.github.nahkd123.tinyexpr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ExprTokenTest {
	// Naming convention:
	// - Last known bad commit: commit$<commit-hash>
	// - Related to issue; issue$<issue-id>

	void assertTokens(String input, ExprToken... tokens) {
		List<ExprToken> list = new ArrayList<>();
		ExprToken.tokenize(input, 0, input.length(), list::add);
		assertEquals(List.of(tokens), list);
	}

	@Test
	void commit$09b8a9d() {
		assertTokens("leveling.evolution > 0",
			new ExprToken.Symbol("leveling"),
			ExprToken.Keyword.PROPERTY,
			new ExprToken.Symbol("evolution"),
			ExprToken.Keyword.GT,
			new ExprToken.NumberLiteral("0", "", ExprToken.NumberLiteral.TYPE_DECIMAL));
	}
}
