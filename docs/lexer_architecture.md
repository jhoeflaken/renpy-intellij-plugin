# Ren'Py Lexer Architecture

This document describes the architectural design, token pipeline, indentation handling, and state machine of the Ren'Py lexical analysis subsystem in the IntelliJ Platform plugin.

---

## 1. Overview & Architectural Goals

Ren'Py combines narrative scripting, Python expressions, layout directives, visual displayables, and indentation-based block hierarchies. Building a fast, resilient, and incrementally restartable lexer for the IntelliJ Platform requires balancing three key architectural goals:

1. **High Throughput & Incremental Restartability:** Lexing occurs on the UI and background highlighting threads during user typing. The lexer must execute in sub-millisecond timeframes and maintain restartable state at token boundaries.
2. **Indentation Sensitivity:** Like Python, Ren'Py uses whitespace indentation rather than curly braces to define structural block scope. The lexer must emit synthetic `INDENT` and `DEDENT` tokens so that downstream grammar parsers (e.g. Grammar-Kit / BNF) can operate using standard CFG grammar rules.
3. **Multi-Dialect & Embedded Code Handling:** Ren'Py scripts freely mix script keywords (`label`, `scene`, `show`), dialogue text, single-line Python statements (`$ ...`), and multi-line Python blocks.

To achieve these goals cleanly without coupling low-level character scanning with indentation state management, the subsystem employs a **Two-Stage Lexer Pipeline**.

---

## 2. Two-Stage Lexer Pipeline

```
                                  Source Code (.rpy)
                                          │
                                          ▼
                      ┌─────────────────────────────────────────┐
                      │                Stage 1                  │
                      │       JFlex Base Scanner                │
                      │       (_RenpyLexer.flex)                │
                      │                                         │
                      │  • Scans raw character stream           │
                      │  • Emits keywords, operators, comments, │
                      │    strings, newlines, raw whitespace    │
                      │  • State machine for '$' one-line Python│
                      └─────────────────────────────────────────┘
                                          │
                                          │ Base Tokens
                                          ▼
                      ┌─────────────────────────────────────────┐
                      │          RenpyLexerAdapter              │
                      │          (FlexAdapter)                  │
                      └─────────────────────────────────────────┘
                                          │
                                          │ IntelliJ Lexer API
                                          ▼
                      ┌─────────────────────────────────────────┐
                      │                Stage 2                  │
                      │       RenpyIndentLexer                  │
                      │       (LookAheadLexer)                  │
                      │                                         │
                      │  • Maintains Indentation Stack [0, ...] │
                      │  • Translates whitespace into           │
                      │    synthetic INDENT and DEDENT tokens   │
                      │  • Tracks bracket nesting depth () [] {}│
                      │  • Unwinds remaining DEDENTs at EOF     │
                      └─────────────────────────────────────────┘
                                          │
                                          ▼
                             Tokens to Parser & Highlighter
```

### Stage 1: Base Lexer (`_RenpyLexer.flex` + `RenpyLexerAdapter`)
- Generated using **JFlex** via `Grammar-Kit`.
- Performs high-performance character-level regex matching.
- Recognizes all Ren'Py keywords, control flow constructs, boolean literals, identifiers, numeric literals (integers, floats, hex), comments (`# ...`), strings (single, double, triple-quoted), operators, and newlines.
- Employs lexical states (e.g., `IN_ONE_LINE_PYTHON`) to capture single-line Python statements following `$`.
- Wrapped in `RenpyLexerAdapter` (`FlexAdapter`) to conform to the IntelliJ Platform `Lexer` interface.

### Stage 2: Indentation & Layout Adapter (`RenpyIndentLexer`)
- Extends IntelliJ's `LookAheadLexer`.
- Wraps the base lexer and intercepts the token stream.
- Maintains an indentation stack and line-state flags.
- Replaces leading line whitespace with appropriate synthetic `INDENT` or `DEDENT` tokens.
- Ensures bracket-nested expressions and blank/comment lines do not disrupt block hierarchy.

---

## 3. Indentation Algorithm and Stack Management

Ren'Py code blocks are indented relative to their parent statements. The indentation tracking algorithm in `RenpyIndentLexer` works as follows:

### 3.1 Indentation Stack
- Initialized with `[0]` on `start()`.
- The top of the stack (`indentStack.last()`) represents the current active block indentation level in column units.

### 3.2 Indentation Calculation
Leading whitespace on each line is converted to column units:
- Space `' '`: advances column by `1`.
- Tab `'\t'`: advances column to the next multiple of 4 (`(column / 4 + 1) * 4`).

### 3.3 Indentation Transitions
When encountering the first token on a new line (outside bracket nesting):
1. **Deeper Indentation (`indent > currentIndent`):**
   - Push new column level to `indentStack`.
   - Emit `WHITE_SPACE` followed by a synthetic `INDENT` token.
2. **Shallower Indentation (`indent < currentIndent`):**
   - Pop levels from `indentStack` until matching the target level (or reaching 0).
   - Emit a synthetic `DEDENT` token for each popped level.
3. **Same Indentation (`indent == currentIndent`):**
   - Emit leading `WHITE_SPACE` without modifying block tokens.
4. **End of File (EOF):**
   - When the underlying token stream terminates (`baseLexer.tokenType == null`), any active indentation levels above `0` are popped sequentially, emitting `DEDENT` tokens until the stack returns to `[0]`.

---

## 4. Special Syntax & Edge Cases

### 4.1 Blank Lines and Comment Lines
- Blank lines (whitespace followed immediately by `NEWLINE`) and comment-only lines (whitespace followed by `COMMENT`) do **not** trigger block indentation changes.
- They are emitted as whitespace/comments and preserve the line-start state for the subsequent substantive statement.

### 4.2 Bracket Nesting Suppression
- When inside open parentheses `()`, square brackets `[]`, or curly braces `{}`, indentation changes do not trigger `INDENT` or `DEDENT` tokens.
- `bracketDepth` is incremented on opening brackets and decremented on closing brackets.
- Newlines inside nested brackets do not reset `atLineStart`, preventing multi-line lists, function calls, or parameter tuples from breaking block scope.

### 4.3 Line Continuations
- A backslash before a newline (`\` + `\n`) is scanned by the base lexer as `LINE_CONTINUATION`.
- It connects the physical lines into a single logical statement line without triggering block indentation logic.

### 4.4 One-Line Python (`$ ...`)
- When the lexer encounters `$` in state `YYINITIAL`, it emits `ONE_LINE_PYTHON` and transitions to the `IN_ONE_LINE_PYTHON` lexical state.
- In this state, the remainder of the line up to the newline is scanned as `PYTHON_CODE`.
- On encountering a newline, the state resets to `YYINITIAL`.

---

## 5. Token Taxonomy (`RenpyTokenTypes`)

Tokens are structured into cohesive semantic groups:

| Category | Token Elements | Description |
| :--- | :--- | :--- |
| **Structural / Layout** | `NEWLINE`, `INDENT`, `DEDENT`, `LINE_CONTINUATION`, `COMMENT` | Define script hierarchy and layout |
| **Python Embedding** | `ONE_LINE_PYTHON` (`$`), `PYTHON_CODE` | Delimit and encapsulate embedded Python logic |
| **Literals & Identifiers** | `IDENTIFIER`, `STRING_LITERAL`, `NUMBER` | User variables, dialogue text, numbers |
| **Ren'Py Statement Keywords** | `KW_LABEL`, `KW_JUMP`, `KW_CALL`, `KW_RETURN`, `KW_SCENE`, `KW_SHOW`, `KW_HIDE`, `KW_WITH`, `KW_MENU`, `KW_PYTHON`, `KW_INIT`, `KW_DEFINE`, `KW_DEFAULT`, `KW_TRANSFORM`, `KW_SCREEN`, `KW_IMAGE`, `KW_PLAY`, `KW_STOP`, etc. | Engine script statements and declarations |
| **Control Flow Keywords** | `KW_IF`, `KW_ELIF`, `KW_ELSE`, `KW_WHILE`, `KW_FOR`, `KW_PASS`, `KW_IN`, `KW_NOT`, `KW_AND`, `KW_OR`, `KW_IS` | Logical branching and loop control |
| **Punctuation & Operators** | `COLON`, `COMMA`, `DOT`, `EQ`, `EQEQ`, `NEQ`, `LEQ`, `GEQ`, `PLUS`, `MINUS`, `STAR`, `SLASH`, `LPAREN`, `RPAREN`, `LBRACKET`, `RBRACKET`, etc. | Expression and syntax delimiters |

---

## 6. Testing & Validation Strategy

The lexer is validated by `RenpyLexerTest`, inheriting from `com.intellij.testFramework.LexerTestCase`:
- **Statement & Dialogue Tokenization:** Verifies keywords, identifiers, strings, and operators across canonical Ren'Py scripts.
- **Nested Menus & Block Scoping:** Validates that `INDENT` and `DEDENT` tokens strictly balance across multi-level branching menus and label definitions.
- **Embedded Python Scanning:** Verifies correct capture of `$` statement boundaries and Python payload strings.
- **Comment & Blank Line Invariance:** Confirms indentation stacks remain stable when encountering comments and blank lines within deep blocks.
- **Line Continuation Resiliency:** Ensures backslash line continuations do not produce spurious block tokens.
