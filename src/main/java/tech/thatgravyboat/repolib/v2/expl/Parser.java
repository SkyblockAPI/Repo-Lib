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
            return new BlockExpression(expressions);
        }
    }

    public Expression parseUntil(Lexer.Token... end) {
        var endings = Set.of(end);
        var expression = switch (lexer.next()) {
            case IDENT -> {
                var access = memberAccess();

                yield switch (lexer.peek()) {
                    case Lexer.Token.EQUALS -> {
                        lexer.next();

                        var value = parseUntil(end);

                        yield new AssignExpression(access, value);
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
                        yield new CallExpression(access, args);
                    }
                    case null, default -> access;
                };
            }
            case IF -> ifExpr();
            case FOR -> forExpr();
            case LITERAL_STR -> {
                var span = lexer.span();
                var negate = lexer.peek() == Lexer.Token.UNARY_NOT;

                if (negate || lexer.peek() == Lexer.Token.IN) {
                    if (negate) lexer.expect(Lexer.Token.UNARY_NOT);
                    lexer.expect(Lexer.Token.IN);
                    lexer.expect(Lexer.Token.IDENT); // The first span must be expected as memberAccess checks for the span right away.
                    var access = memberAccess();
                    var field = span.substring(1, span.length() - 1);
                    var inExpr = new InExpression(access, field);

                    yield negate ? new UnaryExpression(UnaryExpression.Op.NOT, inExpr) : inExpr;
                }

                yield new StrExpression(span.substring(1, span.length() - 1));
            }
            case LITERAL_NUM -> new NumExpression(Double.parseDouble(lexer.span()));
            case LITERAL_BOOL -> new BoolExpression(Boolean.parseBoolean(lexer.span()));
            case L_BRACE -> {
                var fields = new StructExpression(new HashMap<>());

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

                yield fields;
            }
            case L_BRACKET -> {
                ArrayExpression array = new ArrayExpression(new ArrayList<>());

                while (lexer.peek() != Lexer.Token.R_BRACKET) {
                    array.list().add(parseUntil(Lexer.Token.COMMA, Lexer.Token.R_BRACKET));

                    if (lexer.peek() == Lexer.Token.COMMA) {
                        lexer.next();
                    }
                }
                lexer.expect(Lexer.Token.R_BRACKET);

                yield array;
            }
            case RETURN -> new StatementExpression(StatementExpression.Op.RETURN);
            case BREAK -> new StatementExpression(StatementExpression.Op.BREAK);
            case CONTINUE -> new StatementExpression(StatementExpression.Op.CONTINUE);
            case UNARY_MINUS -> new UnaryExpression(UnaryExpression.Op.NEGATE, parseUntil(end));
            case UNARY_NOT -> new UnaryExpression(UnaryExpression.Op.NOT, parseUntil(end));
            case null, default -> throw new IllegalStateException("Unexpected value: " + lexer.span());
        };

        if (endings.contains(lexer.peek())) {
            return expression;
        }
        throw new IllegalStateException("Expected one of " + endings + " but got " + lexer.peek());
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

        return new IfExpression(cond, thenExpr, elseExpr);
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

        return new ForExpression(init, cond, incr, body);
    }

    private Expression scopeOrSingleStatement() {
        if (lexer.peek() == Lexer.Token.L_BRACE) {
            return block();
        }
        return parseUntil(Lexer.Token.SEMICOLON);
    }

    private BlockExpression block() {
        lexer.expect(Lexer.Token.L_BRACE);

        var block = new BlockExpression(new ArrayList<>());

        while (lexer.peek() != Lexer.Token.R_BRACE) {
            block.exprs().add(parseUntil(Lexer.Token.SEMICOLON));
            lexer.expect(Lexer.Token.SEMICOLON);
        }
        lexer.expect(Lexer.Token.R_BRACE);

        return block;
    }

    private AccessExpression memberAccess() {
        var access = new AccessExpression(null, lexer.span());

        Lexer.Token next;
        loop:
        while ((next = lexer.peek()) != null) {
            switch (next) {
                case Lexer.Token.IDENT -> {
                    lexer.next();
                    access = new AccessExpression(access, lexer.span());
                }
                case Lexer.Token.DOT -> lexer.next();
                case Lexer.Token.L_BRACKET -> {
                    lexer.next();
                    lexer.expect(Lexer.Token.LITERAL_STR);
                    var field = lexer.span();
                    lexer.expect(Lexer.Token.R_BRACKET);
                    access = new AccessExpression(access, field.substring(1, field.length() - 1));
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
