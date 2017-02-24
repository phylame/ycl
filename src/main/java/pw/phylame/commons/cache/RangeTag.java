package pw.phylame.commons.cache;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class RangeTag {
    static final RangeTag EMPTY = new RangeTag(0, 0);

    final long offset;
    final long length;
}
