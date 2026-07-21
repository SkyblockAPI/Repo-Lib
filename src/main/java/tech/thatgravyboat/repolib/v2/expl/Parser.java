package tech.thatgravyboat.repolib.v2.expl;

import tech.thatgravyboat.repolib.v2.RepoLoader;
import tech.thatgravyboat.repolib.v2.expl.expression.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class Parser {
    private final String source;
    private final Lexer lexer;

    public Parser(String source) {
        this.source = source;
        this.lexer = new Lexer(source);
    }

    static class Holder<Type> {

        final String name;
        Optional<Type> expression = Optional.empty();

        Holder(String name) {
            this.name = name;
        }

        void update(Supplier<Type> expressionSupplier) {
            if (expression.isPresent()) {
                throw new UnsupportedOperationException("Duplicate section " + name + "!");
            }

            expression = Optional.of(expressionSupplier.get());
        }

        Type get() {
            return expression.get();
        }

        Type get(Supplier<Type> defaultExpression) {
            return expression.orElseGet(defaultExpression);
        }
    }

    public StackFile parseFile(RepoLoader loader) {
        Holder<Expression> meta = new Holder<>("meta");
        Holder<Expression> script = new Holder<>("script");

        while (lexer.peek() == Lexer.Token.IDENT) {
            lexer.next();
            switch (lexer.span()) {
                case "meta" -> meta.update(this::block);
                case "script" -> script.update(this::block);
            }
        }

        return new StackFile(loader, meta.get(), script.get(StackFile.DEFAULT_SCRIPT));
    }

    public ModuleFile parseModuleFile(RepoLoader loader) {
        Holder<StructExpression> struct = new Holder<>("static");

        if (lexer.peek() == Lexer.Token.IDENT && "static".equals(lexer.peekSpan())) {
            lexer.next();
            struct.update(() -> {
                var expr = this.parseUntil(Lexer.Token.SEMICOLON);
                if (expr instanceof StructExpression structExpression) {
                    return structExpression;
                }

                throw new RuntimeException("Expected struct expression for static data");
            });
            lexer.expect(Lexer.Token.SEMICOLON);
        }

        return new ModuleFile(loader, struct.get(() -> null), parseExpression());
    }

    public Expression parseExpression() {
        List<Expression> expressions = new ArrayList<>();

        while (this.lexer.peek() != null) {
            var expression = parseUntil(Lexer.Token.SEMICOLON, null);
            if (lexer.atEnd()) {
                expressions.add(new BlockExpression.LastElement(expression));
            } else {
                expressions.add(expression);
                if (lexer.peek() == Lexer.Token.SEMICOLON || expression.requiresSemicolon()) {
                    lexer.expect(Lexer.Token.SEMICOLON);
                }
            }
        }

        if (expressions.size() == 1) {
            return expressions.getFirst();
        } else {
            return new BlockExpression(expressions);
        }
    }

    public Expression parseBinaryOrNormalUntil(Lexer.Token... ends) {
        var end = join(ends, Lexer.Token.BINARY);
        var first = parseUntil(end);

        var peek = lexer.peek();
        return switch (peek) {
            case INCLUSIVE_EXCLUSIVE_RANGE,
                 EXCLUSIVE_INCLUSIVE_RANGE,
                 INCLUSIVE_INCLUSIVE_RANGE,
                 EXCLUSIVE_EXCLUSIVE_RANGE -> {
                var type = lexer.next();
                boolean inclusiveStart = type == Lexer.Token.INCLUSIVE_EXCLUSIVE_RANGE || type == Lexer.Token.INCLUSIVE_INCLUSIVE_RANGE;
                boolean inclusiveEnd = type == Lexer.Token.EXCLUSIVE_INCLUSIVE_RANGE || type == Lexer.Token.INCLUSIVE_INCLUSIVE_RANGE;
                var second = parseBinaryOrNormalUntil(end);

                yield new RangeExpression(inclusiveStart, inclusiveEnd, first, second);
            }
            case PLUS -> {
                lexer.next();
                yield new BinaryExpression(BinaryExpression.Op.PLUS, first, parseBinaryOrNormalUntil(end));
            }
            case MINUS -> {
                lexer.next();
                yield new BinaryExpression(BinaryExpression.Op.MINUS, first, parseUntil(end));
            }
            case DIV -> {
                lexer.next();
                yield new BinaryExpression(BinaryExpression.Op.DIV, first, parseUntil(end));
            }
            case MUL -> {
                lexer.next();
                yield new BinaryExpression(BinaryExpression.Op.MUL, first, parseUntil(end));
            }
            case MOD -> {
                lexer.next();
                yield new BinaryExpression(BinaryExpression.Op.MOD, first, parseUntil(end));
            }
            case AND -> {
                lexer.next();
                yield new BinaryExpression(BinaryExpression.Op.AND, first, parseUntil(end));
            }
            case OR -> {
                lexer.next();
                yield new BinaryExpression(BinaryExpression.Op.OR, first, parseUntil(end));
            }
            case GT -> {
                lexer.next();
                yield new BinaryExpression(BinaryExpression.Op.GT, first, parseUntil(end));
            }
            case GTE -> {
                lexer.next();
                yield new BinaryExpression(BinaryExpression.Op.GTE, first, parseUntil(end));
            }
            case LT -> {
                lexer.next();
                yield new BinaryExpression(BinaryExpression.Op.LT, first, parseUntil(end));
            }
            case LTE -> {
                lexer.next();
                yield new BinaryExpression(BinaryExpression.Op.LTE, first, parseUntil(end));
            }
            case null, default -> first;
        };
    }

    public Expression parseUntil(Lexer.Token... end) {
        var endings = new HashSet<>(Arrays.asList(end));
        var expression = switch (lexer.next()) {
            case IDENT -> {
                var access = memberAccess();

                yield switch (lexer.peek()) {
                    case PLUS_ASSIGN -> {
                        lexer.next();
                        yield new AssignExpression(
                                access,
                                new BinaryExpression(BinaryExpression.Op.PLUS, access, parseBinaryOrNormalUntil(end)));
                    }
                    case MINUS_ASSIGN -> {
                        lexer.next();
                        yield new AssignExpression(
                                access,
                                new BinaryExpression(BinaryExpression.Op.MINUS, access, parseBinaryOrNormalUntil(end)));
                    }
                    case DIV_ASSIGN -> {
                        lexer.next();
                        yield new AssignExpression(
                                access,
                                new BinaryExpression(BinaryExpression.Op.DIV, access, parseBinaryOrNormalUntil(end)));
                    }
                    case MUL_ASSIGN -> {
                        lexer.next();
                        yield new AssignExpression(
                                access,
                                new BinaryExpression(BinaryExpression.Op.MUL, access, parseBinaryOrNormalUntil(end)));
                    }
                    case MOD_ASSIGN -> {
                        lexer.next();
                        yield new AssignExpression(
                                access,
                                new BinaryExpression(BinaryExpression.Op.MOD, access, parseBinaryOrNormalUntil(end)));
                    }
                    case Lexer.Token.EQUALS -> {
                        lexer.next();

                        var value = parseBinaryOrNormalUntil(end);

                        yield new AssignExpression(access, value);
                    }
                    case Lexer.Token.L_PARENTHESES -> {
                        lexer.next();

                        List<Expression> args = new ArrayList<>();
                        var next = lexer.peek();

                        if (next != null && next != Lexer.Token.R_PARENTHESES) {
                            do {
                                args.add(parseBinaryOrNormalUntil(Lexer.Token.COMMA, Lexer.Token.R_PARENTHESES));
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
                var negate = lexer.peek() == Lexer.Token.NOT;

                if (negate || lexer.peek() == Lexer.Token.IN) {
                    if (negate) {
                        lexer.expect(Lexer.Token.NOT);
                    }
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
            case DEBUG -> DebugExpression.INSTANCE;
            case L_BRACE -> {
                var fields = new StructExpression(new HashMap<>());

                while (lexer.peek() != Lexer.Token.R_BRACE) {
                    String field;
                    if (lexer.peek() == Lexer.Token.IDENT) {
                        lexer.next();
                        field = lexer.span();
                    } else {
                        lexer.expect(Lexer.Token.LITERAL_STR);
                        field = lexer.span();
                        field = field.substring(1, field.length() - 1);
                    }

                    if (lexer.peek() == Lexer.Token.COLON) {
                        lexer.expect(Lexer.Token.COLON);
                        fields.fields().put(field, parseBinaryOrNormalUntil(Lexer.Token.COMMA, Lexer.Token.R_BRACE));
                    } else {
                        fields.fields().put(field, new AccessExpression(null, new StrExpression(field)));
                        if (this.lexer.peek() != Lexer.Token.COMMA) break;
                    }

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
                    array.list().add(parseBinaryOrNormalUntil(Lexer.Token.COMMA, Lexer.Token.R_BRACKET));

                    if (lexer.peek() == Lexer.Token.COMMA) {
                        lexer.next();
                    }
                }
                lexer.expect(Lexer.Token.R_BRACKET);

                yield array;
            }
            case L_PARENTHESES -> {
                var expr = parseBinaryOrNormalUntil(join(end, Lexer.Token.R_PARENTHESES));
                lexer.expect(Lexer.Token.R_PARENTHESES);
                yield expr;
            }
            case RETURN -> {
                if (lexer.peek() != null && !endings.contains(lexer.peek())) yield new ReturnExpression(parseBinaryOrNormalUntil(end));
                yield new StatementExpression(StatementExpression.Op.RETURN);
            }
            case BREAK -> new StatementExpression(StatementExpression.Op.BREAK);
            case CONTINUE -> new StatementExpression(StatementExpression.Op.CONTINUE);
            case MINUS -> new UnaryExpression(UnaryExpression.Op.NEGATE, parseUntil(end));
            case NOT -> new UnaryExpression(UnaryExpression.Op.NOT, parseUntil(end));
            case MATCH -> matchExpr();
            case null, default -> throw new IllegalStateException("Unexpected value: " + lexer.span());
        };

        if (endings.contains(lexer.peek())) {
            return expression;
        }

        if (!expression.requiresSemicolon() && lexer.peek() != Lexer.Token.SEMICOLON) {
            return expression;
        }
        throw new IllegalStateException("Expected one of " + endings + " but got " + lexer.peek() + " near " + lexer.near());
    }

    private Lexer.Token[] join(Lexer.Token[] first, Lexer.Token... tokens) {
        var newArray = new Lexer.Token[first.length + tokens.length];
        System.arraycopy(first, 0, newArray, 0, first.length);
        System.arraycopy(tokens, 0, newArray, first.length, tokens.length);
        return newArray;
    }

    private Expression matchExpr() {
        final Expression value;
        if (lexer.peek() == Lexer.Token.L_PARENTHESES) {
            lexer.expect(Lexer.Token.L_PARENTHESES);
            value = parseBinaryOrNormalUntil(Lexer.Token.R_PARENTHESES);
            lexer.expect(Lexer.Token.R_PARENTHESES);
        } else {
            value = new BoolExpression(true);
        }

        var branches = new LinkedList<MatchExpression.MatchBranch>();

        lexer.expect(Lexer.Token.L_BRACE);
        while (lexer.peek() != Lexer.Token.R_BRACE) {
            MatchExpression.MatchCondition condition;
            switch (lexer.peek()) {
                case GT -> condition = MatchExpression.MatchCondition.GT;
                case LT -> condition = MatchExpression.MatchCondition.LT;
                case LTE -> condition = MatchExpression.MatchCondition.LTE;
                case GTE -> condition = MatchExpression.MatchCondition.GTE;
                case ELSE -> condition = MatchExpression.MatchCondition.ELSE;
                case null, default -> condition = MatchExpression.MatchCondition.EQUALS;
            }

            if (condition != MatchExpression.MatchCondition.EQUALS) {
                lexer.next();
            }

            var check = condition == MatchExpression.MatchCondition.ELSE ? null : parseUntil(Lexer.Token.LAMBDA_ARROW);
            lexer.expect(Lexer.Token.LAMBDA_ARROW);
            var branch = scopeOrSingleStatement();

            lexer.expect(Lexer.Token.COMMA);
            branches.add(new MatchExpression.MatchBranch(condition, check, branch));
        }
        lexer.expect(Lexer.Token.R_BRACE);

        return new MatchExpression(value, branches);
    }

    private Expression ifExpr() {
        lexer.expect(Lexer.Token.L_PARENTHESES);
        var cond = parseBinaryOrNormalUntil(Lexer.Token.R_PARENTHESES);
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

        Expression init = parseUntil(Lexer.Token.SEMICOLON, Lexer.Token.COLON);

        if (lexer.peek() == Lexer.Token.COLON  && init instanceof AccessExpression access) {
            lexer.expect(Lexer.Token.COLON);

            var array = parseUntil(Lexer.Token.R_PARENTHESES);
            lexer.expect(Lexer.Token.R_PARENTHESES);

            var body = scopeOrSingleStatement();

            return new ForEachExpression(access, array, body);
        } else {
            lexer.expect(Lexer.Token.SEMICOLON);

            Expression cond = parseUntil(Lexer.Token.SEMICOLON);
            lexer.expect(Lexer.Token.SEMICOLON);

            Expression incr = parseUntil(Lexer.Token.R_PARENTHESES);
            lexer.expect(Lexer.Token.R_PARENTHESES);

            var body = scopeOrSingleStatement();

            return new ForExpression(init, cond, incr, body);
        }
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
            var expression = parseUntil(Lexer.Token.SEMICOLON, Lexer.Token.R_BRACE);
            if (lexer.peek() == Lexer.Token.R_BRACE) {
                block.exprs().add(new BlockExpression.LastElement(expression));
            } else {
                block.exprs().add(expression);
                lexer.expect(Lexer.Token.SEMICOLON);
            }
        }
        lexer.expect(Lexer.Token.R_BRACE);

        return block;
    }

    private AccessExpression memberAccess() {
        var access = new AccessExpression(null, new StrExpression(lexer.span()));

        Lexer.Token next;
        loop:
        while ((next = lexer.peek()) != null) {
            switch (next) {
                case Lexer.Token.IDENT -> {
                    lexer.next();
                    access = new AccessExpression(access, new StrExpression(lexer.span()));
                }
                case Lexer.Token.DOT -> lexer.next();
                case Lexer.Token.L_BRACKET -> {
                    lexer.next();
                    var field = parseBinaryOrNormalUntil(Lexer.Token.R_BRACKET);
                    lexer.expect(Lexer.Token.R_BRACKET);
                    access = new AccessExpression(access, field);
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
