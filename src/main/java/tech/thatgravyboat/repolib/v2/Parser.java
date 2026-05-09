package tech.thatgravyboat.repolib.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Parser {
    private final String source;
    private final Lexer lexer;

    public Parser(String source) {
        this.source = source;
        this.lexer = new Lexer(source);
    }

    public Expression parse() {
        return parseUntil(Lexer.Token.SEMICOLON);
    }

    public Expression parseUntil(Lexer.Token end) {
        Expression current = null;

        while (true) {

            if (lexer.peek() == end) {
                return current;
            }

            switch (lexer.next()) {
                case Lexer.Token.IDENT -> {
                    var access = memberAccess();

                    Lexer.Token op;
                    switch (op = lexer.peek()) {
                        case Lexer.Token.EQUALS -> {
                            lexer.next();

                            var value = parseUntil(Lexer.Token.SEMICOLON);

                            current = new Expression.Assign(access, value);
                        }
                        case Lexer.Token.L_PARENTHESES -> {
                            lexer.next();

                            List<Expression> args = new ArrayList<>();
                            var next = lexer.peek();

                            if (next != null && next != Lexer.Token.R_PARENTHESES) {
                                do {
                                    args.add(parseUntil(Lexer.Token.COMMA));
                                    next = lexer.peek();
                                } while (next == Lexer.Token.COMMA && lexer.next() != null);

                                lexer.expect(Lexer.Token.R_PARENTHESES);
                                current = new Expression.Call(access, args);
                            }
                        }
                        case null, default -> throw new IllegalStateException("Unexpected value: " + op);
                    }
                }
                case LITERAL_STR -> {
                    var span = lexer.span();
                    current = new Expression.Str(span.substring(1, span.length() - 1));
                }
                case LITERAL_NUM -> {
                    var span = lexer.span();
                    current = new Expression.Num(Double.parseDouble(span));
                }
                case LITERAL_BOOL -> {
                    var span = lexer.span();
                    current = new Expression.Num(Boolean.parseBoolean(span) ? 1 : 0);
                }
                case null, default -> {}
            }
        }
    }

    private Expression.Access memberAccess() {
        var access = new Expression.Access(null, new Expression.Str(lexer.span()));

        Lexer.Token next;
        loop: while ((next = lexer.next()) != null) {
            switch (next) {
                case Lexer.Token.IDENT -> access = new Expression.Access(access, new Expression.Str(lexer.span()));
                case Lexer.Token.DOT -> {}
                case Lexer.Token.L_BRACKET -> {
                    var field = parseUntil(Lexer.Token.R_BRACKET);
                    lexer.expect(Lexer.Token.R_BRACKET);
                    access = new Expression.Access(access, field);
                }
                default -> {
                    break loop;
                }
            }
        }

        return access;
    }

    public String source() {
        return source;
    }




}
