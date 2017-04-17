package li.cil.architect.common.json;

import li.cil.architect.common.config.converter.Blacklist;
import li.cil.architect.common.config.converter.BlockStateFilter;

public class BlacklistAdapter extends FilterListAdapter<BlockStateFilter, Blacklist> {
    @Override
    protected Blacklist newInstance() {
        return new Blacklist();
    }

    @Override
    protected Class<BlockStateFilter> getFilterClass() {
        return BlockStateFilter.class;
    }
}
