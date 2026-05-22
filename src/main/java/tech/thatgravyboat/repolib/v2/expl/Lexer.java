package tech.thatgravyboat.repolib.v2.expl;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public final class Lexer {
    private final String source;
    private int start = 0;
    private int cursor = 0;

    public Lexer(String source) {
        this.source = source;
    }

    public @Nullable Token peek() {
        var result = next();
        cursor = start; // Reset to where it was before the call to next.
        return result;
    }

    public void expect(Token type) {
        var next = next();
        if (next != type) {
            throw new IllegalStateException("Expected " + type + " but got " + next);
        }
    }

    public void expect(Token type, String span) {
        expect(type);
        if (!span().equals(span)) {
            throw new IllegalStateException("Expected " + span + " but got " + span());
        }
    }

    public @Nullable Token next() {
        consumeWhitespace();

        start = cursor;
        if (atEnd()) {
            return null;
        }

        char c = advance();
        if (isAlpha(c)) {
            return identOrKeyword();
        } else if (isDigit(c)) {
            return number();
        } else if (c == '\'' || c == '"') {
            return string(c);
        }

        return symbol(c);
    }

    private Token string(char quote) {
        while (peek0() != quote) {
            var isEscaped = peek0() == '\\';
            advance();
            if (isEscaped && peek0() == quote) {
                advance();
            }
        }

        match(quote);

        return Token.LITERAL_STR;
    }

    private Token symbol(char c) {
        return switch (c) {
            case '[' -> Token.L_BRACKET;
            case ']' -> Token.R_BRACKET;
            case '(' -> Token.L_PARENTHESES;
            case ')' -> Token.R_PARENTHESES;
            case '{' -> Token.L_BRACE;
            case '}' -> Token.R_BRACE;
            case '=' -> Token.EQUALS;
            case ',' -> Token.COMMA;
            case '.' -> Token.DOT;
            case '>' -> {
                if (peek0() == '=') {
                    advance();
                    yield Token.GTE;
                }
                yield Token.GT;
            }
            case '<' -> {
                if (peek0() == '=') {
                    advance();
                    yield Token.LTE;
                }
                yield Token.LT;
            }
            case '-' -> {
                if (peek0() == '>') {
                    advance();
                    yield Token.LAMBDA_ARROW;
                }
                yield Token.UNARY_MINUS;
            }
            case '!' -> Token.UNARY_NOT;
            case '?' -> Token.QUESTION;
            case ':' -> Token.COLON;
            case ';' -> Token.SEMICOLON;
            default -> unexpected(c);
        };
    }

    private Token identOrKeyword() {
        advanceWhile(it -> isAlpha(it) || isDigit(it));

        return switch (span()) {
            case "true", "false" -> Token.LITERAL_BOOL;
            case "if" -> Token.IF;
            case "debug" -> Token.DEBUG;
            case "else" -> Token.ELSE;
            case "in" -> Token.IN;
            case "for" -> Token.FOR;
            case "return" -> Token.RETURN;
            case "break" -> Token.BREAK;
            case "continue" -> Token.CONTINUE;
            case "match" -> Token.MATCH;
            default -> Token.IDENT;
        };
    }

    private Token number() {
        advanceWhile(this::isDigit);

        if (match('.')) {
            advanceWhile(this::isDigit);
        }

        return Token.LITERAL_NUM;
    }

    public String span() {
        return source.substring(start, cursor).strip();
    }

    private void consumeWhitespace() {
        boolean inComment = false;
        while (true) {
            if (peek0() == '#') {
                inComment = true;
                advance();
            } else if (inComment && peek0() == '\n') {
                inComment = false;
                advance();
            } else if (inComment || Character.isWhitespace(peek0())) {
                advance();
            } else {
                break;
            }
        }
    }

    public boolean atEnd() {
        return cursor >= source.length();
    }

    private char peek0() {
        if (atEnd()) {
            return '\u0000';
        }
        return source.charAt(cursor);
    }

    private void advanceWhile(Predicate<Character> condition) {
        while (condition.test(peek0())) {
            advance();
        }
    }

    private char advance() {
        if (atEnd()) {
            throw new IllegalStateException("unexpected end of input");
        }
        return source.charAt(cursor++);
    }

    private boolean match(char c) {
        if (atEnd()) {
            return false;
        }
        if (peek0() != c) {
            return false;
        }
        advance();
        return true;
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private Token unexpected(char c) {
        throw new IllegalStateException(String.format("unexpected token '%s' at %d.", c, cursor));
    }

    public enum Token {
        IDENT,
        LITERAL_STR,
        LITERAL_NUM,
        LITERAL_BOOL,

        // Keywords
        IF,
        DEBUG,
        ELSE,
        IN,
        FOR,
        RETURN,
        BREAK,
        CONTINUE,
        MATCH,

        EQUALS,

        L_PARENTHESES,
        R_PARENTHESES,

        COMMA,
        DOT,


        QUESTION,
        COLON,
        SEMICOLON,

        UNARY_NOT,
        UNARY_MINUS,
        LAMBDA_ARROW,

        GT,
        GTE,
        LT,
        LTE,

        L_BRACKET,
        R_BRACKET,

        L_BRACE,
        R_BRACE
    }

}

