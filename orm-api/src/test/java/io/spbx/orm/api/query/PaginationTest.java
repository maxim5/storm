package io.spbx.orm.api.query;

import io.spbx.orm.api.PageToken;
import io.spbx.orm.testing.FakeColumn;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.orm.api.query.Pagination.*;

@Tag("fast")
public class PaginationTest {
    @Test
    public void init_valid_first_page() {
        Pagination pagination = firstPage(3);
        assertThat(pagination.hasOffset()).isFalse();
        assertThat(pagination.offset()).isEqualTo(NO_OFFSET);
        assertThat(pagination.hasLastItem()).isFalse();
        assertThat(pagination.lastItem()).isNull();
        assertThat(pagination.limit()).isEqualTo(3);
    }

    @Test
    public void init_valid_offset() {
        Pagination pagination = ofOffset(10, 5);
        assertThat(pagination.hasOffset()).isTrue();
        assertThat(pagination.offset()).isEqualTo(10);
        assertThat(pagination.hasLastItem()).isFalse();
        assertThat(pagination.lastItem()).isNull();
        assertThat(pagination.limit()).isEqualTo(5);
    }

    @Test
    public void init_valid_column_term() {
        Pagination pagination = ofColumn(FakeColumn.FOO.makeVar("x"), Order.ASC, 20);
        assertThat(pagination.hasOffset()).isFalse();
        assertThat(pagination.offset()).isEqualTo(NO_OFFSET);
        assertThat(pagination.hasLastItem()).isTrue();
        assertThat(pagination.lastItem()).isEqualTo(FakeColumn.FOO.makeVar("x"));
        assertThat(pagination.limit()).isEqualTo(20);
        assertThat(pagination.order()).isEqualTo(Order.ASC);
    }

    @Test
    public void init_valid_column_desc_term() {
        Pagination pagination = ofColumnDesc(FakeColumn.FOO.makeVar("x"), 30);
        assertThat(pagination.hasOffset()).isFalse();
        assertThat(pagination.offset()).isEqualTo(NO_OFFSET);
        assertThat(pagination.hasLastItem()).isTrue();
        assertThat(pagination.lastItem()).isEqualTo(FakeColumn.FOO.makeVar("x"));
        assertThat(pagination.limit()).isEqualTo(30);
        assertThat(pagination.order()).isEqualTo(Order.DESC);
    }

    @Test
    public void optional_chaining() {
        assertThat(PageToken.parseHumanToken("x").map(token -> ofOffsetIfMatches(token, 5))).isEmpty();
        assertThat(PageToken.parseHumanToken("1").map(token -> ofOffsetIfMatches(token, 5))).isEmpty();
        assertThat(PageToken.parseHumanToken(":1").map(token -> ofOffsetIfMatches(token, 5))).hasValue(ofOffset(1, 5));

        assertThat(PageToken.parseHumanToken(":1").map(token -> ofColumnIfMatches(token, FakeColumn.FOO, Order.ASC, 5)))
            .isEmpty();
        assertThat(PageToken.parseHumanToken(":x").map(token -> ofColumnIfMatches(token, FakeColumn.FOO, Order.ASC, 5)))
            .isEmpty();
        assertThat(PageToken.parseHumanToken("x").map(token -> ofColumnIfMatches(token, FakeColumn.FOO, Order.ASC, 5)))
            .hasValue(ofColumnAsc(FakeColumn.FOO.makeVar("x"), 5));
    }
}
