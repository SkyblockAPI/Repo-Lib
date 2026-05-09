package tech.thatgravyboat.repolib;

import tech.thatgravyboat.repolib.v2.Expression;
import tech.thatgravyboat.repolib.v2.Value;

public record KillingMyself(
        Value.Struct meta,
        Expression item
) {
}
