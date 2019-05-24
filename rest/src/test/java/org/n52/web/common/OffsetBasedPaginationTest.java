
package org.n52.web.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.hamcrest.core.Is;
import org.junit.Test;

public class OffsetBasedPaginationTest {

    @Test
    public void startingWithOutOfBoundsOffsetThenLastPageEmpty() {
        OffsetBasedPagination pagination = new OffsetBasedPagination(201, 20);
        Optional<Pagination> last = pagination.last(200);
        assertFalse(last.isPresent());
    }

    @Test
    public void startingWithZeroOffsetThenFirstPageDetermined() {
        OffsetBasedPagination pagination = new OffsetBasedPagination(0, 20);
        Optional<Pagination> last = pagination.first(200);
        assertThat(last.get().getOffset(), Is.is(0L));
        assertThat(last.get().getLimit(), Is.is(20L));
    }

    @Test
    public void startingWithZeroOffsetThenLastPageDetermined() {
        OffsetBasedPagination pagination = new OffsetBasedPagination(0, 20);
        Optional<Pagination> last = pagination.last(200);
        assertThat(last.get().getOffset(), Is.is(180L));
        assertThat(last.get().getLimit(), Is.is(20L));
    }

}
