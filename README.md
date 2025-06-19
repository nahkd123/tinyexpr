# TinyExpr
Tiny embedding expression language

## Quick embedding example
```java
var expr = ExprParser.parse("'hello world ' + 42");
expr.eval(null); // => "hello world 42"

// Providing variables
expr = ExprParser.parse("x * 5 + y * 3");
expr.eval(name -> switch (name) {
    case "x" -> new Expr.JavaDouble(1.2);
    case "y" -> new Expr.JavaDouble(1.8);
    default -> new Expr.JavaNull();
});
```

## Quick expression examples
```
42 + 1337
"hello world"[2] (returns "l")
("hello world"[0] == "h") ? 42 : 1337 (returns 42)
```

## Quick reference
### Literals
- `0`, `1`, `42`, `1337`, etc: Integer;
- `12.`, `.12`, `1.2`: Float;
- `077`: Octal number representation;
- `0xDEADBEEF`: Hexadecimal number representation;
- `0x.7F`: Float with value `127/256`;
- `"string"` and `'string'`: String;
- `'\tindented'`: Escaping.

### Expression
- Unary `~` (`~0`): Flip all bits;
- Unary `-` (`-5`): Negate value;
- Unary `!` (`!1`): Negate truth value (in this case, the value is `0`);
- Binary `+`, `-`, `*` and `/`: Basic binary operators;
- Binary `&`, `|`, `^`, `<<` and `>>`: AND, OR, XOR, SHL and SHR bitwise operators;
- Binary `==`, `!=`, `>`, `>=`, `<` and `<=`: Conditional;
- Grouping `(` and `)`: Group expressions;
- Function call `<expression>(<param-1>, <param-2>, ...)`: Function call;
- Indexing `<expression>[<index-expression>]`: Indexing in the value;
- Ternary `<expression> ? <true> : <false>`: If `<expression>` is non-zero, use `<true>`, otherwise `<false>`.

### String properties
- `.len`, `.length`: Length of the string;
- `.upper`, `.uppercase`: Convert string to uppercase;
- `.lower`, `.lowercase`: Convert string to lowercase.

## License
MIT License.
