package li.cil.architect.common.json;

import li.cil.architect.common.config.converter.TileEntityFilter;
import li.cil.architect.common.config.converter.Whitelist;

public class WhitelistAdapter extends FilterListAdapter<TileEntityFilter, Whitelist> {
    @Override
    protected Whitelist newInstance() {
        return new Whitelist();
    }

    @Override
    protected Class<TileEntityFilter> getFilterClass() {
        return TileEntityFilter.class;
    }
}
