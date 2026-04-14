package notification.api.support;

import java.util.List;

public record Page<T>(Boolean isLastPage, Long totalPage, List<T> data) {
}
