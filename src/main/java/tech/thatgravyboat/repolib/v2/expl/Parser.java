package tech.thatgravyboat.repolib.v2.expl;

import tech.thatgravyboat.repolib.v2.expl.expression.*;

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
            return new Block(expressions);
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

                            var value = parseUntil(end);

                            current = new Assign(access, value);
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
                            current = new Call(access, args);
                        }
                        case null, default -> current = access;
                    }
                }
                case IF -> current = ifExpr();
                case FOR -> current = forExpr();
                case LITERAL_STR -> {
                    var span = lexer.span();

                    if (lexer.peek() == Lexer.Token.IN) {
                        lexer.next();
                        lexer.next();
                        var access = memberAccess();
                        current = new In(access, span.substring(1, span.length() - 1));
                        break;
                    }

                    current = new Str(span.substring(1, span.length() - 1));
                }
                case LITERAL_NUM -> {
                    var span = lexer.span();
                    current = new Num(Double.parseDouble(span));
                }
                case LITERAL_BOOL -> {
                    var span = lexer.span();
                    current = new Bool(Boolean.parseBoolean(span));
                }
                case L_BRACE -> {
                    var fields = new Struct(new HashMap<>());

                    while (lexer.peek() != Lexer.Token.R_BRACE) {
                        String field;
                        if (lexer.peek() == Lexer.Token.IDENT) {
                            lexer.next();
                            field = lexer.span();
                        } else {
                            lexer.expect(Lexer.Token.LITERAL_STR);
                            field =  lexer.span();
                            field = field.substring(1, field.length() - 1);
                        }

                        lexer.expect(Lexer.Token.COLON);
                        fields.fields().put(field, parseUntil(Lexer.Token.COMMA, Lexer.Token.R_BRACE));

                        if (lexer.peek() == Lexer.Token.COMMA) {
                            lexer.next();
                        }
                    }
                    lexer.expect(Lexer.Token.R_BRACE);

                    current = fields;
                }
                case L_BRACKET -> {
                    Array array = new Array(new ArrayList<>());

                    while (lexer.peek() != Lexer.Token.R_BRACKET) {
                        array.list().add(parseUntil(Lexer.Token.COMMA, Lexer.Token.R_BRACKET));

                        if (lexer.peek() == Lexer.Token.COMMA) {
                            lexer.next();
                        }
                    }
                    lexer.expect(Lexer.Token.R_BRACKET);

                    current = array;
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

        return new If(cond, thenExpr, elseExpr);
    }

    private Expression forExpr() {
        lexer.expect(Lexer.Token.L_PARENTHESES);

        Expression init = parseUntil(Lexer.Token.SEMICOLON);
        lexer.expect(Lexer.Token.SEMICOLON);

        Expression cond = parseUntil(Lexer.Token.SEMICOLON);
        lexer.expect(Lexer.Token.SEMICOLON);

        Expression incr = parseUntil(Lexer.Token.R_PARENTHESES);
        lexer.expect(Lexer.Token.R_PARENTHESES);

        var body = scopeOrSingleStatement();

        return new For(init, cond, incr, body);
    }

    private Expression scopeOrSingleStatement() {
        if (lexer.peek() == Lexer.Token.L_BRACE) {
            return block();
        }
        return parseUntil(Lexer.Token.SEMICOLON);
    }

    private Block block() {
        lexer.expect(Lexer.Token.L_BRACE);

        var block = new Block(new ArrayList<>());

        while (lexer.peek() != Lexer.Token.R_BRACE) {
            block.exprs().add(parseUntil(Lexer.Token.SEMICOLON));
            lexer.expect(Lexer.Token.SEMICOLON);
        }
        lexer.expect(Lexer.Token.R_BRACE);

        return block;
    }

    private Access memberAccess() {
        var access = new Access(null, lexer.span());

        Lexer.Token next;
        loop:
        while ((next = lexer.peek()) != null) {
            switch (next) {
                case Lexer.Token.IDENT -> {
                    lexer.next();
                    access = new Access(access, lexer.span());
                }
                case Lexer.Token.DOT -> lexer.next();
                case Lexer.Token.L_BRACKET -> {
                    lexer.next();
                    lexer.expect(Lexer.Token.LITERAL_STR);
                    var field = lexer.span();
                    lexer.expect(Lexer.Token.R_BRACKET);
                    access = new Access(access, field.substring(1, field.length() - 1));
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
