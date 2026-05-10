package tech.thatgravyboat.repolib.v2.expl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public final class Parser {
    private final String source;
    private final Lexer lexer;

    public Parser(String source) {
        this.source = source;
        this.lexer = new Lexer(source);
    }

    public StackFile parseFile() {
        Expression meta;
        Expression script;

        lexer.expect(Lexer.Token.IDENT, "meta");
        meta = this.block();

        lexer.expect(Lexer.Token.IDENT, "script");
        script = this.block();

        return new StackFile(meta, script);
    }

    public Expression parseExpression() {
        List<Expression> expressions = new ArrayList<>();

        while (this.lexer.peek() != null) {
            expressions.add(parseUntil(Lexer.Token.SEMICOLON));
            lexer.expect(Lexer.Token.SEMICOLON);
        }

        if (expressions.size() == 1) {
            return expressions.getFirst();
        } else {
            return new Expression.Block(expressions);
        }
    }

    public Expression parseUntil(Lexer.Token... end) {
        Set<Lexer.Token> endSet = Set.of(end);
        Expression current = null;

        while (true) {

            if (lexer.peek() == null || endSet.contains(lexer.peek())) {
                return current;
            }

            switch (lexer.next()) {
                case IDENT -> {
                    var access = memberAccess();

                    switch (lexer.peek()) {
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
                                    args.add(parseUntil(Lexer.Token.COMMA, Lexer.Token.R_PARENTHESES));
                                    next = lexer.peek();
                                } while (next == Lexer.Token.COMMA && lexer.next() != null);
                            }
                            lexer.expect(Lexer.Token.R_PARENTHESES);
                            current = new Expression.Call(access, args);
                        }
                        case null, default -> {
                            current = access;
                        }
                    }
                }
                case IF -> current = ifExpr();
                case LITERAL_STR -> {
                    var span = lexer.span();

                    if (lexer.peek() == Lexer.Token.IN) {
                        lexer.next();
                        lexer.next();
                        var access = memberAccess();
                        current = new Expression.In(access, span.substring(1, span.length() - 1));
                        break;
                    }

                    current = new Expression.Str(span.substring(1, span.length() - 1));
                }
                case LITERAL_NUM -> {
                    var span = lexer.span();
                    current = new Expression.Num(Double.parseDouble(span));
                }
                case LITERAL_BOOL -> {
                    var span = lexer.span();
                    current = new Expression.Bool(Boolean.parseBoolean(span));
                }
                case L_BRACE -> {
                    var fields = new Expression.Struct(new HashMap<>());

                    while (lexer.peek() != Lexer.Token.R_BRACE) {
                        lexer.expect(Lexer.Token.LITERAL_STR);

                        var field = lexer.span();
                        field = field.substring(1, field.length() - 1);

                        lexer.expect(Lexer.Token.COLON);
                        fields.fields().put(field, parseUntil(Lexer.Token.COMMA, Lexer.Token.R_BRACE));

                        if (lexer.peek() == Lexer.Token.COMMA) {
                            lexer.next();
                        }
                    }
                    lexer.expect(Lexer.Token.R_BRACE);

                    current = fields;
                }
                case null, default -> throw new IllegalStateException("Unexpected value: " + lexer.span());
            }
        }
    }

    private Expression ifExpr() {
        lexer.expect(Lexer.Token.L_PARENTHESES);
        var cond = parseUntil(Lexer.Token.R_PARENTHESES);
        lexer.expect(Lexer.Token.R_PARENTHESES);

        Expression thenExpr = scopeOrSingleStatement();
        Expression elseExpr = null;
        if (lexer.peek() == Lexer.Token.ELSE) {
            lexer.next();

            if (lexer.peek() == Lexer.Token.IF) {
                lexer.next();
                elseExpr = ifExpr();
            } else {
                elseExpr = scopeOrSingleStatement();
            }
        }

        return new Expression.If(cond, thenExpr, elseExpr);
    }

    private Expression scopeOrSingleStatement() {
        if (lexer.peek() == Lexer.Token.L_BRACE) {
            return block();
        }
        return parseUntil(Lexer.Token.SEMICOLON);
    }

    private Expression.Block block() {
        lexer.expect(Lexer.Token.L_BRACE);

        var block = new Expression.Block(new ArrayList<>());

        while (lexer.peek() != Lexer.Token.R_BRACE) {
            block.exprs().add(parseUntil(Lexer.Token.SEMICOLON));
            lexer.expect(Lexer.Token.SEMICOLON);
        }
        lexer.expect(Lexer.Token.R_BRACE);

        return block;
    }

    private Expression.Access memberAccess() {
        var access = new Expression.Access(null, lexer.span());

        Lexer.Token next;
        loop:
        while ((next = lexer.peek()) != null) {
            switch (next) {
                case Lexer.Token.IDENT -> {
                    lexer.next();
                    access = new Expression.Access(access, lexer.span());
                }
                case Lexer.Token.DOT -> lexer.next();
                case Lexer.Token.L_BRACKET -> {
                    lexer.next();
                    lexer.expect(Lexer.Token.LITERAL_STR);
                    var field = lexer.span();
                    lexer.expect(Lexer.Token.R_BRACKET);
                    access = new Expression.Access(access, field.substring(1, field.length() - 1));
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
